package com.mfa.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.mfa.Helper
import com.mfa.repository.MfaRepository
import com.mfa.api.response.JadwalResponse
import com.mfa.api.response.PertemuanResponse
import com.mfa.api.response.RuangResponse
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class JadwalViewModel(private val repository: MfaRepository) : ViewModel() {
    private val jadwalLiveData = MutableLiveData<Helper<JadwalResponse>>()
    val getJadwalLiveData: LiveData<Helper<JadwalResponse>> = jadwalLiveData

    private val pertemuanData = MutableLiveData<Helper<PertemuanResponse>>()
    val getPertemuanData: LiveData<Helper<PertemuanResponse>> = pertemuanData

    private val lokasiData = MutableLiveData<Helper<RuangResponse>>()
    val getLokasiData: LiveData<Helper<RuangResponse>> = lokasiData

    fun getJadwal() {
        viewModelScope.launch {
            repository.getJadwal().asFlow().collect {
                jadwalLiveData.value = it
            }
        }
    }

    fun getPertemuan(kode_jadwal: String) {
        viewModelScope.launch {
            repository.getPertemuan(kode_jadwal).asFlow().collect {
                pertemuanData.value = it
            }
        }
    }
    fun getKelasByKodeRuang(kodeRuang: String) {
        viewModelScope.launch {
            try {
                repository.getByKodeRuang(kodeRuang).asFlow().collect { helperResult ->
                    lokasiData.value = helperResult // Assign the entire Helper object
                }
            } catch (e: Exception) {
                Log.e("JadwalViewModel", "Error: ${e.message}")
            }
        }
    }

}
