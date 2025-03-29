package com.example.staffmanagement.network

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
}

