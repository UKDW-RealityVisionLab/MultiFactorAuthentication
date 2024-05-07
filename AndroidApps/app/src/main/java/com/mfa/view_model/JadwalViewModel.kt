package com.mfa.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.mfa.Helper
import com.mfa.repository.MfaRepository
import com.mfa.api.response.JadwalResponse
import kotlinx.coroutines.launch

class JadwalViewModel(private val repository: MfaRepository) : ViewModel() {
    private val jadwalLiveData = MutableLiveData<Helper<JadwalResponse>>()
    val getJadwalLiveData: LiveData<Helper<JadwalResponse>> = jadwalLiveData

    fun getJadwal() {
        viewModelScope.launch {
            repository.getJadwal().asFlow().collect {
                jadwalLiveData.value = it
            }
        }

    }
}
