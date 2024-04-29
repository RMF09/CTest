package com.rmf.ctest.core.data.repository

import android.graphics.Bitmap
import com.rmf.ctest.core.data.dto.FakturDto
import com.rmf.ctest.core.data.remote.FakturApi
import com.rmf.ctest.core.domain.repository.FakturRepository
import com.rmf.ctest.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakturRepositoryImpl @Inject constructor(
    private val api: FakturApi
) : FakturRepository {
    override suspend fun get(
    ): Flow<Resource<List<FakturDto>>> {
        return flow {
            emit(Resource.Loading(true))

            val result = try {
                val response = api.get()
                when {
                    response.isSuccessful -> {
                        response.body()?.posts ?: emptyList()
                    }

                    else -> {
                        emit(Resource.Error("Something went wrong"))

                        null
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.Error("Something went wrong"))
                null
            }
            emit(Resource.Loading(false))
            emit(Resource.Success(result))
        }
    }
}