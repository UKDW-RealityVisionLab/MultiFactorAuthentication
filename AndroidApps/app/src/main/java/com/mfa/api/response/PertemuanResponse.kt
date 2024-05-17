package com.mfa.api.response

import com.google.gson.annotations.SerializedName

data class PertemuanResponse(

	@field:SerializedName("jadwal")
	val jadwal: JadwalPertemuan? = null
)

data class DataJadwalItemPertemuan(

	@field:SerializedName("kode_kelas")
	val kodeKelas: String? = null,

	@field:SerializedName("kode_jadwal")
	val kodeJadwal: String? = null,

	@field:SerializedName("kode_sesi")
	val kodeSesi: String? = null,

	@field:SerializedName("tanggal")
	val tanggal: String? = null,

	@field:SerializedName("kode_ruang")
	val kodeRuang: String? = null
)

data class JadwalPertemuan(

	@field:SerializedName("dataJadwal")
	val dataJadwal: List<DataJadwalItemPertemuan?>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)
