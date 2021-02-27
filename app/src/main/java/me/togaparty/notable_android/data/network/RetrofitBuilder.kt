package me.togaparty.notable_android.data.network

import me.togaparty.notable_android.utils.Constants
import retrofit2.Retrofit

class RetrofitBuilder {
    companion object {
        private var retrofitInstance : Retrofit? = null
        init {
            retrofitInstance = Retrofit.Builder()
                    .baseUrl(Constants.url)
                    //.addConverterFactory(OkhttpClientBuilder.gsonConverterFactory)
                    .client(OkhttpClientBuilder.okHttpClient)
                    .build()
        }

        fun getRetrofit(): Retrofit = retrofitInstance!!
    }
}