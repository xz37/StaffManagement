package com.example.staffmanagement.domain

import android.graphics.Bitmap
import com.example.staffmanagement.data.Staff

sealed class LoginResult {
    data class Success(val token: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

sealed class FetchStaffResult {
    data class Success(val staffList: List<Staff>) : FetchStaffResult()
    data class Error(val message: String) : FetchStaffResult()
}

sealed class ImageLoadResult {
    data class Success(val bitmap: Bitmap) : ImageLoadResult()
    data class Error(val message: String) : ImageLoadResult()
}
