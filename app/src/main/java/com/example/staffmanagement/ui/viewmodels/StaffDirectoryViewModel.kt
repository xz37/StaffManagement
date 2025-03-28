package com.example.staffmanagement.ui.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staffmanagement.data.Staff
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed class FetchStaffResult {
    data class Success(val staffList: List<Staff>) : FetchStaffResult()
    data class Error(val message: String) : FetchStaffResult()
}

sealed class ImageLoadResult {
    data class Success(val bitmap: Bitmap) : ImageLoadResult()
    data class Error(val message: String) : ImageLoadResult()
}

data class StaffWithImage(
    val staff: Staff,
    val bitmap: Bitmap? = null,
    val imageError: String = "",
)

data class StaffDirectoryUiState(
    val staffList: List<StaffWithImage> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String = "",
    val token: String = ""
)

class StaffDirectoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StaffDirectoryUiState())
    val uiState: StateFlow<StaffDirectoryUiState> = _uiState.asStateFlow()

    init {
        fetchStaffList()
    }

    private fun fetchStaffList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            when (val result = performFetchStaffList()) {
                is FetchStaffResult.Success -> {
                    val staffList = result.staffList.map { StaffWithImage(staff = it) }
                    _uiState.update { it.copy(staffList = staffList, isLoading = false, errorMessage = "") }
                    loadImagesForStaff(staffList)
                }
                is FetchStaffResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    private suspend fun performFetchStaffList(): FetchStaffResult {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://reqres.in/api/users?page=1")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().use { reader ->
                        val response = reader.readText()
                        val jsonResponse = JSONObject(response)
                        val data = jsonResponse.getJSONArray("data")
                        val newStaffList = mutableListOf<Staff>()
                        for (i in 0 until data.length()) {
                            val staffObject = data.getJSONObject(i)
                            newStaffList.add(
                                Staff(
                                    id = staffObject.getInt("id"),
                                    email = staffObject.getString("email"),
                                    firstName = staffObject.getString("first_name"),
                                    lastName = staffObject.getString("last_name"),
                                    avatar = staffObject.getString("avatar")
                                )
                            )
                        }
                        FetchStaffResult.Success(newStaffList)
                    }
                } else {
                    connection.errorStream?.bufferedReader()?.use { reader ->
                        val errorResponse = reader.readText()
                        FetchStaffResult.Error("Error: $errorResponse")
                    } ?: FetchStaffResult.Error("Error: HTTP $responseCode")
                }
            } catch (e: Exception) {
                FetchStaffResult.Error("Connection error: ${e.message ?: "Unknown error"}")
            }
        }
    }

    private fun loadImagesForStaff(staffList: List<StaffWithImage>) {
        staffList.forEach { staffWithImage ->
            viewModelScope.launch {
                val result = loadImage(staffWithImage.staff.avatar)
                _uiState.update { currentState ->
                    val updatedList = currentState.staffList.map {
                        if (it.staff == staffWithImage.staff) {
                            when (result) {
                                is ImageLoadResult.Success -> it.copy(bitmap = result.bitmap, imageError = "")
                                is ImageLoadResult.Error -> it.copy(bitmap = null, imageError = result.message)
                            }
                        } else {
                            it
                        }
                    }
                    currentState.copy(staffList = updatedList)
                }
            }
        }
    }

    private suspend fun loadImage(avatarUrl: String): ImageLoadResult {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(avatarUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                bitmap?.let {
                    ImageLoadResult.Success(it)
                } ?: ImageLoadResult.Error("Failed to decode bitmap")

            } catch (e: Exception) {
                e.printStackTrace()
                ImageLoadResult.Error("Failed to load image: ${e.message}")
            }
        }
    }
}