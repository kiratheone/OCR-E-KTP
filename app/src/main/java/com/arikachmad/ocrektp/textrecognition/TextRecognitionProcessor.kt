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
    private var listener: ((String) -> Unit)? = null

    var nik: String = ""
    var nikIndex: Int = -1
    var nama: String = ""
    var namaIndex: Int = -1
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
        nik = ""
        nama = ""
        nikIndex = -1
        namaIndex - 1

        graphicOverlay.clear()
        originalCameraImage?.let { image ->
            val imageGraphic = CameraImageGraphic(graphicOverlay, image)
            graphicOverlay.add(imageGraphic)
        }
        val lines = results.text.lines()
        for ((index, value) in lines.withIndex()) {
            recognizeNIK(index, value)
        }
        if (namaIndex > -1 && namaIndex < lines.size) {
            var numeric = true
            numeric = lines[namaIndex].matches("-?\\d+(\\.\\d+)?".toRegex())
            if (!numeric && lines[namaIndex].indexOf(",") == -1 && lines[namaIndex].indexOf("-") == -1)
                nama = lines[namaIndex]
        }
        if (!nik.isEmpty() && !nama.isEmpty()) {
            listener?.invoke("NIK : $nik\nNama : $nama")
        }
        graphicOverlay.postInvalidate()
    }

    fun recognizeNIK(index: Int, text: String) {
        val regexKtpPattern = "[0-9]{16}"
        val pattern = Pattern.compile(regexKtpPattern)
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            nik = matcher.group()
            nikIndex = index
            namaIndex = nikIndex + 1
        }
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
