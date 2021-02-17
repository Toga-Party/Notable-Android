package me.togaparty.notable_opencv.network

import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class RetrofitBuilder {

    companion object {
        private val logger: HttpLoggingInterceptor =
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        private val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logger)
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .build()
        fun getRetrofit(): Retrofit = Retrofit.Builder()
            .baseUrl(WebURL.url)
            .client(okHttpClient)
            //.addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
    }
}