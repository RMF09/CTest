package com.rmf.ctest.core.data.remote

data class ResponseData<T>(
    val status: String? = null,
    val message: String? = null,
    val data: T? = null
)
