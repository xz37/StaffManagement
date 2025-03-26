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

    fun fetchStaffList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            val result = performFetchStaffList()
            if (result.first) {
                @Suppress("UNCHECKED_CAST")
                val staffList = (result.second as List<Staff>).map { StaffWithImage(staff = it) }
                _uiState.update { it.copy(staffList = staffList, isLoading = false, errorMessage = "") }
                loadImagesForStaff(staffList) // 加载图片
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.second as String) }
            }
        }
    }

    private suspend fun performFetchStaffList(): Pair<Boolean, Any> {
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
                        Pair(true, newStaffList)
                    }
                } else {
                    connection.errorStream?.bufferedReader()?.use { reader ->
                        val errorResponse = reader.readText()
                        Pair(false, "Error: $errorResponse")
                    } ?: Pair(false, "Error: HTTP $responseCode")
                }
            } catch (e: Exception) {
                Pair(false, "Connection error: ${e.message ?: "Unknown error"}")
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
                            it.copy(bitmap = result.first, imageError = result.second)
                        } else {
                            it
                        }
                    }
                    currentState.copy(staffList = updatedList)
                }
            }
        }
    }

    private suspend fun loadImage(avatarUrl: String): Pair<Bitmap?, String> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(avatarUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                Pair(bitmap, "")
            } catch (e: Exception) {
                e.printStackTrace()
                Pair(null, "Failed to load image: ${e.message}")
            }
        }
    }
}