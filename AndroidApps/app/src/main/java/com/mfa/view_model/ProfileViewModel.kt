package com.mfa.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.mfa.api.request.EmailRequest
import com.mfa.repository.MfaRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: MfaRepository): ViewModel() {
    fun getProfile(email:String?) {
        viewModelScope.launch {
            repository.getProfile(email)

        }
    }
}