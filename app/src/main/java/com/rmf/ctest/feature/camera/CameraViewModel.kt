package com.rmf.ctest.feature.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.face.Face
import com.rmf.ctest.core.domain.FaceRecognition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val faceRecognition: FaceRecognition
) : ViewModel() {

    val isFaceDetected = MutableSharedFlow<Boolean>()
    val isFrontFaceDetected = MutableSharedFlow<Boolean>()

    fun processFace(faces: List<Face>) {
        viewModelScope.launch {
            faceRecognition.process(faces)
        }
    }

    fun performClick() {
        viewModelScope.launch {
            isFaceDetected.emit(faceRecognition.isFaceDetected())
            isFrontFaceDetected.emit(faceRecognition.isFrontFaceDetected())
        }
    }
}