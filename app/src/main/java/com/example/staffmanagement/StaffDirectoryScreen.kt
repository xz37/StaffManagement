package com.example.staffmanagement

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


data class Staff(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val avatar: String
)

@Composable
fun StaffDirectoryScreen(token: String) {
    var staffList by remember { mutableStateOf<List<Staff>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    suspend fun fetchStaffList(): Pair<Boolean, Any> {
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
                                    avatar = staffObject.getString("avatar"),
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

    LaunchedEffect(key1 = true) {
        coroutineScope.launch {
            try {
                isLoading = true
                errorMessage = ""

                val result = fetchStaffList()

                if (result.first) {
                    @Suppress("UNCHECKED_CAST")
                    staffList = result.second as List<Staff>
                } else {
                    errorMessage = result.second as String
                }
            } catch (e: Exception) {
                errorMessage = "Unexpected error: ${e.message ?: "Unknown error"}"
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(vertical = 40.dp, horizontal = 16.dp)
    ) {
        Text(
            text = "Staff Directory",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Text(
            text = "Token: $token",
            modifier = Modifier.padding(bottom = 16.dp),
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (staffList.isNotEmpty()) {
            Column {
                staffList.forEach { staff ->
                    StaffItem(staff = staff)
                }
            }
        } else if (errorMessage.isEmpty() && staffList.isEmpty()) {
            Text(
                text = "No staff found",
                modifier = Modifier.padding(16.dp)
            )
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Snackbar(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(errorMessage)
            }
        }
    }
}

@Composable
fun StaffItem(staff: Staff) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageError by remember { mutableStateOf("") }
    LaunchedEffect(staff.avatar) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(staff.avatar)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
                imageError = "Failed to load image: ${e.message}"
            }
        }
    }

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
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
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
                Text(
                    text = "${staff.firstName} ${staff.lastName}",
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = staff.email,
                )
                if (imageError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = imageError,
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