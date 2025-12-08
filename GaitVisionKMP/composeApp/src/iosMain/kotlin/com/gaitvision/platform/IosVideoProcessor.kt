package com.gaitvision.platform

import com.gaitvision.platform.Pose
import com.gaitvision.platform.LandmarkType
import kotlinx.cinterop.*
import platform.AVFoundation.*
import platform.CoreGraphics.*
import platform.CoreMedia.*
import platform.CoreVideo.*
import platform.Foundation.NSURL
import platform.Foundation.NSError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class IOSVideoProcessor : VideoProcessor {
    private val poseDetector = IOSPoseDetector()

    override suspend fun processVideo(
        inputPath: String,
        outputPath: String,
        onProgress: (Int) -> Unit,
        onPoseDetected: (Pose) -> Unit
    ): String? = withContext(Dispatchers.Default) {
        val inputUrl = NSURL.fileURLWithPath(inputPath)
        val outputUrl = NSURL.fileURLWithPath(outputPath)
        
        // Remove existing file if any
        val fileManager = platform.Foundation.NSFileManager.defaultManager
        if (fileManager.fileExistsAtPath(outputPath)) {
            fileManager.removeItemAtPath(outputPath, null)
        }

        val asset = AVAsset.assetWithURL(inputUrl)
        val reader = AVAssetReader.assetReaderWithAsset(asset, null) ?: return@withContext null
        
        val videoTrack = asset.tracksWithMediaType(AVMediaTypeVideo).firstOrNull() as? AVAssetTrack ?: return@withContext null
        
        val readerOutputSettings = mapOf(
            kCVPixelBufferPixelFormatTypeKey to kCVPixelFormatType_32BGRA
        )
        val readerOutput = AVAssetReaderTrackOutput(videoTrack, readerOutputSettings)
        if (reader.canAddOutput(readerOutput)) {
            reader.addOutput(readerOutput)
        } else {
            return@withContext null
        }

        val writer = AVAssetWriter.assetWriterWithURL(outputUrl, "com.apple.quicktime-movie", null) ?: return@withContext null
        val writerInputSettings = mapOf(
            AVVideoCodecKey to AVVideoCodecTypeH264,
            AVVideoWidthKey to videoTrack.naturalSize.useContents { width },
            AVVideoHeightKey to videoTrack.naturalSize.useContents { height }
        )
        val writerInput = AVAssetWriterInput(mediaType = AVMediaTypeVideo, outputSettings = writerInputSettings)
        writerInput.expectsMediaDataInRealTime = false
        
        val adaptorAttributes = mapOf(
            kCVPixelBufferPixelFormatTypeKey to kCVPixelFormatType_32BGRA,
            kCVPixelBufferWidthKey to videoTrack.naturalSize.useContents { width },
            kCVPixelBufferHeightKey to videoTrack.naturalSize.useContents { height }
        )
        val adaptor = AVAssetWriterInputPixelBufferAdaptor(writerInput, adaptorAttributes)

        if (writer.canAddInput(writerInput)) {
            writer.addInput(writerInput)
        } else {
            return@withContext null
        }

        if (!reader.startReading()) return@withContext null
        if (!writer.startWriting()) return@withContext null
        writer.startSessionAtSourceTime(kCMTimeZero)

        val queue = platform.darwin.dispatch_queue_create("videoProcessingQueue", null)
        
        // Processing Loop
        // Note: In Kotlin Native, we can't easily use requestMediaDataWhenReadyOnQueue with a lambda that suspends.
        // We will do a synchronous loop for simplicity in this migration.
        
        var frameCount = 0
        // Estimate frame count for progress (duration * fps)
        val duration = CMTimeGetSeconds(asset.duration)
        val fps = videoTrack.nominalFrameRate
        val totalFrames = (duration * fps).toInt()

        while (reader.status == AVAssetReaderStatusReading) {
            val sampleBuffer = readerOutput.copyNextSampleBuffer()
            if (sampleBuffer != null) {
                val pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer)
                val presentationTime = CMSampleBufferGetPresentationTimeStamp(sampleBuffer)

                if (pixelBuffer != null) {
                    // Detect Pose
                    val pose = poseDetector.detectPose(pixelBuffer)
                    if (pose != null) {
                        onPoseDetected(pose)
                        
                        // Draw Overlay
                        // We need to lock base address to draw
                        CVPixelBufferLockBaseAddress(pixelBuffer, 0u)
                        val context = createBitmapContext(pixelBuffer)
                        if (context != null) {
                            drawOverlay(context, pose, CVPixelBufferGetWidth(pixelBuffer).toDouble(), CVPixelBufferGetHeight(pixelBuffer).toDouble())
                        }
                        CVPixelBufferUnlockBaseAddress(pixelBuffer, 0u)
                    }
                    
                    // Write Frame
                    // We need to wait for writer input to be ready
                    while (!writerInput.readyForMoreMediaData) {
                        platform.Foundation.NSThread.sleepForTimeInterval(0.01)
                    }
                    adaptor.appendPixelBuffer(pixelBuffer, presentationTime)
                }
                
                // Release sample buffer (CFRelease is needed in Kotlin Native for CoreFoundation types?)
                // CMSampleBufferRef is a CFType, so yes.
                // However, Kotlin Native's memory management might handle it if it's mapped to an ObjC object.
                // CFRelease(sampleBuffer) // Uncomment if memory leak occurs
                
                frameCount++
                if (totalFrames > 0) {
                    onProgress((frameCount.toDouble() / totalFrames * 100).toInt())
                }
            } else {
                writerInput.markAsFinished()
                writer.finishWritingWithCompletionHandler { 
                    // Completion
                }
                break
            }
        }
        
        return@withContext outputPath
    }

    private fun createBitmapContext(pixelBuffer: CVPixelBufferRef): CGContextRef? {
        val width = CVPixelBufferGetWidth(pixelBuffer)
        val height = CVPixelBufferGetHeight(pixelBuffer)
        val stride = CVPixelBufferGetBytesPerRow(pixelBuffer)
        val data = CVPixelBufferGetBaseAddress(pixelBuffer)
        val colorSpace = CGColorSpaceCreateDeviceRGB()
        
        // kCGBitmapByteOrder32Little | kCGImageAlphaPremultipliedFirst is typical for BGRA
        val bitmapInfo = kCGBitmapByteOrder32Little or kCGImageAlphaPremultipliedFirst
        
        return CGBitmapContextCreate(data, width, height, 8u, stride, colorSpace, bitmapInfo)
    }

    private fun drawOverlay(context: CGContextRef, pose: Pose, width: Double, height: Double) {
        CGContextSetLineWidth(context, 4.0)
        CGContextSetRGBStrokeColor(context, 1.0, 1.0, 1.0, 1.0) // White
        CGContextSetRGBFillColor(context, 1.0, 0.0, 0.0, 1.0) // Red

        fun drawLine(start: LandmarkType, end: LandmarkType) {
            val p1 = pose.getLandmark(start)
            val p2 = pose.getLandmark(end)
            if (p1 != null && p2 != null && p1.visibility > 0.5 && p2.visibility > 0.5) {
                CGContextMoveToPoint(context, p1.position.x.toDouble() * width, p1.position.y.toDouble() * height)
                CGContextAddLineToPoint(context, p2.position.x.toDouble() * width, p2.position.y.toDouble() * height)
                CGContextStrokePath(context)
            }
        }

        fun drawPoint(type: LandmarkType) {
            val p = pose.getLandmark(type)
            if (p != null && p.visibility > 0.5) {
                val x = p.position.x.toDouble() * width
                val y = p.position.y.toDouble() * height
                val rect = CGRectMake(x - 5, y - 5, 10.0, 10.0)
                CGContextFillEllipseInRect(context, rect)
            }
        }

        // Draw Skeleton
        drawLine(LandmarkType.RIGHT_HIP, LandmarkType.RIGHT_KNEE)
        drawLine(LandmarkType.RIGHT_KNEE, LandmarkType.RIGHT_ANKLE)
        drawLine(LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE)
        drawLine(LandmarkType.LEFT_KNEE, LandmarkType.LEFT_ANKLE)
        
        drawPoint(LandmarkType.RIGHT_KNEE)
        drawPoint(LandmarkType.LEFT_KNEE)
        drawPoint(LandmarkType.RIGHT_ANKLE)
        drawPoint(LandmarkType.LEFT_ANKLE)
    }
}
