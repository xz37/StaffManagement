package com.example.staffmanagement.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.staffmanagement.data.Staff
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}

private const val BASE_URL = "https://reqres.in/api"

interface ApiService {
    suspend fun login(email: String, password: String): ApiResult<String>
    suspend fun performFetchStaffList(): ApiResult<List<Staff>>
    suspend fun loadImage(imageUrl: String): ApiResult<Bitmap>
}

class NetworkApiService : ApiService {
    override suspend fun login(email: String, password: String): ApiResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/login?delay=5")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonPayload = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonPayload.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().use { reader ->
                        val response = reader.readText()
                        val jsonResponse = JSONObject(response)
                        val token = jsonResponse.getString("token")
                        ApiResult.Success(token)
                    }
                } else {
                    connection.errorStream?.bufferedReader()?.use { reader ->
                        val errorResponse = reader.readText()
                        try {
                            val jsonError = JSONObject(errorResponse)
                            val error = jsonError.getString("error")
                            ApiResult.Error(error)
                        } catch (e: Exception) {
                            ApiResult.Error("Error: $errorResponse")
                        }
                    } ?: ApiResult.Error("Error: HTTP $responseCode")
                }
            } catch (e: Exception) {
                ApiResult.Error("Connection error: ${e.message ?: "Unknown error"}")
            }
        }
    }

    override suspend fun performFetchStaffList(): ApiResult<List<Staff>> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/users?page=1")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().use { reader ->
                        val response = reader.readText()
                        val jsonResponse = JSONObject(response)
                        val data = jsonResponse.getJSONArray("data")
                        val staffList = mutableListOf<Staff>()

                        for (i in 0 until data.length()) {
                            val staffObject = data.getJSONObject(i)
                            staffList.add(
                                Staff(
                                    id = staffObject.getInt("id"),
                                    email = staffObject.getString("email"),
                                    firstName = staffObject.getString("first_name"),
                                    lastName = staffObject.getString("last_name"),
                                    avatar = staffObject.getString("avatar")
                                )
                            )
                        }
                        ApiResult.Success(staffList)
                    }
                } else {
                    connection.errorStream?.bufferedReader()?.use { reader ->
                        val errorResponse = reader.readText()
                        ApiResult.Error("Error: $errorResponse")
                    } ?: ApiResult.Error("Error: HTTP $responseCode")
                }
            } catch (e: Exception) {
                ApiResult.Error("Connection error: ${e.message ?: "Unknown error"}")
            }
        }
    }

    override suspend fun loadImage(imageUrl: String): ApiResult<Bitmap> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()

                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                bitmap?.let {
                    ApiResult.Success(it)
                } ?: ApiResult.Error("Failed to decode bitmap")
            } catch (e: Exception) {
                e.printStackTrace()
                ApiResult.Error("Failed to load image: ${e.message}")
            }
        }
    }
}

