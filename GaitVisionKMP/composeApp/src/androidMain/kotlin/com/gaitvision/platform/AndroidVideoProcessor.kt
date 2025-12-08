package com.gaitvision.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroidVideoProcessor(private val context: Context) : VideoProcessor {

    private val poseDetector = AndroidPoseDetector()

    override suspend fun processVideo(
        inputPath: String,
        outputPath: String,
        onProgress: (Int) -> Unit,
        onPoseDetected: (Pose) -> Unit
    ): String? = withContext(Dispatchers.IO) {
        try {
            val fileUri = Uri.parse(inputPath)
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, fileUri)

            val videoLengthMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
            val videoLengthUs = videoLengthMs * 1000L
            
            // Frame extraction logic
            val frameList = mutableListOf<Bitmap>()
            val frameInterval = (1000L * 1000L) / 30 // 30 FPS
            var currTime = 0L
            
            // 1. Extract Frames
            while (currTime <= videoLengthUs) {
                val frame = retriever.getFrameAtTime(currTime, MediaMetadataRetriever.OPTION_CLOSEST)
                if (frame != null) {
                    frameList.add(frame)
                }
                val progress = ((currTime.toDouble() / videoLengthUs) * 50).toInt() // First 50% is extraction
                withContext(Dispatchers.Main) { onProgress(progress) }
                currTime += frameInterval
            }
            retriever.release()

            if (frameList.isEmpty()) return@withContext null

            val firstFrame = frameList[0]
            val width = firstFrame.width
            val height = firstFrame.height

            // 2. Setup Encoder & Muxer
            val mediaMuxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val format = MediaFormat.createVideoFormat("video/avc", width, height)
            format.setInteger(MediaFormat.KEY_BIT_RATE, 2000000)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            
            val encoder = MediaCodec.createEncoderByType("video/avc")
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = encoder.createInputSurface()
            encoder.start()

            val frameDurationUs = 1000000L / 30
            var trackIndex = -1
            var muxerStarted = false
            val bufferInfo = MediaCodec.BufferInfo()

            // 3. Process & Encode Frames
            for ((index, frame) in frameList.withIndex()) {
                // Detect Pose
                val pose = poseDetector.detectPose(frame)
                if (pose != null) {
                    onPoseDetected(pose)
                }
                
                // Draw Overlay
                val modifiedBitmap = drawOverlay(frame, pose)
                
                // Encode
                val canvas = inputSurface.lockCanvas(null)
                canvas.drawBitmap(modifiedBitmap, 0f, 0f, null)
                inputSurface.unlockCanvasAndPost(canvas)

                // Drain encoder output buffers
                while (true) {
                    val outputBufferId = encoder.dequeueOutputBuffer(bufferInfo, 10000)
                    when {
                        outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            if (!muxerStarted) {
                                trackIndex = mediaMuxer.addTrack(encoder.outputFormat)
                                mediaMuxer.start()
                                muxerStarted = true
                            }
                        }
                        outputBufferId >= 0 -> {
                            val outputBuffer = encoder.getOutputBuffer(outputBufferId) ?: continue
                            if (muxerStarted) {
                                bufferInfo.presentationTimeUs = index * frameDurationUs
                                mediaMuxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                            }
                            encoder.releaseOutputBuffer(outputBufferId, false)
                        }
                        outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER -> break
                    }
                }
                
                val progress = 50 + ((index.toDouble() / frameList.size) * 50).toInt()
                withContext(Dispatchers.Main) { onProgress(progress) }
            }

            encoder.signalEndOfInputStream()
            
            // Final drain
            while (true) {
                val outputBufferId = encoder.dequeueOutputBuffer(bufferInfo, 10000)
                if (outputBufferId >= 0) {
                    val outputBuffer = encoder.getOutputBuffer(outputBufferId) ?: break
                    if (muxerStarted) {
                        // Use the last frame's time or increment
                        bufferInfo.presentationTimeUs = frameList.size * frameDurationUs
                        mediaMuxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(outputBufferId, false)
                } else {
                    break
                }
            }

            encoder.stop()
            encoder.release()
            mediaMuxer.stop()
            mediaMuxer.release()

            return@withContext outputPath

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    private fun drawOverlay(bitmap: Bitmap, pose: Pose?): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paintLine = Paint().apply {
            color = Color.WHITE
            strokeWidth = 4f
        }
        val paintDot = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }

        if (pose == null) return mutableBitmap

        // Helper to draw line
        fun drawLine(start: LandmarkType, end: LandmarkType) {
            val p1 = pose.getLandmark(start)
            val p2 = pose.getLandmark(end)
            if (p1 != null && p2 != null && p1.visibility > 0.5 && p2.visibility > 0.5) {
                canvas.drawLine(p1.position.x, p1.position.y, p2.position.x, p2.position.y, paintLine)
            }
        }
        
        // Helper to draw point
        fun drawPoint(type: LandmarkType) {
             val p = pose.getLandmark(type)
             if (p != null && p.visibility > 0.5) {
                 canvas.drawCircle(p.position.x, p.position.y, 5f, paintDot)
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

        return mutableBitmap
    }
}
