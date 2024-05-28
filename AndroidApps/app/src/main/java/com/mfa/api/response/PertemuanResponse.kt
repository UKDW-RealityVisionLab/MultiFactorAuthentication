package com.mfa.api.response

import com.google.gson.annotations.SerializedName

data class PertemuanResponse(

	@field:SerializedName("jadwal")
	val jadwal: List<PertemuanResponseItem?>? = null
)

data class PertemuanRequest(
	@SerializedName("kodeKelas") val kodeKelas: Int
)

data class PertemuanResponseItem(

	@field:SerializedName("jadwal")
	val jadwal: String? = null,

	@field:SerializedName("grup")
	val grup: String? = null,

	@field:SerializedName("sesiEnd")
	val sesiEnd: String? = null,

	@field:SerializedName("mataKuliah")
	val mataKuliah: String? = null,

	@field:SerializedName("kodeKelas")
	val kodeKelas: Int? = null,

	@field:SerializedName("ruang")
	val ruang: String? = null,

	@field:SerializedName("tanggal")
	val tanggal: String? = null,

	@field:SerializedName("sesi")
	val sesi: String? = null,

	@field:SerializedName("sesiStart")
	val sesiStart: String? = null
)










//package com.mfa.api.response
//
//import com.google.gson.annotations.SerializedName
//
//data class RuangResponse(
//
//	@field:SerializedName("ruang")
//	val ruang: List<RuangResponseItem?>? = null
//)
//
//data class RuangRequest(
//	@SerializedName("jadwal") val idJadwal: String
//)
//
//data class RuangResponseItem(
//
//	@field:SerializedName("nama")
//	val nama: String? = null,
//
//	@field:SerializedName("latitude")
//	val latitude: Any? = null,
//
//	@field:SerializedName("kodeRuang")
//	val kodeRuang: String? = null,
//
//	@field:SerializedName("longitude")
//	val longitude: Any? = null
//)
