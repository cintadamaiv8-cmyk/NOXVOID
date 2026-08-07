package com.example.network

import com.example.data.AppConfig
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginRequest(val nama: String, val password: String)
data class UserData(val nama: String, val role: String, val tag: String)
data class LoginResponse(val success: Boolean, val message: String?, val token: String?, val user: UserData?)
data class PingResponse(val status: String, val timestamp: Long)

interface ApiService {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    @GET("/ping")
    suspend fun ping(): PingResponse
}

object RetrofitClient {
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
