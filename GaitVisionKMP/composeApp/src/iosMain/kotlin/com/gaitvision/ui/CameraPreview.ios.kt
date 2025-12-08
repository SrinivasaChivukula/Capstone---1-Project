package com.gaitvision.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.gaitvision.platform.PoseDetector
import com.gaitvision.platform.VideoProcessor
import com.gaitvision.platform.Pose
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.AVFoundation.*
import platform.CoreMedia.CMSampleBufferRef
import platform.CoreMedia.CMSampleBufferGetImageBuffer
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.*
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create
import platform.darwin.NSObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraPreview(
    modifier: Modifier,
    poseDetector: PoseDetector,
    videoProcessor: VideoProcessor,
    onPoseDetected: (Pose) -> Unit
) {
    val cameraController = remember { CameraViewController(poseDetector, onPoseDetected) }

    DisposableEffect(Unit) {
        cameraController.startSession()
        onDispose {
            cameraController.stopSession()
        }
    }

    UIKitView(
        factory = {
            val view = UIView()
            view.backgroundColor = UIColor.blackColor
            cameraController.view.frame = view.bounds
            view.addSubview(cameraController.view)
            cameraController.setupPreviewLayer(view)
            view
        },
        modifier = modifier.fillMaxSize(),
        onResize = { view: UIView, rect: CValue<platform.CoreGraphics.CGRect> ->
            CATransaction.begin()
            CATransaction.setValue(true, kCATransactionDisableActions)
            view.layer.setFrame(rect)
            cameraController.previewLayer?.setFrame(rect)
            CATransaction.commit()
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
private class CameraViewController(
    private val poseDetector: PoseDetector,
    private val onPoseDetected: (Pose) -> Unit
) : UIViewController(null, null), AVCaptureVideoDataOutputSampleBufferDelegateProtocol {

    private val captureSession = AVCaptureSession()
    var previewLayer: AVCaptureVideoPreviewLayer? = null
    private val cameraQueue = dispatch_queue_create("cameraQueue", null)
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun viewDidLoad() {
        super.viewDidLoad()
        setupCamera()
    }

    fun setupPreviewLayer(container: UIView) {
        previewLayer = AVCaptureVideoPreviewLayer(session = captureSession)
        previewLayer?.videoGravity = AVLayerVideoGravityResizeAspectFill
        previewLayer?.frame = container.bounds
        container.layer.addSublayer(previewLayer!!)
    }

    private fun setupCamera() {
        captureSession.beginConfiguration()

        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo) ?: return
        val input = try {
            AVCaptureDeviceInput.deviceInputWithDevice(device, null)
        } catch (e: Exception) {
            return
        }

        if (captureSession.canAddInput(input)) {
            captureSession.addInput(input)
        }

        val output = AVCaptureVideoDataOutput()
        output.setSampleBufferDelegate(this, cameraQueue)
        
        if (captureSession.canAddOutput(output)) {
            captureSession.addOutput(output)
        }

        captureSession.commitConfiguration()
    }

    fun startSession() {
        dispatch_async(cameraQueue) {
            if (!this@CameraViewController.captureSession.running) {
                this@CameraViewController.captureSession.startRunning()
            }
        }
    }

    fun stopSession() {
        dispatch_async(cameraQueue) {
            if (this@CameraViewController.captureSession.running) {
                this@CameraViewController.captureSession.stopRunning()
            }
        }
    }

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection
    ) {
        val sampleBuffer = didOutputSampleBuffer ?: return
        val pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) ?: return

        // We are on a background thread here (cameraQueue)
        // Pose detection is suspend function, so we launch a coroutine
        // Ideally we should use a dedicated scope/dispatcher for processing to avoid blocking main thread
        // But detectPose likely dispatches internally or is fast enough.
        // Since detectPose is suspend, we need a scope.
        
        scope.launch(Dispatchers.Default) {
             val pose = poseDetector.detectPose(pixelBuffer)
             if (pose != null) {
                 dispatch_async(dispatch_get_main_queue()) {
                     onPoseDetected(pose)
                 }
             }
        }
    }
}
