package com.rmf.ctest.core.data.remote

import retrofit2.Response
import retrofit2.http.GET

interface FakturApi {

    @GET("retrieve/faktur/1/2024-01-01/2024-01-30")
    suspend fun get(
    ): Response<ResponseFaktur>
}