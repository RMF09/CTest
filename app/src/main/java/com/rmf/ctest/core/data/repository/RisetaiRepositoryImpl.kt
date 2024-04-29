package com.rmf.ctest.core.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.google.gson.Gson
import com.rmf.ctest.core.data.dto.EnrollDto
import com.rmf.ctest.core.data.remote.ResponseRisetai
import com.rmf.ctest.core.data.remote.Risetai
import com.rmf.ctest.core.domain.repository.RisetaiRepository
import com.rmf.ctest.util.Resource
import com.rmf.ctest.util.toBase64
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RisetaiRepositoryImpl @Inject constructor(
    private val api: Risetai,
    private val gson: Gson
) : RisetaiRepository {
    override suspend fun enrollFace(
        userId: String,
        username: String,
        image: Bitmap
    ): Flow<Resource<Boolean>> {
        return flow {
            emit(Resource.Loading(true))

            val base64 = image.toBase64()
            val body = EnrollDto(
                user_id = userId,
                user_name = username,
                image = base64
            )
            print(base64)
            Log.e("TAG", "enrollFace: $body")
            val result = try {
                val response = api.enrollFace(body)
                Log.e("TAG", "enrollFace: $response")
                when {
                    response.isSuccessful -> {
                        if (response.body()!!.risetai.status == "200") {
                            true
                        } else {
                            if (response.body()!!.risetai.status_message == "Database Error - UserID Already Exists") {
                                val responseDelete = api.deleteFace(body)
                                Log.e("TAG", "delete face: $responseDelete" )
                                if (responseDelete.isSuccessful) {
                                    val responseEnrollAgain = api.enrollFace(body)
                                    if (responseEnrollAgain.isSuccessful) {
                                        true
                                    } else {
                                        emit(Resource.Error(responseEnrollAgain.body()!!.risetai.status_message))
                                        false
                                    }

                                } else {
                                    emit(Resource.Error(responseDelete.body()!!.risetai.status_message))
                                    false
                                }
                            } else {
                                emit(Resource.Error(response.body()!!.risetai.status_message))
                                false
                            }

                        }
                    }

                    else -> {
                        val responseError =
                            gson.fromJson(
                                response.errorBody()?.string(),
                                ResponseRisetai::class.java
                            )
                        emit(
                            Resource.Error(
                                responseError.risetai.status_message
                            )
                        )
                        false
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.Error("Something went wrong"))
                false
            }
            emit(Resource.Loading(false))
            emit(Resource.Success(result))
        }
    }

    override suspend fun deleteFace(userId: String): Flow<Resource<Boolean>> {
        TODO("Not yet implemented")
    }

    override suspend fun verifyFace(userId: String, image: Bitmap): Flow<Resource<Boolean>> {
        return flow {
            emit(Resource.Loading(true))

            val base64 = image.toBase64()
            val body = EnrollDto(
                user_id = userId,
                user_name = "",
                image = base64
            )
            print(base64)
            Log.e("TAG", "verify: $body")
            val result = try {
                val response = api.verifyFace(body)
                Log.e("TAG", "verify: $response")
                when {
                    response.isSuccessful -> {
                        if (response.body()!!.risetai.status != "200") {
                            emit(Resource.Error(response.body()!!.risetai.status_message))
                        }
                        response.body()!!.risetai.verified
                    }

                    else -> {
                        val responseError =
                            gson.fromJson(
                                response.errorBody()?.string(),
                                ResponseRisetai::class.java
                            )
                        emit(
                            Resource.Error(
                                responseError.risetai.status_message
                            )
                        )
                        false
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.Error("Something went wrong"))
                false
            }
            emit(Resource.Loading(false))
            emit(Resource.Success(result))
        }
    }
}