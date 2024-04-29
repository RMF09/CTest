package com.rmf.ctest.core.data

import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import com.rmf.ctest.core.domain.FaceRecognition
import javax.inject.Inject

class FaceRecognitionImpl @Inject constructor() : FaceRecognition {

    private var isFaceDetected: Boolean = false
    private var isFrontFaceDetected: Boolean = false

    override fun process(faces: List<Face>) {
        isFaceDetected = false
        var result: Rect?
        if (faces.isNotEmpty()) {
            faces.forEach { face ->
                val leftEyeOpenProbability = face.leftEyeOpenProbability
                val rightEyeOpenProbability = face.rightEyeOpenProbability
                val smilingProbability = face.smilingProbability

                val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)
                val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)
                val leftCheek = face.getLandmark(FaceLandmark.LEFT_CHEEK)
                val rightCheek = face.getLandmark(FaceLandmark.RIGHT_CHEEK)

                Log.e("TAG", "process: front : $leftEye, $rightEye, $leftCheek, $rightCheek ")
                isFrontFaceDetected =
                    (leftEyeOpenProbability ?: 0F) > 0.9F && (rightEyeOpenProbability ?: 0F) > 0.9F


                result = face.boundingBox
                isFaceDetected = true
                Log.e("TAG", "process result: $result")
                Log.e(
                    "TAG",
                    "processFace: H ${result!!.height()}, W ${result!!.width()}, L ${result!!.left},T ${result!!.top},R ${result!!.right}, B ${result!!.bottom}"
                )

                if ((smilingProbability ?: 0f) > 0.3f) {
                    Log.e("TAG", "processFace: Senyum")
                } else {
                    Log.e("TAG", "processFace: Jutek, tersenyum atuh")
                }

                if ((leftEyeOpenProbability ?: 0F) > 0.9F && (rightEyeOpenProbability ?: 0F) > 0.9F
                ) {
                    Log.e("TAG", "processFace: Benta")

                }
                if (((leftEyeOpenProbability
                        ?: 0F) < 0.4 && (leftEyeOpenProbability != 0f)) && ((rightEyeOpenProbability
                        ?: 0F) < 0.4F && (leftEyeOpenProbability != 0f))
                ) {
                    Log.e("TAG", "processFace: Kedipan mata")
                }
            }
        }
    }

    override fun isFaceDetected(): Boolean {
        return isFaceDetected
    }

    override fun isFrontFaceDetected(): Boolean {
        return isFrontFaceDetected
    }

}