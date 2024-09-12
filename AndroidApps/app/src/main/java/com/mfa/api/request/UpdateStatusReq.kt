package com.mfa.api.request

import com.google.gson.annotations.SerializedName

data class UpdateStatusReq(
    @SerializedName("idJadwal") var idJadwal:String?,
    @SerializedName("nim") var nim:String?
)
