package com.rmf.ctest.feature.register

data class RegisterState(
    val email: String="",
    val password: String="",
    val name: String="",
    val errorMessage: String?=null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)
