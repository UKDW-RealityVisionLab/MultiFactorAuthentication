package com.mfa.api.retrofit

import com.mfa.api.request.KodeJadwalRequest
import com.mfa.api.response.HomeResponseItem
import com.mfa.api.response.KodeJadwalResponse
import com.mfa.api.response.PertemuanRequest
import com.mfa.api.response.PertemuanResponse
import com.mfa.api.response.PertemuanResponseItem
import com.mfa.api.response.RuangRequest
import com.mfa.api.response.RuangResponse
import com.mfa.api.response.RuangResponseItem
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("kelas")
    suspend fun getKelas(): List<HomeResponseItem>

    @POST("jadwal/jadwalPresensi/request")
    suspend fun getDetail(@Body request: PertemuanRequest): List<PertemuanResponseItem>

    @POST("ruang/selectRuang")
    suspend fun getRuang(@Body request: RuangRequest): RuangResponseItem

    @POST("presensi/android")
    suspend fun getKodeJadwal(@Body request: KodeJadwalRequest): KodeJadwalResponse
}
