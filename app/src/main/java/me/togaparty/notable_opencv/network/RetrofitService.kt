package me.togaparty.notable_opencv.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface RetrofitService {
    @Headers("Content-Type: multipart/form-data")
    @Multipart
    @POST("/predict")
    fun upload(
        @Part image: MultipartBody.Part
    ) : Call<ResponseBody>
}