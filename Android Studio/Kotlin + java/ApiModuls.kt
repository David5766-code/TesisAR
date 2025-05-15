package com.example.deftesisar

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Headers
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @Headers("Content-Type: application/json")

    @POST("signup") 
    fun signUp(@Body user: User): Call<SignUpResponse> 

    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("search/symptoms")
    fun searchBySymptoms(@Body request: SymptomsRequest): Call<List<SearchResult>>

    @POST("progress")
    fun saveProgress(@Body progressData: ProgressData): Call<ProgressResponse> 

    @GET("user/{userId}/progress") 
    fun getUserProgress(@Path("userId") userId: Int): Call<UserProgressResponse> 

    @GET("case")
    fun getRandomCase(): Call<case>
}

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:3000/"
    private const val TIMEOUT = 30L 

    private var retrofit: Retrofit? = null
    val retrofitInstance: Retrofit? 
        get() {
            if (retrofit == null) {
                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }

                val client = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build()

                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit
        }
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:4000/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

class SharedPreferencesManager private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) 

    companion object { 
        private const val PREF_NAME = "TesisARPrefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"

        @Volatile // Asegura visibilidad de cambios entre hilos.
        private var instance: SharedPreferencesManager? = null

        fun getInstance(context: Context): SharedPreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: SharedPreferencesManager(context.applicationContext).also {
                    instance = it 
                }
            }
        }
    }

    fun saveUserData(userId: Int, email: String, name: String) {
        sharedPreferences.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            apply() 
        }
    }

    fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, -1)
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    fun clearUserData() { 
        sharedPreferences.edit().clear().apply()
    }

    fun isUserLoggedIn(): Boolean {
        return getUserId() != -1
    }
}

class SearchViewModel : ViewModel() {
    private val _searchResults = MutableLiveData<List<SearchResult>>()
    val searchResults: LiveData<List<SearchResult>> get() = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val apiService = RetrofitClient.apiService

    fun searchSymptoms(symptoms: List<String>) {
        _isLoading.value = true
        val request = SymptomsRequest(symptoms)

        apiService.searchBySymptoms(request).enqueue(object : retrofit2.Callback<List<SearchResult>> {
            override fun onResponse(
                call: Call<List<SearchResult>>,
                response: retrofit2.Response<List<SearchResult>>
            ) {
                if (response.isSuccessful) {
                    val results = response.body()?.map { result ->
                        result.copy(
                            symptom = symptoms.joinToString(", "), 
                            name_Component = result.name_Component ?: "No component"
                        )
                    } ?: emptyList()

                    _searchResults.value = results
                } else {
                    _searchResults.value = emptyList()
                }
                _isLoading.value = false
            }

            override fun onFailure(call: Call<List<SearchResult>>, t: Throwable) {
                println("API Call Failure: ${t.message}")
                _searchResults.value = emptyList()
                _isLoading.value = false
            }
        })
    }
}

class SearchResultsAdapter : ListAdapter<SearchResult, SearchResultsAdapter.ViewHolder>(SearchResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { 
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) { 
        holder.bind(getItem(position))
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val symptomText: TextView = view.findViewById(R.id.symptomText)
        private val faultNameText: TextView = view.findViewById(R.id.faultNameText)
        private val componentText: TextView = view.findViewById(R.id.componentText)
        private val descriptionText: TextView = view.findViewById(R.id.descriptionText)

        fun bind(result: SearchResult) {
            symptomText.text = "Symptom: ${result.symptom ?: "No symptom"}"
            faultNameText.text = "Failure: ${result.fault_name ?: "No failure"}"
            componentText.text = "Component: ${result.name_Component ?: "No component"}"
            descriptionText.text = "Description: ${result.description ?: "No description"}"
        }
    }

    class SearchResultDiffCallback : DiffUtil.ItemCallback<SearchResult>() { 
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult) = 
            oldItem.id_Failure == newItem.id_Failure

        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult) = 
            oldItem == newItem
    }
}