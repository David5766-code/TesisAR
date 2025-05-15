package com.example.deftesisar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class MainMenu : AppCompatActivity() {

    private lateinit var helpRAButton: Button
    private lateinit var knowledgeTestButton: Button
    private lateinit var myProgressButton: Button
    private lateinit var closeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)

        initiliazeViews()
        setupClickListeners()

    }

    private fun initiliazeViews() {
        helpRAButton = findViewById(R.id.help_ra_btn)
        knowledgeTestButton = findViewById(R.id.knowledge_test_btn)
        myProgressButton = findViewById(R.id.my_progress_btn)
        closeButton = findViewById(R.id.close_btn)
    }

    private fun setupClickListeners() {
        helpRAButton.setOnClickListener {navigateToHelpRA()}
        knowledgeTestButton.setOnClickListener {navigateToKnowledgeTest()}
        myProgressButton.setOnClickListener {navigateToMyProgress()}
        closeButton.setOnClickListener {logout()}
    }

    private fun navigateToHelpRA() {
        startActivity(Intent(this, SearchActivity::class.java))
        finish()
    }

    private fun navigateToKnowledgeTest() {
        startActivity(Intent(this, TestActivity::class.java))
        finish()
    }

    private fun navigateToMyProgress() {
        startActivity(Intent(this, MyproccessActivity::class.java))
        finish()
    }

    private fun logout() {
        SharedPreferencesManager.getInstance(this).clearUserData()

        getSharedPreferences("auth", MODE_PRIVATE).edit().apply {
            remove("token")
            apply()
        }

        startActivity(Intent(this, StartActivity::class.java))
        finish()
    }
}