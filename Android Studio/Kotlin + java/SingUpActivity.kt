package com.example.deftesisar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class sing_up : AppCompatActivity() {
    private lateinit var usernameInput: EditText
    private lateinit var lastnameInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var passwordConfirm: EditText
    private lateinit var signUpBtn: Button
    private lateinit var cancelBtn: Button
    private lateinit var progressBar: ProgressBar
    private val apiService by lazy { ApiClient.retrofitInstance?.create(ApiService::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_up)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        usernameInput = findViewById(R.id.username_input)
        lastnameInput = findViewById(R.id.lastname_input)
        ageInput = findViewById(R.id.age_input)
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        passwordConfirm = findViewById(R.id.password_saved)
        signUpBtn = findViewById(R.id.sing_up_btn)
        cancelBtn = findViewById(R.id.Cancel_btn)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        signUpBtn.setOnClickListener { performSignUp() }
        cancelBtn.setOnClickListener { navigateToLogin() }
    }

    private fun performSignUp() {
        if (!validateInputs()) return

        showLoading(true)

        val newUser = User(
            user_name = usernameInput.text.toString().trim(),
            user_lastname = lastnameInput.text.toString().trim(),
            mail = emailInput.text.toString().trim(),
            age = ageInput.text.toString().toInt(),
            password = passwordInput.text.toString().trim()
        )

        apiService?.signUp(newUser)?.enqueue(object : Callback<SignUpResponse> {
            override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                showLoading(false)

                if (response.isSuccessful) {
                    handleSuccessfulSignUp(response.body())
                } else {
                    handleSignUpError(response)
                }
            }

            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                showLoading(false)
                handleNetworkError(t)
            }
        })
    }

    private fun validateInputs(): Boolean {
        val username = usernameInput.text.toString().trim()
        val lastname = lastnameInput.text.toString().trim()
        val age = ageInput.text.toString()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val confirmPassword = passwordConfirm.text.toString()

        when {
            username.isEmpty() -> {
                usernameInput.error = "Username is required"
                return false
            }
            lastname.isEmpty() -> {
                lastnameInput.error = "Last name is required"
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailInput.error = "Valid email is required"
                return false
            }
            password.length < 8 -> {
                passwordInput.error = "Password must be at least 8 characters"
                return false
            }
            password != confirmPassword -> {
                passwordConfirm.error = "Passwords do not match"
                return false
            }
        }

        try {
            val ageValue = age.toInt()
            if (ageValue !in 1..120) {
                ageInput.error = "Please enter a valid age (1-120)"
                return false
            }
        } catch (e: NumberFormatException) {
            ageInput.error = "Please enter a valid age"
            return false
        }

        return true
    }

    private fun handleSuccessfulSignUp(response: SignUpResponse?) {
        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun handleSignUpError(response: Response<SignUpResponse>) {
        val errorMessage = when (response.code()) {
            409 -> "Email already registered"
            400 -> "Invalid input data"
            else -> "Registration failed: ${response.message()}"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun handleNetworkError(throwable: Throwable) {
        Log.e("SignUp", "Connection error", throwable)
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
        signUpBtn.isEnabled = !show
        cancelBtn.isEnabled = !show
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, StartActivity::class.java))
        finish()
    }
}
