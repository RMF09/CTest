package com.rmf.ctest.feature.home

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.rmf.ctest.core.data.local.SessionManager
import com.rmf.ctest.core.domain.repository.FakturRepository
import com.rmf.ctest.core.domain.repository.RisetaiRepository
import com.rmf.ctest.util.Resource
import com.rmf.ctest.util.exhaustive
import com.rmf.ctest.util.toBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val repository: RisetaiRepository,
    private val fakturRepository: FakturRepository
) : ViewModel() {

    var state by mutableStateOf(HomeState())

    init {
        state = state.copy(email = sessionManager.getEmail() ?: "")
        getFaktur()
    }

    fun enrollFace(image: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = sessionManager.getEmail() ?: ""
            val username = sessionManager.getEmail() ?: ""
            repository.enrollFace(userId = userId, username = username, image = image)
                .collect { result ->
                    when (result) {
                        is Resource.Error -> {
                            state = state.copy(errorMessage = result.message)
                        }

                        is Resource.Loading -> {
                            state = state.copy(isLoading = result.isLoading)
                        }

                        is Resource.Success -> {
                            state = state.copy(isSuccessToEnrollFace = result.data ?: false)
                        }
                    }.exhaustive
                }
        }
    }

    fun getFaktur() {
        viewModelScope.launch {
            fakturRepository.get().collect { result ->
                when (result) {
                    is Resource.Error -> {
                        state = state.copy(errorMessageFaktur = result.message)
                    }

                    is Resource.Loading -> {
                        state = state.copy(isLoadingFaktur = result.isLoading)
                    }

                    is Resource.Success -> {
                        state = state.copy(list = result.data ?: emptyList())
                    }
                }.exhaustive
            }
        }
    }

    fun logout() {
        sessionManager.clear()
        Firebase.auth.signOut()
    }

    fun onDismissDialog() {
        state = state.copy(isSuccessToEnrollFace = false, errorMessage = null)
    }

    fun retryFaktur() {
        state = state.copy(errorMessageFaktur = null, list = emptyList())
        getFaktur()
    }
}