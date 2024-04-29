package com.rmf.ctest.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.rmf.ctest.core.data.FaceRecognitionImpl
import com.rmf.ctest.core.data.remote.ApiInterface
import com.rmf.ctest.core.data.remote.FakturApi
import com.rmf.ctest.core.data.remote.Risetai
import com.rmf.ctest.core.data.repository.FakturRepositoryImpl
import com.rmf.ctest.core.data.repository.RepositoryImpl
import com.rmf.ctest.core.data.repository.RisetaiRepositoryImpl
import com.rmf.ctest.core.domain.FaceRecognition
import com.rmf.ctest.core.domain.repository.FakturRepository
import com.rmf.ctest.core.domain.repository.Repository
import com.rmf.ctest.core.domain.repository.RisetaiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()


    @Singleton
    @Provides
    @Named("risetai")
    fun provideOkHttpClientRisetai(): OkHttpClient {
        val interceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Accesstoken", "tok_OVmE6U9emi9loFEAb12wm35SqMGmTrhSRhB0")
                .build()
            chain.proceed(request)
        }
        return OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .build()

    }

    @Singleton
    @Provides
    fun provideApi(okHttpClient: OkHttpClient): ApiInterface =
        Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create()

    @Singleton
    @Provides
    fun provideApiRisetai(@Named("risetai") okHttpClient: OkHttpClient): Risetai =
        Retrofit.Builder().baseUrl(BASE_URL_RISETAI)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create()

    @Singleton
    @Provides
    fun provideFakturApi(okHttpClient: OkHttpClient): FakturApi =
        Retrofit.Builder().baseUrl(BASE_URL_FAKTUR)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create()

    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder().create()

    @Singleton
    @Provides
    fun provideRepository(api: ApiInterface, gson: Gson): Repository = RepositoryImpl(api, gson)

    @Singleton
    @Provides
    fun provideFakturRepository(api: FakturApi): FakturRepository = FakturRepositoryImpl(api)

    @Singleton
    @Provides
    fun provideRisetaiRepository(api: Risetai, gson: Gson): RisetaiRepository =
        RisetaiRepositoryImpl(api, gson)

    @Singleton
    @Provides
    fun provideFaceRecognition(): FaceRecognition = FaceRecognitionImpl()
}

private const val BASE_URL = "http://192.168.43.228:8080/api/"
private const val BASE_URL_RISETAI = "https://fr.neoapi.id/risetai/face-api/facegallery/"
private const val BASE_URL_FAKTUR = "https://appsmsa.com/"
