package com.rmf.ctest.core.data.remote

import com.rmf.ctest.core.data.dto.EnrollDto
import com.rmf.ctest.core.data.dto.ForgotPasswordDto
import com.rmf.ctest.core.data.dto.ProfileDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInterface {

    @POST("login")
    @FormUrlEncoded
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("login_with") loginWith: String,
        @Field("name") name: String,
    ): Response<ResponseData<ProfileDto>>

    @POST("register")
    @FormUrlEncoded
    suspend fun register(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("name") name: String
    ): Response<ResponseData<ProfileDto>>

    @POST("forgot-password")
    @FormUrlEncoded
    suspend fun forgotPassword(
        @Field("email") email: String,
    ): Response<ResponseData<ForgotPasswordDto>>

    @GET("profile")
    suspend fun getProfile(
        @Field("email") email: String,
    ): Response<ResponseData<ProfileDto>>




}