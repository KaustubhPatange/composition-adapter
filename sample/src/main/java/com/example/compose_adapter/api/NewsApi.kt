package com.example.compose_adapter.api

import com.example.compose_adapter.BuildConfig
import com.example.compose_adapter.data.model.NewsResponse
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface NewsApi {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String,
        @Query("category") category: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ) : NewsResponse

    companion object {
        private var INSTANCE: NewsApi? = null
        fun get(): NewsApi {
            var instance = INSTANCE
            if (instance == null) {
                instance = Retrofit.Builder().client(OkHttpClient.Builder().addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .url(
                            chain.request().url.newBuilder()
                                .addQueryParameter("apiKey", BuildConfig.NEWS_API_KEY).build()
                        )
                        .cacheControl(CacheControl.Builder().maxAge(3, TimeUnit.HOURS).build())
                        .build()
                    chain.proceed(request)
                }.build())
                    .addConverterFactory(MoshiConverterFactory.create())
                    .baseUrl("https://newsapi.org/v2/").build().create(NewsApi::class.java)
                INSTANCE = instance
            }
            return instance!!
        }
    }
}