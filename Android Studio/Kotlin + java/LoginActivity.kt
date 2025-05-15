package com.example.deftesisar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StartActivity : AppCompatActivity() {
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginBtn: Button
    private lateinit var signInBtn: Button
    private lateinit var progressBar: ProgressBar
    private val apiService by lazy { ApiClient.retrofitInstance?.create(ApiService::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeViews()
        setupClickListeners()
        checkExistingSession()
    }

    private fun initializeViews() {
        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        loginBtn = findViewById(R.id.login_btn)
        signInBtn = findViewById(R.id.sing_in_btn)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        loginBtn.setOnClickListener { performLogin() }
        signInBtn.setOnClickListener { navigateToSignUp() }
    }

    private fun checkExistingSession() {
        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", null)
        if (!token.isNullOrEmpty()) {
            navigateToMainMenu()
        }
    }

    private fun performLogin() {
        val email = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (!validateInputs(email, password)) return

        showLoading(true)

        apiService?.login(LoginRequest(email, password))?.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                showLoading(false)

                if (response.isSuccessful) {
                    handleSuccessfulLogin(response.body())

                    Log.d("LoginActivity", "Login Successful: ${response.body()?.user?.name}")
                } else {
                    handleLoginError(response)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showLoading(false)
                handleNetworkError(t)
            }
        })
    }

    private fun validateInputs(email: String, password: String): Boolean {
        when {
            email.isEmpty() -> {
                usernameInput.error = "Email is required"
                return false
            }
            password.isEmpty() -> {
                passwordInput.error = "Password is required"
                return false
            }
        }
        return true
    }

    private fun handleSuccessfulLogin(response: LoginResponse?) {
        response?.let {
            // Guardar datos en SharedPreferencesManager
            SharedPreferencesManager.getInstance(this).saveUserData(
                userId = it.user.id,
                email = it.user.email,
                name = it.user.name
            )

            // Mantener el guardado del token en SharedPreferences
            getSharedPreferences("auth", MODE_PRIVATE).edit().apply {
                putString("token", it.token)
                apply()
            }

            Toast.makeText(this, "Welcome ${it.user.name}!", Toast.LENGTH_SHORT).show()
            navigateToMainMenu()
        }
    }

    private fun handleLoginError(response: Response<LoginResponse>) {
        val errorMessage = when (response.code()) {
            401 -> "Invalid email or password"
            else -> "Login failed: ${response.message()}"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun handleNetworkError(throwable: Throwable) {
        Log.e("Login", "Connection error", throwable)
        val errorMessage = when {
            throwable.message?.contains("Unable to resolve host") == true ->
                "No internet connection"
            throwable.message?.contains("timeout") == true ->
                "Connection timed out"
            else -> "Connection error: ${throwable.message}"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        loginBtn.isEnabled = !show
        signInBtn.isEnabled = !show
    }

    private fun navigateToMainMenu() {
        startActivity(Intent(this, MainMenu::class.java))
        finish()
    }

    private fun navigateToSignUp() {
        startActivity(Intent(this, sing_up::class.java))
        finish()
    }
}
