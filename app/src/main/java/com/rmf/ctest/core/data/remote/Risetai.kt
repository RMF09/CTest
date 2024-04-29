package com.rmf.ctest.core.data.remote

import com.rmf.ctest.core.data.dto.EnrollDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST

interface Risetai {
    @POST("enroll-face")
    suspend fun enrollFace(
        @Body body: EnrollDto
    ): Response<ResponseRisetai>


    @HTTP(method = "DELETE", path = "delete-face", hasBody = true)
    suspend fun deleteFace(
        @Body body: EnrollDto
    ): Response<ResponseRisetai>

    @POST("verify-face")
    suspend fun verifyFace(
        @Body body: EnrollDto
    ): Response<ResponseRisetai>
}