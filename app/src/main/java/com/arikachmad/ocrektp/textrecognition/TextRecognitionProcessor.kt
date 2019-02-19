package com.arikachmad.ocrektp.textrecognition

import android.graphics.Bitmap
import com.arikachmad.ocrektp.VisionProcessorBase
import com.arikachmad.ocrektp.common.CameraImageGraphic
import com.arikachmad.ocrektp.common.FrameMetadata
import com.arikachmad.ocrektp.common.GraphicOverlay
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import timber.log.Timber
import java.io.IOException
import java.util.regex.Pattern

/** Processor for the text recognition demo.  */
class TextRecognitionProcessor : VisionProcessorBase<FirebaseVisionText>() {

    private val detector: FirebaseVisionTextRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
    private var listener : ((String) -> Unit)? = null


    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Timber.d("Exception thrown while trying to close Text Detector: $e")
        }
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<FirebaseVisionText> {
        return detector.processImage(image)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        results: FirebaseVisionText,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        graphicOverlay.clear()
        originalCameraImage?.let { image ->
            val imageGraphic = CameraImageGraphic(graphicOverlay, image)
            graphicOverlay.add(imageGraphic)
        }
        val blocks = results.textBlocks
        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val elements = lines[j].elements
                for (k in elements.indices) {
                    val textGraphic = TextGraphic(graphicOverlay, elements[k])
                    graphicOverlay.add(textGraphic)
                    val regexKtpPattern = "[0-9]{16}"
                    val pattern = Pattern.compile(regexKtpPattern)
                    val matcher = pattern.matcher(elements[k].text)
                    if (matcher.find()) {
                        listener?.invoke(matcher.group())
                    }

                }
            }
        }
        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Timber.w("Text detection failed.$e")
    }

    fun readingText(text: (String) -> Unit) {
        listener = text
    }


    companion object {

        private const val TAG = "TextRecProc"
    }
}
