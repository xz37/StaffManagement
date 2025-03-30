package com.example.staffmanagement.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.staffmanagement.R
import com.example.staffmanagement.ui.viewmodels.StaffDirectoryViewModel
import com.example.staffmanagement.ui.viewmodels.StaffWithImage

@Composable
fun StaffDirectoryScreen(
    token: String,
    staffDirectoryViewModel: StaffDirectoryViewModel = viewModel()
) {
    val staffDirectoryUiState by staffDirectoryViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .padding(vertical = 40.dp, horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.staff_directory),
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Text(
            text = stringResource(R.string.token, token),
            modifier = Modifier.padding(bottom = 16.dp),
        )

        if (staffDirectoryUiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (staffDirectoryUiState.staffList.isNotEmpty()) {
            LazyColumn {
                items(staffDirectoryUiState.staffList) { staffWithImage ->
                    StaffItem(staffWithImage = staffWithImage)
                }
            }
        } else if (staffDirectoryUiState.errorMessage.isEmpty() && staffDirectoryUiState.staffList.isEmpty()) {
            Text(
                text = stringResource(R.string.no_staff_found),
                modifier = Modifier.padding(16.dp)
            )
        }

        if (staffDirectoryUiState.errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Snackbar(modifier = Modifier.padding(8.dp)) {
                Text(staffDirectoryUiState.errorMessage)
            }
        }
    }
}

@Composable
fun StaffItem(staffWithImage: StaffWithImage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (staffWithImage.bitmap != null) {
                Image(
                    bitmap = staffWithImage.bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp),
                        color = Color.Gray
                    )
                }
            }

            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(text = "${staffWithImage.staff.firstName} ${staffWithImage.staff.lastName}")
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = staffWithImage.staff.email)
                if (staffWithImage.imageError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = staffWithImage.imageError,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StaffDirectoryScreenPreview() {
    StaffDirectoryScreen(token = "abc123")
}