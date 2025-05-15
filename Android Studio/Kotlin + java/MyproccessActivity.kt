package com.example.deftesisar

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.XAxis
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyproccessActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var lineChartTime: LineChart
    private lateinit var barChartPerformance: BarChart
    private lateinit var pieChartQuestion1: PieChart
    private lateinit var pieChartQuestion2: PieChart
    private lateinit var pieChartAssistance: PieChart
    private lateinit var summaryTextView: TextView
    private lateinit var backButton: Button
    private val apiService by lazy { ApiClient.retrofitInstance?.create(ApiService::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_myproccess)

        initializeViews()
        setupCharts()
        loadUserData()
        setupNavigationButtons()
    }

    private fun initializeViews() {
        progressBar = findViewById(R.id.progressBar)
        lineChartTime = findViewById(R.id.lineChartTime)
        barChartPerformance = findViewById(R.id.barChartPerformance)
        pieChartQuestion1 = findViewById(R.id.pieChartQuestion1)
        pieChartQuestion2 = findViewById(R.id.pieChartQuestion2)
        pieChartAssistance = findViewById(R.id.pieChartAssistance)
        summaryTextView = findViewById(R.id.summaryTextView)
        backButton = findViewById(R.id.backButton)
    }

    private fun setupCharts() {
        val textColor = Color.WHITE
        val backgroundColor = Color.BLACK

        lineChartTime.apply {
            setBackgroundColor(backgroundColor)
            description.textColor = textColor
            legend.textColor = textColor
            xAxis.textColor = textColor
            axisLeft.textColor = textColor
            axisRight.textColor = textColor
        }

        barChartPerformance.apply {
            setBackgroundColor(backgroundColor)
            description.textColor = textColor
            legend.textColor = textColor
            xAxis.textColor = textColor
            axisLeft.textColor = textColor
            axisRight.textColor = textColor
        }

        listOf(pieChartQuestion1, pieChartQuestion2, pieChartAssistance).forEach { chart ->
            chart.apply {
                setBackgroundColor(backgroundColor)
                description.textColor = textColor
                legend.textColor = textColor
                setEntryLabelColor(textColor)
            }
        }
    }

    private fun loadUserData() {
        showLoading(true)
        val userId = SharedPreferencesManager.getInstance(this).getUserId()

        apiService?.getUserProgress(userId)?.enqueue(object : Callback<UserProgressResponse> {
            override fun onResponse(call: Call<UserProgressResponse>, response: Response<UserProgressResponse>) {
                showLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let { updateCharts(it) }
                }
            }

            override fun onFailure(call: Call<UserProgressResponse>, t: Throwable) {
                showLoading(false)
            }
        })
    }

    private fun updateCharts(data: UserProgressResponse) {
        val lineEntries = data.attempts.mapIndexed { index, attempt ->
            Entry(index.toFloat(), attempt.detectionTime)
        }
        val lineDataSet = LineDataSet(lineEntries, "Detection Time").apply {
            color = Color.MAGENTA
            setCircleColor(Color.MAGENTA)
            valueTextColor = Color.WHITE
        }
        lineChartTime.data = LineData(lineDataSet)
        lineChartTime.invalidate()

        val categories = data.performanceCategories
        val barEntries = categories.map { BarEntry(it.index.toFloat(), it.value) }
        val barDataSet = BarDataSet(barEntries, "Performance").apply {
            colors = listOf(Color.GREEN, Color.YELLOW, Color.RED)
            valueTextColor = Color.WHITE
        }
        barChartPerformance.data = BarData(barDataSet)
        barChartPerformance.invalidate()

        updatePieChart(pieChartQuestion1, data.question1Performance, "Question 1")
        updatePieChart(pieChartQuestion2, data.question2Performance, "Question 2")
        updatePieChart(pieChartAssistance, data.assistanceUsage, "Assistance")

        updateSummary(data)
    }

    private fun updatePieChart(chart: PieChart, data: Map<String, Float>, label: String) {
        val entries = data.map { PieEntry(it.value, it.key) }
        val dataSet = PieDataSet(entries, label).apply {
            colors = listOf(Color.CYAN, Color.YELLOW)
            valueTextColor = Color.WHITE
        }
        chart.data = PieData(dataSet)
        chart.invalidate()
    }

    private fun updateSummary(data: UserProgressResponse) {
        val summaryText = """
            Performance Summary:
            Total Attempts: ${data.totalAttempts}
            Average Detection Time: ${String.format("%.2f", data.averageDetectionTime)}s
            Best Detection Time: ${String.format("%.2f", data.bestDetectionTime)}s
            Success Rate Q1: ${String.format("%.1f", data.question1SuccessRate)}%
            Success Rate Q2: ${String.format("%.1f", data.question2SuccessRate)}%
            Assistance Usage Rate: ${String.format("%.1f", data.assistanceUsageRate)}%
        """.trimIndent()
        summaryTextView.text = summaryText
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setupNavigationButtons() {
        backButton.setOnClickListener {
            val intent = Intent(this, MainMenu::class.java)
            startActivity(intent)
            finish()
        }
    }
}
