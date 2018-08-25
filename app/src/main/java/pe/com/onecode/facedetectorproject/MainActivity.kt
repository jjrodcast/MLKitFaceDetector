package pe.com.onecode.facedetectorproject

import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.wonderkiln.camerakit.*
import kotlinx.android.synthetic.main.activity_main.*
import pe.com.onecode.facedetectorproject.utilities.FaceGraphic

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraView.facing = CameraKit.Constants.FACING_FRONT
        graphicOverlay.setCameraInfo(cameraView.width, cameraView.height, cameraView.facing)

        cameraView.addCameraKitListener(object : CameraKitEventListener {
            override fun onEvent(cameraKitEvent: CameraKitEvent) = Unit
            override fun onImage(cameraKitImage: CameraKitImage) {
                val bitmap = cameraKitImage.bitmap
                cameraView.stop()
                beginFaceDetection(bitmap)
            }

            override fun onError(cameraKitError: CameraKitError) = Unit
            override fun onVideo(cameraKitVideo: CameraKitVideo) = Unit

        })

        takePicture.setOnClickListener {
            cameraView.captureImage()
        }
    }

    override fun onResume() {
        super.onResume()
        graphicOverlay.clear()
        cameraView.start()
    }

    override fun onPause() {
        graphicOverlay.clear()
        cameraView.stop()
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.new_photo -> {
                if (!cameraView.isStarted) {
                    graphicOverlay.clear()
                    cameraView.start()
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun beginFaceDetection(bitmap: Bitmap) {
        // Creamos las opciones del detector de imágenes
        val options = FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setMinFaceSize(0.15f)
                .setTrackingEnabled(true)
                .build()

        // Obtenemos la imágen
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        // Creamos el detector de caras
        val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)

        val result = detector.detectInImage(image)
                .addOnSuccessListener { showResult(it) }
                .addOnFailureListener { Toast.makeText(applicationContext, "Error en ejecutar", Toast.LENGTH_SHORT).show() }

        detector.close()
    }

    private fun showResult(list: List<FirebaseVisionFace>) {
        if (list.isEmpty()) {
            Toast.makeText(this, "No face detected", Toast.LENGTH_SHORT).show()
            return
        }

        for (face in list.withIndex()) {
            val faceGraphic = FaceGraphic(graphicOverlay)
            faceGraphic.updateFace(face.value)
            graphicOverlay.add(faceGraphic)
        }
    }
}
