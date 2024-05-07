package com.mfa.repository

import android.util.Log
import androidx.lifecycle.liveData
import com.mfa.Helper
import com.mfa.api.retrofit.ApiService

class MfaRepository private constructor(
    private val apiService: ApiService
) {

    suspend fun getJadwal() = liveData {
        try {
            val response = apiService.getJadwal()
            emit(Helper.Success(response))
        } catch (e: Exception) {
            Log.d("MfA repository", "Permintaan memuat data gagal", e)
        }
    }

//    suspend fun getDetailUser(token: String, id: String) = liveData {
//        try {
//            val response = apiService.getDetail("Bearer $token", id)
//            emit(Helper.Success(response))
//        } catch (e: Exception) {
//            Log.d("User repository", "Permintaan memuat data gagal", e)
//            Log.d("User repository", "$token dan $id masih eror")
//        }
//    }


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