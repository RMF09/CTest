package com.rmf.ctest.feature.forgot_password

data class ForgotPasswordState(
    val email: String="",
    val password: String="",
    val errorMessage: String?=null,
    val isLoading: Boolean = false
)
