package com.mfa.api.retrofit

import com.mfa.api.response.JadwalResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {

    @GET("jadwal")
    suspend fun getJadwal(): JadwalResponse

}