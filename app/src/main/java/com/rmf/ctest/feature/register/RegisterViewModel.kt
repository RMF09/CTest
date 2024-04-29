package com.rmf.ctest.feature.register

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
import com.rmf.ctest.feature.login.LoginWith
import com.rmf.ctest.util.Resource
import com.rmf.ctest.util.exhaustive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: Repository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var state by mutableStateOf(RegisterState())
    var shouldNavigateToHome by mutableStateOf(false)


    fun onChangeName(value: String) {
        state = state.copy(name = value)
    }

    fun onChangeEmail(value: String) {
        state = state.copy(email = value)
    }

    fun onChangePassword(value: String) {
        state = state.copy(password = value)
    }

    fun login(loginWith: LoginWith, profile: Profile? = null) {
        if (loginWith == LoginWith.EMAIL && !validate())
            return

        val email =
            if (loginWith == LoginWith.EMAIL )
                state.email
            else profile?.email ?: ""

        viewModelScope.launch {
            repository.login(profile?.email ?: "", "", loginWith, profile?.name ?: "-")
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

    fun register() {
        if (!validate())
            return
        viewModelScope.launch {
            repository.register(
                email = state.email,
                password = state.password,
                name = state.name
            ).collect { result ->
                when (result) {
                    is Resource.Error -> {
                        state = state.copy(errorMessage = result.message)
                    }

                    is Resource.Loading -> {
                        state = state.copy(isLoading = result.isLoading)
                    }

                    is Resource.Success -> {
                        state = state.copy(isSuccess = result.data ?: false)
                    }
                }.exhaustive
            }
        }
    }

    fun dismissDialog(){
        state = state.copy(errorMessage = null, isSuccess = false)
    }

    private fun validate(): Boolean {
        val message =
            if (state.name.isBlank()) {
                "Harap masukan nama"
            } else if (state.email.isBlank()) {
                "Harap masukan email"
            } else if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
                "Harap masukan email dengan benar"
            } else if (state.password.isBlank())
                "Harap masukan password"
            else null

        state = state.copy(errorMessage = message)
        return message.isNullOrEmpty()
    }

}