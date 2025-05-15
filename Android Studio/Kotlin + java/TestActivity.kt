package com.example.deftesisar

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date

class TestActivity : AppCompatActivity() {
    private lateinit var chronometer: Chronometer
    private lateinit var btnStartTest: Button
    private lateinit var btnFinishTest: Button
    private lateinit var btnHelpAR: Button
    private lateinit var btnMainMenu: Button
    private lateinit var etAnswer1: EditText
    private lateinit var etAnswer2: EditText
    private lateinit var tvResult1: TextView
    private lateinit var tvResult2: TextView
    private lateinit var tvCaseDescription: TextView

    private var startTime: Long = 0
    private var isTestStarted = false
    private var currentCase: case? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        initializeViews()
        setupListeners()
        fetchRandomCase()
    }

    private fun initializeViews() {
        chronometer = findViewById(R.id.chronometer)
        btnStartTest = findViewById(R.id.btnStartTest)
        btnFinishTest = findViewById(R.id.btnFinishTest)
        btnHelpAR = findViewById(R.id.btnHelpAR)
        btnMainMenu = findViewById(R.id.btnMainMenu)
        etAnswer1 = findViewById(R.id.etAnswer1)
        etAnswer2 = findViewById(R.id.etAnswer2)
        tvResult1 = findViewById(R.id.tvResult1)
        tvResult2 = findViewById(R.id.tvResult2)
        tvCaseDescription = findViewById(R.id.tvCaseDescription)

        btnFinishTest.isEnabled = false
        btnHelpAR.isEnabled = false
        etAnswer1.isEnabled = false
        etAnswer2.isEnabled = false
    }

    private fun setupListeners() {
        btnStartTest.setOnClickListener {
            startTest()
        }

        btnFinishTest.setOnClickListener {
            finishTest(false)
        }

        btnHelpAR.setOnClickListener {
            finishTest(true)
            startActivity(Intent(this, SearchActivity::class.java))
        }

        btnMainMenu.setOnClickListener {
            resetTest()
            startActivity(Intent(this, MainMenu::class.java))
            finish()
        }
    }

    private fun fetchRandomCase() {
        tvCaseDescription.text = "Loading case..."

        val apiService = ApiClient.retrofitInstance?.create(ApiService::class.java)
        apiService?.getRandomCase()?.enqueue(object : Callback<case> {
            override fun onResponse(call: Call<case>, response: Response<case>) {
                if (response.isSuccessful && response.body() != null) {
                    currentCase = response.body()
                    tvCaseDescription.text = currentCase?.practice_case
                } else {
                    tvCaseDescription.text = "Error loading case. Please try again."
                    Toast.makeText(this@TestActivity, "Error loading case", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<case>, t: Throwable) {
                tvCaseDescription.text = "Network error. Please check your connection."
                Toast.makeText(this@TestActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startTest() {
        if (currentCase == null) {
            Toast.makeText(this, "Cannot start test: No case loaded", Toast.LENGTH_SHORT).show()
            return
        }

        isTestStarted = true
        startTime = SystemClock.elapsedRealtime()
        chronometer.base = startTime
        chronometer.start()

        btnStartTest.isEnabled = false
        btnFinishTest.isEnabled = true
        btnHelpAR.isEnabled = true
        etAnswer1.isEnabled = true
        etAnswer2.isEnabled = true
    }

    private fun finishTest(usedHelp: Boolean) {
        if (!isTestStarted || currentCase == null) return

        chronometer.stop()
        val elapsedMillis = SystemClock.elapsedRealtime() - startTime

        val answer1Correct = etAnswer1.text.toString().trim().equals(currentCase?.fault, ignoreCase = true)
        val answer2Correct = etAnswer2.text.toString().trim().equals(currentCase?.component, ignoreCase = true)

        tvResult1.visibility = View.VISIBLE
        tvResult2.visibility = View.VISIBLE
        tvResult1.text = if (answer1Correct) "Correct!" else "Incorrect"
        tvResult2.text = if (answer2Correct) "Correct!" else "Incorrect"

        saveProgress(elapsedMillis, usedHelp, answer1Correct, answer2Correct)

        etAnswer1.isEnabled = false
        etAnswer2.isEnabled = false
        btnFinishTest.isEnabled = false
        btnHelpAR.isEnabled = false
    }

    private fun resetTest() {
        chronometer.stop()
        chronometer.base = SystemClock.elapsedRealtime()
        etAnswer1.setText("")
        etAnswer2.setText("")
        tvResult1.visibility = View.GONE
        tvResult2.visibility = View.GONE
        isTestStarted = false
        fetchRandomCase()
    }

    private fun saveProgress(
        elapsedMillis: Long,
        usedHelp: Boolean,
        answer1Correct: Boolean,
        answer2Correct: Boolean
    ) {
        val progressData = ProgressData(
            id_user = SharedPreferencesManager.getInstance(this).getUserId(),
            date_time_attempted = Date(),
            detection_time = elapsedMillis / 1000.0,
            use_assistance = usedHelp,
            answer1 = answer1Correct,
            answer2 = answer2Correct
        )

        val apiService = ApiClient.retrofitInstance?.create(ApiService::class.java)
        apiService?.saveProgress(progressData)?.enqueue(object : Callback<ProgressResponse> {
            override fun onResponse(call: Call<ProgressResponse>, response: Response<ProgressResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@TestActivity, "Progress saved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@TestActivity, "Error saving progress", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProgressResponse>, t: Throwable) {
                Toast.makeText(this@TestActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}