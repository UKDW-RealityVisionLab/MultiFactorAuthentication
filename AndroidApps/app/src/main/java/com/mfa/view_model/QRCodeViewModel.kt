package com.mfa.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mfa.api.request.KodeJadwalRequest
import com.mfa.api.response.PertemuanRequest
import com.mfa.api.response.PertemuanResponseItem
import com.mfa.api.retrofit.ApiService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QRCodeViewModel(application: Application) : AndroidViewModel(application) {

    val kodeJadwalResponse = MutableLiveData<Result<Boolean>>()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.1.5:3000/") // Replace with your actual base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    fun checkKodeJadwal(kodeJadwalRequest: KodeJadwalRequest) {
        viewModelScope.launch {
            try {
                // Adjusting to check against the entire pertemuan list
                val pertemuanList = apiService.getDetail(PertemuanRequest(kodeKelas = kodeJadwalRequest.kodeJadwal.toInt()))
                val matched = pertemuanList.any {
                    it.jadwal == kodeJadwalRequest.kodeJadwal && it.kodeKelas.toString() == kodeJadwalRequest.qrCodeData
                }
                kodeJadwalResponse.postValue(Result.success(matched))
            } catch (e: Exception) {
                kodeJadwalResponse.postValue(Result.failure(e))
                Toast.makeText(getApplication(), "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
