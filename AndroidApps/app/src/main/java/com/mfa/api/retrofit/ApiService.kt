package com.mfa.api.retrofit

import com.mfa.api.response.JadwalResponse
import com.mfa.api.response.PertemuanResponse
import com.mfa.api.response.RuangResponse
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("jadwal")
    suspend fun getJadwal(): JadwalResponse

    @GET("jadwal/{kode_jadwal}")
    suspend fun getDetail(
        @Path("kode_jadwal") id: String,
    ): PertemuanResponse

    @GET("ruang/{kode_ruang}")
    suspend fun getRuang(
        @Path("kode_ruang") kodeRuang: String
    ): RuangResponse

    @POST("ruang")
    suspend fun validationLocation(
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String
    ): RuangResponse
}
