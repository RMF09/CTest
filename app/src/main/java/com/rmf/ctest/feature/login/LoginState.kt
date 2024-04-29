package com.rmf.ctest.feature.login

import com.rmf.ctest.core.domain.model.Profile

data class LoginState(
    val email: String="",
    val password: String="",
    val errorMessage: String?=null,
    val isLoading: Boolean = false,
    val profile: Profile? =null
)
