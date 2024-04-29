package com.rmf.ctest.core.domain.repository

import android.graphics.Bitmap
import com.rmf.ctest.core.domain.model.Profile
import com.rmf.ctest.feature.login.LoginWith
import com.rmf.ctest.util.Resource
import kotlinx.coroutines.flow.Flow

interface RisetaiRepository {

    suspend fun enrollFace(userId: String, username: String, image: Bitmap): Flow<Resource<Boolean>>
    suspend fun deleteFace(userId: String): Flow<Resource<Boolean>>
    suspend fun verifyFace(userId: String, image: Bitmap): Flow<Resource<Boolean>>
}