package com.mfa.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mfa.api.request.KodeJadwalRequest
import com.mfa.api.response.PertemuanRequest
import com.mfa.api.retrofit.ApiService
import com.mfa.api.retrofit.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QRCodeViewModel(application: Application) : AndroidViewModel(application) {

    private val _kodeJadwalResponse = MutableLiveData<Result<Boolean>>()
    val kodeJadwalResponse: LiveData<Result<Boolean>> = _kodeJadwalResponse

    private val apiService = RetrofitInstance.api

    fun checkKodeJadwal(request: KodeJadwalRequest) {
        viewModelScope.launch {
            try {
                val response = apiService.getKodeJadwal(request)
                _kodeJadwalResponse.value = Result.success(response.matched)
            } catch (e: Exception) {
                _kodeJadwalResponse.value = Result.failure(e)
            }
        }
    }
}
