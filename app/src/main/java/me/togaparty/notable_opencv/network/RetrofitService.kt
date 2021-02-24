package me.togaparty.notable_opencv.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface RetrofitService {
    //@Headers("Content-Type: application/json")
    @Multipart
    @POST(WebURL.header)
    fun upload(
        @Part image: MultipartBody.Part,
        @Part("file_name") name: RequestBody
    ) : Call<ServerResponse>
}