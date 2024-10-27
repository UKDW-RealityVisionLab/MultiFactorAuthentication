package com.mfa.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.mfa.Helper
import com.mfa.api.request.EmailRequest
import com.mfa.api.request.StatusReq
import com.mfa.api.request.UpdateStatusReq
import com.mfa.api.response.PertemuanRequest
import com.mfa.api.response.ProfileResponse
import com.mfa.api.response.RuangRequest
import com.mfa.api.response.RuangResponseItem
import com.mfa.api.retrofit.ApiService
//import com.mfa.api.retrofit.RetrofitInstance.api
import kotlinx.coroutines.Dispatchers


class MfaRepository private constructor(
    private val apiService: ApiService
) {

    suspend fun getKelas() = liveData {
        try {
            val response = apiService.getKelas()
            emit(Helper.Success(response))
        } catch (e: Exception) {
            Log.d("MfaRepository", "Permintaan memuat data gagal", e)
        }
    }

    fun getPertemuan(kodeKelas: Int) = liveData(Dispatchers.IO) {
        try {
            val request = PertemuanRequest(kodeKelas)
            val response = apiService.getDetail(request)
            emit(Helper.Success(response))
        } catch (e: Exception) {
            Log.d("MfaRepository", "$kodeKelas masih eror", e)
        }
    }


    fun getRuangByKodeJadwal(idJadwal: String): LiveData<Helper<RuangResponseItem>> = liveData(Dispatchers.IO) {
        try {
            val requestRuang = RuangRequest(idJadwal)
            val response = apiService.getRuang(requestRuang)
            emit(Helper.Success(response))
            Log.d("get ruang by kode jadwal", "$idJadwal")
        } catch (e: Exception) {
            Log.e("MfaRepository", "Failed to fetch room data: ${e.message}", e)
        }
    }

    suspend fun getProfile(email:EmailRequest) = liveData{
        try {
             val response= apiService.getProfile(email)
            Log.d("profile", "$response")
            emit(response)
        }
        catch (e:Exception){
            Log.d("MfaRepository", "Permintaan email gagal $email", e)
        }
    }

    suspend fun cekStatusUser(req: StatusReq) = liveData{
        try {
            val response= apiService.cekStatusKehadiran(req)
            Log.d("status", "$response")
            emit(response)
        }
        catch (e:Exception){
            Log.d("MfaRepository status", "Permintaan di cek status gagal $req", e)
        }
    }

    suspend fun updateStatus(req: UpdateStatusReq) = liveData{
        try {
            val response= apiService.updateStatusKehadiran(req)
            Log.d("repo: status update", "$response")
            emit(response)
        }
        catch (e:Exception){
            Log.d("MfaRepository status", "Permintaan email gagal $req", e)
        }
    }



    companion object {
        @Volatile
        private var instance: MfaRepository? = null
        fun getInstance(
            apiService: ApiService
        ): MfaRepository =
            instance ?: synchronized(this) {
                instance ?: MfaRepository(apiService)
            }.also { instance = it }
    }
}