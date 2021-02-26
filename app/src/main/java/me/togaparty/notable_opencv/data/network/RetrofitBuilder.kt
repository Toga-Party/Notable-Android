package me.togaparty.notable_opencv.data.network

import retrofit2.Retrofit

class RetrofitBuilder {
    companion object {
        internal var retrofitInstance : Retrofit? = null
        init {
            retrofitInstance = Retrofit.Builder()
                    .baseUrl(WebURL.url)
                    //.addConverterFactory(OkhttpClientBuilder.gsonConverterFactory)
                    .client(OkhttpClientBuilder.okHttpClient)
                    .build()
        }

        fun getRetrofit(): Retrofit = retrofitInstance!!
    }
}