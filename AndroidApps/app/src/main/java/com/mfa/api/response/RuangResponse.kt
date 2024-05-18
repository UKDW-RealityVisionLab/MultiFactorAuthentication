package com.mfa.api.response

import com.google.gson.annotations.SerializedName

data class RuangResponse(

	@field:SerializedName("ruang")
	val ruang: Ruang? = null
)

data class DataRuangItem(

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("latitude")
	val latitude: Any? = null,

	@field:SerializedName("kodeRuang")
	val kodeRuang: String? = null,

	@field:SerializedName("longitude")
	val longitude: Any? = null
)

data class Ruang(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("dataRuang")
	val dataRuang: List<DataRuangItem?>? = null,

	@field:SerializedName("status")
	val status: Int? = null
)
