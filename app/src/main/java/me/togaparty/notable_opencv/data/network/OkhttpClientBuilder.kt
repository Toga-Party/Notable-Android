package me.togaparty.notable_opencv.data.network

import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.TimeUnit

class OkhttpClientBuilder {

    companion object {
        internal var client: OkHttpClient? = null
        //private var gson: GsonConverterFactory? = null

        val okHttpClient: OkHttpClient
            @Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
            get() {
                if (client == null) {
                    val logger = HttpLoggingInterceptor()
                    logger.level = HttpLoggingInterceptor.Level.HEADERS

                    val httpBuilder = OkHttpClient.Builder()
                            .connectTimeout(270, TimeUnit.SECONDS)
                            .readTimeout(270, TimeUnit.SECONDS)
                            .writeTimeout(270, TimeUnit.SECONDS)
//                            .followRedirects(false)
//                            .followSslRedirects(false)
//                            .retryOnConnectionFailure(false)
                            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                            //.addInterceptor(logger)  /// show all JSON in logCat
                    client = httpBuilder.build()

                }
                return client!!
            }
//        val gsonConverterFactory: GsonConverterFactory
//            get() {
//                if (this.gson == null) {
//                    this.gson = GsonConverterFactory.create(GsonBuilder()
//                            .setLenient()
//                            .disableHtmlEscaping()
//                            .create())
//                }
//                return this.gson!!
//            }
    }
}