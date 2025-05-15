package com.example.deftesisar

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchActivity : AppCompatActivity() {
    private lateinit var viewModel: SearchViewModel
    private lateinit var adapter: SearchResultsAdapter

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var resultsRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var helpButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        helpButton = findViewById(R.id.helpButton)
        backButton = findViewById(R.id.backButton)

        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        setupRecyclerView()
        setupSearchButton()
        setupNavigationButtons()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = SearchResultsAdapter()
        resultsRecyclerView.layoutManager = LinearLayoutManager(this)
        resultsRecyclerView.adapter = adapter
    }

    private fun setupSearchButton() {
        searchButton.setOnClickListener {
            val searchText = searchEditText.text.toString().trim()

            if (searchText.isNotBlank()) {
                val symptoms = searchText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                if (symptoms.isEmpty()) {
                    searchEditText.error = "Enter at least one valid symptom or indicator."
                    return@setOnClickListener
                }

                viewModel.searchSymptoms(symptoms)
            } else {
                searchEditText.error = "Enter at least one symptom or indicator."
            }
        }
    }


    private fun setupNavigationButtons() {
        helpButton.setOnClickListener {
            val intent = Intent(this, UnityPlayerGameActivity::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            val intent = Intent(this, MainMenu::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun observeViewModel() {
        viewModel.searchResults.observe(this) { results ->
            if (results.isEmpty()) {
                searchEditText.error = "No results found. Please check your search. "
            } else {
                adapter.submitList(results)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}
