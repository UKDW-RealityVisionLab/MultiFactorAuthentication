package com.mfa.api.response

import com.google.gson.annotations.SerializedName

data class HomeResponse(

	@field:SerializedName("ruang")
	val kelas: List<HomeResponseItem?>? = null
)

data class HomeResponseItem(

	@field:SerializedName("kodeMatakuliah")
	val kodeMatakuliah: String? = null,

	@field:SerializedName("dosen")
	val dosen: String? = null,

	@field:SerializedName("grup")
	val grup: String? = null,

	@field:SerializedName("kodeKelas")
	val kodeKelas: Int? = null,

	@field:SerializedName("semester")
	val semester: Int? = null,

	@field:SerializedName("matakuliah")
	val matakuliah: String? = null
)
