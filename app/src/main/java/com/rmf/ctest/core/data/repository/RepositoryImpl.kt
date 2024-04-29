package com.rmf.ctest.core.data.repository

import android.util.Log
import com.google.gson.Gson
import com.rmf.ctest.core.data.remote.ApiInterface
import com.rmf.ctest.core.data.remote.ResponseFailed
import com.rmf.ctest.core.domain.model.Profile
import com.rmf.ctest.core.domain.model.toProfile
import com.rmf.ctest.core.domain.repository.Repository
import com.rmf.ctest.feature.login.LoginWith
import com.rmf.ctest.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    val api: ApiInterface,
    val gson: Gson
) : Repository {
    override suspend fun login(
        email: String,
        password: String,
        loginWith: LoginWith,
        name: String
    ): Flow<Resource<Profile>> {
        return flow {
            emit(Resource.Loading(true))
            val result = try {
                val response = api.login(
                    email = email,
                    password = password,
                    loginWith = loginWith.name.lowercase(),
                    name = name
                )

                when {
                    response.isSuccessful -> {
                        response.body()?.data
                    }

                    else -> {
                        val responseError =
                            gson.fromJson(
                                response.errorBody()?.string(),
                                ResponseFailed::class.java
                            )

                        emit(
                            Resource.Error(
                                responseError.message
                                    ?: "Something went wrong"
                            )
                        )
                        null
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                emit(
                    Resource.Error(
                        "Something went wrong"
                    )
                )
                null
            }
            emit(Resource.Loading(false))
            result?.let { data ->
                emit(Resource.Success(data.toProfile()))
            }
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        name: String
    ): Flow<Resource<Boolean>> {
        return flow {
            emit(Resource.Loading(true))
            val result = try {
                val response = api.register(email, password, name)

                when {
                    response.isSuccessful -> {
                        true
                    }

                    else -> {
                        val responseError =
                            gson.fromJson(
                                response.errorBody()?.string(),
                                ResponseFailed::class.java
                            )

                        emit(
                            Resource.Error(
                                responseError.message
                                    ?: "Something went wrong"
                            )
                        )
                        false
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                emit(
                    Resource.Error(
                        "Something went wrong"
                    )
                )
                false
            }
            emit(Resource.Loading(false))
            emit(Resource.Success(result))
        }

    }

    override suspend fun forgotPassword(email: String): Flow<Resource<String>> {
        return flow {
            emit(Resource.Loading(true))
            val result = try {
                val response = api.forgotPassword(email)

                Log.e("TAG", "forgotPassword: $response", )

                when {
                    response.isSuccessful -> {
                        Log.e("TAG", "forgotPassword: ${response.body()?.data?.password}", )
                        response.body()?.data?.password ?: ""
                    }

                    else -> {
                        val responseError =
                            gson.fromJson(
                                response.errorBody()?.string(),
                                ResponseFailed::class.java
                            )

                        emit(
                            Resource.Error(
                                responseError.message
                                    ?: "Something went wrong"
                            )
                        )
                        ""
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                emit(
                    Resource.Error(
                        "Something went wrong"
                    )
                )
                ""
            }
            emit(Resource.Loading(false))
            emit(Resource.Success(result))
        }

    }

    override suspend fun getProfile(email: String): Flow<Resource<Profile>> {
        return flow {
            emit(Resource.Loading(true))
            val result = try {
                val response = api.getProfile(email)

                when {
                    response.isSuccessful -> {
                        response.body()?.data
                    }

                    else -> {
                        val responseError =
                            gson.fromJson(
                                response.errorBody()?.string(),
                                ResponseFailed::class.java
                            )

                        emit(
                            Resource.Error(
                                responseError.message
                                    ?: "Something went wrong"
                            )
                        )
                        null
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                emit(
                    Resource.Error(
                        "Something went wrong"
                    )
                )
                null
            }
            emit(Resource.Loading(false))
            result?.let { data ->
                emit(Resource.Success(data.toProfile()))
            }
        }

    }
}