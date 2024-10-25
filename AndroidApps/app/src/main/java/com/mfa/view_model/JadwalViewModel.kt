package com.mfa.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.mfa.Helper
import com.mfa.api.response.HomeResponseItem
import com.mfa.repository.MfaRepository
import com.mfa.api.response.PertemuanResponseItem
import com.mfa.api.response.RuangResponse
import com.mfa.api.response.RuangResponseItem
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class JadwalViewModel(private val repository: MfaRepository) : ViewModel() {
    private val jadwalLiveData = MutableLiveData<Helper.Success<List<HomeResponseItem>>>()
    val getJadwalLiveData: LiveData<Helper.Success<List<HomeResponseItem>>> = jadwalLiveData

    fun getJadwal() {
        viewModelScope.launch {
            repository.getKelas().asFlow().collect {
                jadwalLiveData.value = it
            }
        }
    }


}