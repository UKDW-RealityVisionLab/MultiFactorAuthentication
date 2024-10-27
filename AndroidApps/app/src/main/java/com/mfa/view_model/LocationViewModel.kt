package com.mfa.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.mfa.Helper
import com.mfa.api.response.RuangResponseItem
import com.mfa.repository.MfaRepository
import kotlinx.coroutines.launch

class LocationViewModel(private val repository: MfaRepository):ViewModel() {
    private val lokasiData = MutableLiveData<Helper<RuangResponseItem>>()
    val getLokasiData: LiveData<Helper<RuangResponseItem>> = lokasiData

    fun getKelasByKodeRuang(idJadwal: String) {
        lokasiData.value= Helper.Loading
        viewModelScope.launch {
            try {
                repository.getRuangByKodeJadwal(idJadwal).asFlow().collect { helperResult ->
                    lokasiData.value = helperResult
                    Log.d("getkelasbykoderuang mendapatkan nilai :", idJadwal)
                }
            } catch (e: Exception) {
                Log.e("JadwalViewModel", "Error: ${e.message}")
            }
        }
    }
}