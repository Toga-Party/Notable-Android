package me.togaparty.notable_opencv.data.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface RetrofitService {

    @Multipart
    @POST(WebURL.header)
    fun upload(
            @Part image: MultipartBody.Part,
            @Part("file_name") name: RequestBody
    ) : Call<ResponseBody>
}