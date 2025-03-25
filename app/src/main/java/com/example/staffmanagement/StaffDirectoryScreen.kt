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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    // var staffList by remember { mutableStateOf<List<Staff>>(emptyList()) }

    // TODO: replace with actual data
    var staffList = listOf(
        Staff(1, "example1@example.com", "John", "Doe", "https://reqres.in/img/faces/1-image.jpg"),
        Staff(2, "example2@example.com", "Jane", "Smith", "https://reqres.in/img/faces/2-image.jpg"),
        Staff(3, "example3@example.com", "Alice", "Johnson", "https://reqres.in/img/faces/3-image.jpg"),
    )

    Column(
        modifier = Modifier
            .padding(vertical = 40.dp, horizontal = 16.dp)
    ) {
        Text(
            text = "Staff Directory",
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Text(
            text = "Token: $token",
        )

        Column {
            staffList.forEach { staff ->
                StaffItem(staff = staff)
            }
        }
    }
}

@Composable
fun StaffItem(staff: Staff) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
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
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StaffDirectoryScreenPreview() {
    StaffDirectoryScreen(token = "abc123")
}