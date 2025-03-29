package com.example.staffmanagement.data

import com.example.staffmanagement.domain.LoginResult
import com.example.staffmanagement.network.ApiResult
import com.example.staffmanagement.network.ApiService

interface LoginRepository {
    suspend fun login(email: String, password: String): LoginResult
}

class NetworkLoginRepository(
    private val apiService: ApiService
) : LoginRepository {

    override suspend fun login(email: String, password: String): LoginResult {
        return when (val result = apiService.login(email, password)) {
            is ApiResult.Success -> LoginResult.Success(result.data)
            is ApiResult.Error -> LoginResult.Error(result.message)
        }
    }
}
