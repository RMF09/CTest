package com.rmf.ctest.feature.home

import com.rmf.ctest.core.data.dto.FakturDto

data class HomeState(
    val email: String= "",
    val isLoading: Boolean = false,
    val isLoadingFaktur: Boolean = false,
    val isSuccessToEnrollFace: Boolean = false,
    val errorMessage: String? = null,
    val errorMessageFaktur: String? = null,
    val list: List<FakturDto> = emptyList()
)
