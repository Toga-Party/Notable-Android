package me.togaparty.notable_android.data.network

import me.togaparty.notable_android.utils.Constants
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface RetrofitService {

    @Multipart
    @POST(Constants.HEADER)
    fun upload(
            @Part image: MultipartBody.Part,
            @Part("file_name") name: RequestBody
    ) : Call<ResponseBody>
}