package com.rmf.ctest.core.domain

import android.graphics.Rect
import com.google.mlkit.vision.face.Face

interface FaceRecognition {

    fun process(faces: List<Face>)
    fun isFaceDetected(): Boolean
    fun isFrontFaceDetected(): Boolean
}