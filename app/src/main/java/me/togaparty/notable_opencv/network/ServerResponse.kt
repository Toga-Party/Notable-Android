package me.togaparty.notable_opencv.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ServerResponse(
    @Expose
    @SerializedName("success") var status: Boolean = false,
    @SerializedName("error") val error: String = "",
    @SerializedName("error_type") val errorType: String = "",
    @Expose(deserialize = false) // deserialize is this filed is not required
    @SerializedName("message") val message: String = ""
)