package com.rmf.ctest.feature.login

import android.graphics.Bitmap
import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmf.ctest.core.data.local.SessionManager
import com.rmf.ctest.core.domain.model.Profile
import com.rmf.ctest.core.domain.repository.Repository
import com.rmf.ctest.core.domain.repository.RisetaiRepository
import com.rmf.ctest.util.Resource
import com.rmf.ctest.util.exhaustive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: Repository,
    private val risetaiRepository: RisetaiRepository,
    val sessionManager: SessionManager
) : ViewModel() {

    var state by mutableStateOf(LoginState())
    var shouldNavigateToHome by mutableStateOf(false)


    fun onChangeEmail(value: String) {
        state = state.copy(email = value)
    }

    fun onChangePassword(value: String) {
        state = state.copy(password = value)
    }

    fun verifyFace(image: Bitmap) {

        viewModelScope.launch {
            risetaiRepository.verifyFace(state.email, image).collect { result ->
                when (result) {
                    is Resource.Error -> {
                        state = state.copy(errorMessage = result.message)
                    }

                    is Resource.Loading -> {
                        state = state.copy(isLoading = result.isLoading)
                    }

                    is Resource.Success -> {
                        val isSuccess = result.data ?: false
                        if (isSuccess)
                            login(LoginWith.FACE)
                        else {
                        }
                    }
                }.exhaustive
            }
        }
    }


    fun login(loginWith: LoginWith, profile: Profile? = null) {
        if (loginWith == LoginWith.EMAIL && !validate())
            return

        val email =
            if (loginWith == LoginWith.EMAIL || loginWith == LoginWith.FACE)
                state.email
            else profile?.email ?: ""

        viewModelScope.launch {
            repository.login(email, state.password, loginWith, profile?.name ?: "-")
                .collect { result ->
                    when (result) {
                        is Resource.Error -> {
                            state = state.copy(errorMessage = result.message)
                        }

                        is Resource.Loading -> {
                            state = state.copy(isLoading = result.isLoading)
                        }

                        is Resource.Success -> {
                            Log.e("TAG", "login: success")
                            state = state.copy(profile = result.data)
                            sessionManager.apply {
                                updateEmail(email)
                                updateLoginWith(loginWith.name)
                            }
                            shouldNavigateToHome = true
                        }
                    }.exhaustive
                }
        }
    }

    fun dismissDialog() {
        state = state.copy(errorMessage = null)
    }

    fun validate(isForFace: Boolean = false): Boolean {

        val message = if (state.email.isBlank()) {
            "Harap masukan email"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            "Harap masukan email dengan benar"
        } else if (state.password.isBlank() && !isForFace)
            "Harap masukan password"
        else null

        state = state.copy(errorMessage = message)
        return message.isNullOrEmpty()
    }


}

enum class LoginWith {
    EMAIL, GOOGLE, FACEBOOK, FACE
}