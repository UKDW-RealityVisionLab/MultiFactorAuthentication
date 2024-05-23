package com.mfa.di

import android.content.Context
import com.mfa.repository.MfaRepository
import com.mfa.api.retrofit.ApiConfig

object Injection {
    fun provideRepository(context: Context): MfaRepository {
        val apiService= ApiConfig.getApiService()
        return MfaRepository.getInstance(apiService)
    }
}