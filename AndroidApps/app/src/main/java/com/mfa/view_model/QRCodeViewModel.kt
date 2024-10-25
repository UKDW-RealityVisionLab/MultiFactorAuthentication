package com.mfa.view_model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mfa.api.request.KodeJadwalRequest
import com.mfa.api.retrofit.ApiConfig
import kotlinx.coroutines.launch


class QRCodeViewModel(application: Application) : AndroidViewModel(application) {

    private val _kodeJadwalResponse = MutableLiveData<Result<Boolean>>()
    val kodeJadwalResponse: LiveData<Result<Boolean>> = _kodeJadwalResponse

    private val apiService = ApiConfig.getApiService()

    fun checkKodeJadwal(request: KodeJadwalRequest) {
        viewModelScope.launch {
            try {
                val response = apiService.getKodeJadwal(request)
                Log.d("QR data request","$request")
                _kodeJadwalResponse.value = Result.success(response.matched)
            } catch (e: Exception) {
                _kodeJadwalResponse.value = Result.failure(e)
            }
        }
    }
}
