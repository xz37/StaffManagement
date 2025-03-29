package com.example.staffmanagement.ui.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staffmanagement.data.NetworkStaffRepository
import com.example.staffmanagement.data.Staff
import com.example.staffmanagement.domain.FetchStaffResult
import com.example.staffmanagement.domain.ImageLoadResult
import com.example.staffmanagement.network.NetworkApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    private val repository = NetworkStaffRepository(NetworkApiService())

    private val _uiState = MutableStateFlow(StaffDirectoryUiState())
    val uiState: StateFlow<StaffDirectoryUiState> = _uiState.asStateFlow()

    init {
        fetchStaffList()
    }

    private fun fetchStaffList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            when (val result = repository.performFetchStaffList()) {
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

    private fun loadImagesForStaff(staffList: List<StaffWithImage>) {
        staffList.forEach { staffWithImage ->
            viewModelScope.launch {
                val result = repository.loadImage(staffWithImage.staff.avatar)
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
}