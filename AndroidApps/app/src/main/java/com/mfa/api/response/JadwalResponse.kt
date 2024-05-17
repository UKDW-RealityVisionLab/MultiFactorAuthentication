package com.mfa.api.response

import com.google.gson.annotations.SerializedName

data class JadwalResponse(

	@field:SerializedName("jadwal")
	val jadwal: Jadwal? = null
)

data class Jadwal(

	@field:SerializedName("dataJadwal")
	val dataJadwal: List<DataJadwalItem?>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class DataJadwalItem(

	@field:SerializedName("sesi_start")
	val sesiStart: String? = null,

	@field:SerializedName("group_kelas")
	val groupKelas: String? = null,

	@field:SerializedName("kode_kelas")
	val kodeKelas: Int? = null,

	@field:SerializedName("kode_jadwal")
	val kodeJadwal: String? = null,

	@field:SerializedName("kode_sesi")
	val kodeSesi: String? = null,

	@field:SerializedName("sesi_end")
	val sesiEnd: String? = null,

	@field:SerializedName("tanggal")
	val tanggal: String? = null,

	@field:SerializedName("nama_matakuliah")
	val namaMatakuliah: String? = null,

	@field:SerializedName("kode_ruang")
	val kodeRuang: String? = null
) {
}
