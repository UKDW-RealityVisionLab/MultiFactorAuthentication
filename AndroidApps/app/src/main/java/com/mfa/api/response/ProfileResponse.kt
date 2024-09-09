package com.mfa.api.response

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("nama")
    val nama: String? = null,

    @field:SerializedName("nim")
    val nim: String? = null,

)
