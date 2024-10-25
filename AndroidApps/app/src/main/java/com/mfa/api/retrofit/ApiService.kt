package com.mfa.api.retrofit

import com.mfa.api.request.EmailRequest
import com.mfa.api.request.KodeJadwalRequest
import com.mfa.api.request.StatusReq
import com.mfa.api.request.UpdateStatusReq
import com.mfa.api.response.HomeResponseItem
import com.mfa.api.response.KodeJadwalResponse
import com.mfa.api.response.PertemuanRequest
import com.mfa.api.response.PertemuanResponse
import com.mfa.api.response.PertemuanResponseItem
import com.mfa.api.response.ProfileResponse
import com.mfa.api.response.RuangRequest
import com.mfa.api.response.RuangResponse
import com.mfa.api.response.RuangResponseItem
import com.mfa.api.response.cekStatus
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

    @POST("daftarpresensi/getProfile/req")
    suspend fun getProfile(@Body request: EmailRequest?) :ProfileResponse

    @POST("presensi/kodeJadwal")
    suspend fun getKodeJadwal(@Body request: KodeJadwalRequest): KodeJadwalResponse

    @POST("daftarpresensi/cekStatusPresensi")
    suspend fun cekStatusKehadiran(@Body req :StatusReq) :Boolean

    @POST("face/faceVerify")
    suspend fun updateStatusKehadiran(@Body req: UpdateStatusReq):Boolean
}
