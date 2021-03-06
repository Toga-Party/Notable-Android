package me.togaparty.notable_android.data.network

import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.TimeUnit

class OkHttpClientBuilder {

    companion object {
        internal var client: OkHttpClient? = null
        val okHttpClient: OkHttpClient
            @Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
            get() {
                if (client == null) {
                    val logger = HttpLoggingInterceptor()
                    logger.level = HttpLoggingInterceptor.Level.HEADERS

                    val httpBuilder = OkHttpClient.Builder()
                            .connectTimeout(360, TimeUnit.SECONDS)
                            .readTimeout(360, TimeUnit.SECONDS)
                            .writeTimeout(360, TimeUnit.SECONDS)
                            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                    client = httpBuilder.build()
                }
                return client!!
            }
    }
}