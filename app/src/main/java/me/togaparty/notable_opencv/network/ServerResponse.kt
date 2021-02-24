package me.togaparty.notable_opencv.network

import com.google.gson.annotations.SerializedName

data class ServerResponse(
    @SerializedName("success") var status: Int = 0,
    @SerializedName("error") val error: String = "",
    @SerializedName("error_type") val errorType: String = ""
)