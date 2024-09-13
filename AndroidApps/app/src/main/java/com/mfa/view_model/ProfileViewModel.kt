package com.mfa.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.mfa.Helper
import com.mfa.api.request.EmailRequest
import com.mfa.api.request.StatusReq
import com.mfa.api.request.UpdateStatusReq
import com.mfa.api.response.ProfileResponse
import com.mfa.api.response.RuangResponseItem
import com.mfa.api.response.cekStatus
import com.mfa.repository.MfaRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: MfaRepository): ViewModel() {
    private val data = MutableLiveData<ProfileResponse>()
    val getData: LiveData<ProfileResponse> = data

    private val dataStatus = MutableLiveData<Boolean>()
    val getDataStatus: LiveData<Boolean> = dataStatus

    private val updateDataStatus = MutableLiveData<Boolean>()
    val getUpdateDataStatus: LiveData<Boolean> = updateDataStatus

    fun getProfile(email:EmailRequest) {
        viewModelScope.launch {
            repository.getProfile(email).asFlow().collect{
                data.value=it
            }
        }
    }

    fun getStatus(req: StatusReq) {
        viewModelScope.launch {
            repository.cekStatusUser(req).asFlow().collect{
                dataStatus.value=it
            }
        }
    }

    fun updateStatus(req: UpdateStatusReq) {
        viewModelScope.launch {
            repository.updateStatus(req).asFlow().collect{
                updateDataStatus.value=it
            }
        }
    }
}