package com.arikachmad.ocrektp

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arikachmad.ocrektp.permissionkit.askPermissions
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.marchinram.rxgallery.RxGallery
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import org.michaelbel.bottomsheet.BottomSheet
import timber.log.Timber
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    private lateinit var uriPath: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        buttonAdd.setOnClickListener {
            showBottomView()
        }
        buttonDetect.setOnClickListener {
            if (::uriPath.isInitialized) {
                startOCR()
            }
        }
        liveCheck.setOnClickListener {
            startActivity(intentFor<LivePreviewActivity>())
        }
        reqPermission()

    }

    private fun reqPermission() {
        askPermissions(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) {
            onGranted {
                toast("granted")
            }

            onDenied {
                toast("wajib goblok")
            }

            onShowRationale {
                toast("wajib goblok")
            }

            onNeverAskAgain {
                toast("wajib goblok")
            }
        }
    }

    private fun startOCR() {
        FirebaseApp.initializeApp(this)
        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
        val image = FirebaseVisionImage.fromFilePath(this, uriPath)
        detector.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                for (blockText in firebaseVisionText.textBlocks) {
                    Timber.d(blockText.text)
                    val regexKtpPattern = "[0-9]{8,16}"
                    val pattern = Pattern.compile(regexKtpPattern)
                    val matcher = pattern.matcher(blockText.text)
                    if (matcher.find()) {
                        textoutput.text = matcher.group()
                    }
                }
            }
            .addOnFailureListener {
                Timber.e(it)
                toast("Failed")
            }
    }

    private fun showBottomView() {

        val items = arrayOf("Camera", "Galery")
        val builder = BottomSheet.Builder(this)
        builder.setDarkTheme(false)
        builder.setWindowDimming(80)
        builder.setDividers(false)
        builder.setFullWidth(false)
        builder.setItems(
            items
        ) { dialog, which ->
            when (which) {
                0 -> {
                    RxGallery.photoCapture(this).subscribe({ uriPhoto ->
                        Timber.d(uriPhoto.toString())
                        uriPath = uriPhoto

                    }, { failed ->
                        failed.message?.let { toast(it) }
                    })
                }
                1 -> {
                    RxGallery.gallery(this, false, RxGallery.MimeType.IMAGE).subscribe({ uriPhoto ->
                        Timber.d(uriPhoto.toString())
                        uriPath = uriPhoto[0]


                    }, { failed ->
                        failed.message?.let { toast(it) }
                    })
                }
            }
        }
        builder.show()
    }

}
