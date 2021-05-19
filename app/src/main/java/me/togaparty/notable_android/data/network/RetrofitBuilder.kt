package me.togaparty.notable_android.data.network

import me.togaparty.notable_android.utils.Constants
import retrofit2.Retrofit

class RetrofitBuilder {
    companion object {
        val retrofitInstance: RetrofitService by lazy {
            val retrofit = Retrofit.Builder()
                .baseUrl(Constants.URL)
                .client(OkHttpClientBuilder.okHttpClient)
                .build()
            retrofit.create(RetrofitService::class.java)
        }
    }
}