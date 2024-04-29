package com.rmf.ctest.feature.forgot_password

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmf.ctest.core.domain.repository.Repository
import com.rmf.ctest.util.Resource
import com.rmf.ctest.util.exhaustive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    var state by mutableStateOf(ForgotPasswordState())

    fun onChangeEmail(value: String) {
        state = state.copy(email = value)
    }

    fun send() {
        if (!validate())
            return

        viewModelScope.launch {
            repository.forgotPassword(state.email).collect { result ->
                when (result) {
                    is Resource.Error -> {
                        state = state.copy(errorMessage = result.message)
                    }

                    is Resource.Loading -> {
                        state = state.copy(isLoading = result.isLoading)
                    }

                    is Resource.Success -> {
                        state = state.copy(password = result.data ?: "")
                    }
                }.exhaustive
            }
        }
    }

    fun dismissDialog() {
        state = state.copy(errorMessage = null, password = "")
    }

    private fun validate(): Boolean {

        val message = if (state.email.isBlank()) {
            "Harap masukan email"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            "Harap masukan email dengan benar"
        } else null

        state = state.copy(errorMessage = message)
        return message.isNullOrEmpty()
    }

}