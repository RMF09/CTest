package com.rmf.ctest.feature.camera

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceAnalyzer(private val callback: FaceAnalyzerCallback) : ImageAnalysis.Analyzer {

    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
    )

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)

            faceDetector.process(inputImage)
                .addOnSuccessListener { faces ->
                    callback.processFace(faces)
                    image.close()
                }
                .addOnFailureListener {
                    Log.e("TAG", "analyze: ${it.message}", )
                    callback.errorFace(it.message.orEmpty())
                }
                .addOnCanceledListener {
                    Log.e("TAG", "analyze: canceled")
                    image.close()
                }
        }

    }
}

interface FaceAnalyzerCallback {
    fun processFace(faces: List<Face>)
    fun errorFace(error: String)
}