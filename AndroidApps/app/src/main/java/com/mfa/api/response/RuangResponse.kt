package com.mfa.api.response

import com.google.gson.annotations.SerializedName

data class RuangResponse(

	@field:SerializedName("RuangResponse")
	val ruangResponse: List<RuangResponseItem>? = null
)

data class RuangRequest(
	@SerializedName("idJadwal") val idJadwal: String
)

data class RuangResponseItem(

	@field:SerializedName("latitude")
	val latitude: Double? = null,

	@field:SerializedName("longtitude")
	val longitude: Double? = null
)

