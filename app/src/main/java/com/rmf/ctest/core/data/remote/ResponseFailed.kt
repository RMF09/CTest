package com.rmf.ctest.core.data.remote

data class ResponseFailed<T>(
    val status: String,
    val message: String? = null,
)
