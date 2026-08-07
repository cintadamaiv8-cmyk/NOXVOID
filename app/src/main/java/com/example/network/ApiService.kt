package com.example.network

import android.util.Log
import com.example.data.AppConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.nio.charset.Charset

private const val TAG = "NetworkModule"

@JsonClass(generateAdapter = true)
data class LoginRequest(val nama: String, val password: String)

@JsonClass(generateAdapter = true)
data class UserData(val nama: String, val role: String, val tag: String?)

@JsonClass(generateAdapter = true)
data class LoginResponse(val success: Boolean, val message: String?, val token: String?, val user: UserData?)

@JsonClass(generateAdapter = true)
data class PingResponse(val status: String, val timestamp: Long)

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("ping")
    suspend fun ping(): PingResponse
}

object RetrofitClient {
    // create with optional token provider
    fun create(tokenProvider: () -> String?): ApiService {
        Log.d(TAG, "Creating Retrofit instance with BASE_URL=${AppConfig.BASE_URL}")

        val httpLogger = HttpLoggingInterceptor { msg ->
            Log.d(TAG, "OkHttpLog: $msg")
        }.apply { level = HttpLoggingInterceptor.Level.BODY }

        val inspector = Interceptor { chain ->
            val request = chain.request()
            Log.d(TAG, "BASE_URL used: ${AppConfig.BASE_URL}")
            Log.d(TAG, "Request URL: ${request.url}")
            Log.d(TAG, "Request Method: ${request.method}")

            val rb = request.body
            if (rb != null) {
                try {
                    val buffer = okio.Buffer()
                    rb.writeTo(buffer)
                    val charset = rb.contentType()?.charset(Charset.forName("UTF-8")) ?: Charset.forName("UTF-8")
                    val bodyString = buffer.readString(charset)
                    Log.d(TAG, "Request Body: $bodyString")
                } catch (e: Exception) {
                    Log.d(TAG, "Failed to read request body: ${e.message}")
                }
            } else {
                Log.d(TAG, "Request Body: <empty>")
            }

            val token = tokenProvider()
            val newReqBuilder = request.newBuilder()
            if (!token.isNullOrEmpty()) {
                newReqBuilder.addHeader("Authorization", "Bearer $token")
                Log.d(TAG, "Attaching token (partial): ${token.take(20)}...")
            } else {
                Log.d(TAG, "No token present")
            }

            val newRequest = newReqBuilder.build()
            val response = chain.proceed(newRequest)

            Log.d(TAG, "HTTP Status: ${response.code} for ${response.request.url}")

            val responseBody = response.body
            if (responseBody != null) {
                try {
                    val source = responseBody.source()
                    source.request(Long.MAX_VALUE)
                    val buffer = source.buffer.clone()
                    val charset = responseBody.contentType()?.charset(Charset.forName("UTF-8"))
                        ?: Charset.forName("UTF-8")
                    val bodyString = buffer.readString(charset)
                    Log.d(TAG, "Response Body: $bodyString")
                    val newBody = ResponseBody.create(responseBody.contentType(), bodyString.toByteArray(charset))
                    return@Interceptor response.newBuilder().body(newBody).build()
                } catch (e: Exception) {
                    Log.d(TAG, "Could not log response body: ${e.message}")
                }
            } else {
                Log.d(TAG, "Response Body: <empty>")
            }

            response
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(inspector)
            .addInterceptor(httpLogger)
            .build()

        val moshi = Moshi.Builder().build()

        val retrofit = Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()

        return retrofit.create(ApiService::class.java)
    }

    // convenience singleton (no token)
    val instance: ApiService by lazy { create { null } }
}
