package com.marcmeru.triviagame.data

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("response_code")
    val response_code: Int,

    @SerializedName("response_message")
    val response_message: String,

    @SerializedName("token")
    val token: String
)
