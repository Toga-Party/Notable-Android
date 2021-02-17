package me.togaparty.notable_opencv.network

import retrofit2.Retrofit

class RetrofitWorker {
    companion object {
        fun getRetrofit() = Retrofit.Builder()
                .baseUrl(WebURL.url)
                .addConverterFactory(RetrofitBuilder.gsonConverterFactory)
                .client(RetrofitBuilder.okHttpClient)
                .build()
        }
}