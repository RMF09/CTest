package com.rmf.ctest.core.data.remote

data class ResponseRisetai(
    val risetai: ReponseDataRisetai
)

data class ReponseDataRisetai(
    val status: String,
    val status_message: String,
    val similarity: Float? =null,
    val verified: Boolean? =null,
)
