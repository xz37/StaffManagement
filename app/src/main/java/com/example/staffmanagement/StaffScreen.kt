package com.example.staffmanagement

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class Staff(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val avatar: String
)

@Composable
fun StaffScreen(token: String) {

    // var staffList by remember { mutableStateOf<List<Staff>>(emptyList()) }

    // TODO: replace with actual data
    var staffList = listOf(
        Staff(1, "example1@example.com", "John", "Doe", "image1"),
        Staff(2, "example2@example.com", "Jane", "Smith", "image2"),
        Staff(3, "example3@example.com", "Alice", "Johnson", "image3"),
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
            Image(
                painter = painterResource(id = R.drawable.__image),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

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
fun StaffScreenPreview() {
    StaffScreen(token = "abc123")
}