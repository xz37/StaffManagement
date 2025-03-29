package com.example.staffmanagement.data

import com.example.staffmanagement.domain.ImageLoadResult
import com.example.staffmanagement.network.ApiResult
import com.example.staffmanagement.network.ApiService
import com.example.staffmanagement.domain.FetchStaffResult as FetchStaffResult1

data class Staff(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val avatar: String
)

interface StaffRepository {
    suspend fun performFetchStaffList(): FetchStaffResult1
    suspend fun loadImage(imageUrl: String): ImageLoadResult
}

class NetworkStaffRepository(
    private val apiService: ApiService
) : StaffRepository {

    override suspend fun performFetchStaffList(): FetchStaffResult1 {
        return when (val result = apiService.performFetchStaffList()) {
            is ApiResult.Success -> FetchStaffResult1.Success(result.data)
            is ApiResult.Error -> FetchStaffResult1.Error(result.message)
        }
    }

    override suspend fun loadImage(imageUrl: String): ImageLoadResult {
        return when (val result = apiService.loadImage(imageUrl)) {
            is ApiResult.Success -> ImageLoadResult.Success(result.data)
            is ApiResult.Error -> ImageLoadResult.Error(result.message)
        }
    }
}