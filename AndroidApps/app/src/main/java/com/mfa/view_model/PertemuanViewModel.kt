package com.mfa.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.mfa.Helper
import com.mfa.api.response.PertemuanResponseItem
import com.mfa.repository.MfaRepository
import kotlinx.coroutines.launch

class PertemuanViewModel(private val repository: MfaRepository):ViewModel() {
    private val pertemuanData = MutableLiveData<Helper<List<PertemuanResponseItem>?>>()
    val getPertemuanData: LiveData<Helper<List<PertemuanResponseItem>?>> = pertemuanData

    fun getPertemuan(kodeKelas: Int) {
        pertemuanData.value=Helper.Loading
        viewModelScope.launch {
            repository.getPertemuan(kodeKelas).asFlow().collect { result ->
                pertemuanData.value = result
            }
        }
    }
}