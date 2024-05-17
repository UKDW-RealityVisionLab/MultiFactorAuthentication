package com.mfa.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.mfa.Helper
import com.mfa.api.response.RuangResponse
import com.mfa.api.retrofit.ApiService
import kotlinx.coroutines.Dispatchers

class MfaRepository private constructor(
    private val apiService: ApiService
) {

    suspend fun getJadwal() = liveData {
        try {
            val response = apiService.getJadwal()
            emit(Helper.Success(response))
        } catch (e: Exception) {
            Log.d("MfA repository", "Permintaan memuat data gagal",   e)
        }
    }

    suspend fun getPertemuan(kode_jadwal: String) = liveData {
        try {
            val response = apiService.getDetail(kode_jadwal)
            emit(Helper.Success(response))
        } catch (e: Exception) {
            Log.d("Mfa repository", "$kode_jadwal masih eror")
        }
    }

    fun getByKodeRuang(kodeRuang: String): LiveData<Helper<RuangResponse>> = liveData(Dispatchers.IO) {
        try {
            val response = apiService.getRuang(kodeRuang)
            emit(Helper.Success(response))
        } catch (e: Exception) {
//            emit(Helper.Error(e))
            Log.e("MfaRepository", "Failed to fetch room data: ${e.message}", e)
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