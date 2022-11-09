package com.realityexpander.simplenetworkcalls

import android.net.TrafficStats
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import java.lang.reflect.Method
import java.net.HttpURLConnection
import java.net.URL

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class UserProfileResponse(
    val page : String,
    @JsonNames("per_page")
    val perPage: Int,
    @JsonNames("total")
    val totalPages: Int,
    @JsonNames("total_pages")
    val currentPage : Int,
    val data : List<UserEntity>
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class UserEntity constructor(
    val id : String,
    @JsonNames("first_name")
    val firstName : String,
    @JsonNames("last_name")
    val lastName : String,
    val email : String,
    @JsonNames("avatar")
    val avatarLink : String
)

private val jsonDecodeLenientIgnoreUnknown = Json {
    isLenient = true          // Changes JSON Ints to Strings automatically if needed.
    ignoreUnknownKeys = true  // If there are extra fields in the JSON, ignore them.
}

interface ReqResApi {
    @GET("users?page=2")
    suspend fun getUsers(): Response<UserProfileResponse>
}

class MainViewModel: ViewModel() {

    private val _user0 = MutableStateFlow<UserEntity?>(null)
    val user0 = _user0.asStateFlow()

    private val _user1 = MutableStateFlow<UserEntity?>(null)
    val user1 = _user1.asStateFlow()

    private val _user2 = MutableStateFlow<UserEntity?>(null)
    val user2 = _user2.asStateFlow()

    private val _user3 = MutableStateFlow<UserEntity?>(null)
    val user3 = _user3.asStateFlow()

    private val _user4 = MutableStateFlow<UserEntity?>(null)
    val user4 = _user4.asStateFlow()

    // Setup OKHttp client
    private val okhttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor { chain ->
            TrafficStats.setThreadStatsTag(Thread.currentThread().getId().toInt())
            val request = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer 1234567890")
                .build()
            chain.proceed(request)
        }
        .build()

    // Setup retrofit
    @OptIn(ExperimentalSerializationApi::class)
    private val reqResApi = Retrofit.Builder()
        .baseUrl("https://reqres.in/api/")
        .addConverterFactory(jsonDecodeLenientIgnoreUnknown.asConverterFactory("application/json".toMediaType()))
        .client(okhttpClient)
        .build()
        .create(ReqResApi::class.java)


    init {
        viewModelScope.launch {
            val url = URL("https://reqres.in/api/users?page=2") // Will make separate network calls to the same URL

            launch(Dispatchers.IO) {

                TrafficStats.setThreadStatsTag(Thread.currentThread().getId().toInt()) // For strict mode warning

                //////////////////////////////////////////////////////////////
                // • Method 0 (no status code checking, simplest)
                val user0Json = url.readText()
                val dataUser0 = jsonDecodeLenientIgnoreUnknown.decodeFromString<UserProfileResponse>(user0Json)
                println("user 0: ${dataUser0.data[0].firstName}")
                _user0.value = dataUser0.data[0]


                //////////////////////////////////////////////////////////////
                // • Method 1 (no status code checking, has headers, uses streams)
                val user1Stream = (url.openConnection() as HttpURLConnection).apply {
                        setRequestProperty("Authorization", "Development")
                    }.inputStream
                user1Stream.apply {
                    val user1 = bufferedReader().use {
                        jsonDecodeLenientIgnoreUnknown.decodeFromString<UserProfileResponse>(it.readText())
                    }
                    close()

                    println("user 1: ${user1.data[1].firstName}")
                    _user1.value = user1.data[1]
                }

//                ////////////////////////////////////////////////////////
//                // • Method 2-Alt (same as below except with .disconnect() )
//                val user2Json = (url.openConnection() as HttpURLConnection).let {
//                    it.setRequestProperty("Authorization", "Development")
//                    val json = url.readText(charset = Charsets.UTF_8)
//                    it.disconnect()
//                    json
//                }

                //////////////////////////////////////////////////////////////
                // • Method 2 (no status code checking, has headers, uses simple .readText()) )
                (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("Authorization", "Development")
                }
                val user2Json = url.readText(charset = Charsets.UTF_8)

                val user2 = jsonDecodeLenientIgnoreUnknown.decodeFromString<UserProfileResponse>(user2Json)
                println("user 2: ${user2.data[2].firstName}")
                _user2.value = user2.data[2]


                //////////////////////////////////////////////////////////////
                // • Method 3 (status code checks, headers, can change request method, uses streams)
                val http = (url.openConnection() as HttpURLConnection)
                http.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                http.setRequestProperty("Accept", "application/json")
                http.setRequestProperty("Authorization", "Development")
                http.requestMethod = "GET"
                http.doInput = true    // true if we want to read server's response
                http.doOutput = false  // GET request, so no output
                http.connect()
                val responseCode = http.responseCode
                val userJson = http.inputStream.use { input ->
                    val json = input.bufferedReader().use { it.readText() }
                    input.close()
                    json
                }
                if(responseCode == 200) {
                    println("Success")

                    val user3 = jsonDecodeLenientIgnoreUnknown.decodeFromString<UserProfileResponse>(userJson)
                    println("user 3: ${user3.data[3].firstName}")
                    _user3.value = user3.data[3]
                } else {
                    println("Failure")
                }


                //////////////////////////////////////////////////////////////
                // • Retrofit method
                val response = reqResApi.getUsers()
                if (response.isSuccessful) {
                    val users = response.body()
                    println("user 4: ${users?.data?.get(4)?.firstName ?: "Loading..."}")
                    _user4.value = users?.data?.get(4)
                }
            }
        }
    }

}