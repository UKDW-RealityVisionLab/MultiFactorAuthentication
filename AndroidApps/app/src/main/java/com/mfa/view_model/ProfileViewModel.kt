package com.mfa.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.mfa.Helper
import com.mfa.api.request.EmailRequest
import com.mfa.api.response.ProfileResponse
import com.mfa.api.response.RuangResponseItem
import com.mfa.repository.MfaRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: MfaRepository): ViewModel() {
    private val data = MutableLiveData<ProfileResponse>()
    val getData: LiveData<ProfileResponse> = data

    fun getProfile(email:EmailRequest) {
        viewModelScope.launch {
            repository.getProfile(email).asFlow().collect{
                data.value=it
            }
            repository.getProfile(email)
        }
    }
}