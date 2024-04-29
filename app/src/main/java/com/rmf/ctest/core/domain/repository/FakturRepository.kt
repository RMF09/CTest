package com.rmf.ctest.core.domain.repository

import android.graphics.Bitmap
import com.rmf.ctest.core.data.dto.FakturDto
import com.rmf.ctest.core.domain.model.Profile
import com.rmf.ctest.feature.login.LoginWith
import com.rmf.ctest.util.Resource
import kotlinx.coroutines.flow.Flow

interface FakturRepository {

    suspend fun get(): Flow<Resource<List<FakturDto>>>
}