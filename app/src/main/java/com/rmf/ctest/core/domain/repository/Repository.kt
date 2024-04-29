package com.rmf.ctest.core.domain.repository

import com.rmf.ctest.core.domain.model.Profile
import com.rmf.ctest.feature.login.LoginWith
import com.rmf.ctest.util.Resource
import kotlinx.coroutines.flow.Flow

interface Repository {
    suspend fun login(
        email: String,
        password: String,
        loginWith: LoginWith,
        name: String
    ): Flow<Resource<Profile>>

    suspend fun register(email: String, password: String, name: String): Flow<Resource<Boolean>>
    suspend fun forgotPassword(email: String): Flow<Resource<String>>
    suspend fun getProfile(email: String): Flow<Resource<Profile>>
}