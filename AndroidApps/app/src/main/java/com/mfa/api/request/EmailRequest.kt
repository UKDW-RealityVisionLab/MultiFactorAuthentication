package com.mfa.api.request

import com.google.gson.annotations.SerializedName

data class EmailRequest(
    @SerializedName("email") val email: String?
)
