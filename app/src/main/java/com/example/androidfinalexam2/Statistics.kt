package com.example.androidfinalexam2

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class Statistics : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val themeValue = sharedPrefs.getInt("theme", R.style.AppTheme_Blue)
        setTheme(themeValue)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)
        updateBar()
        updatePie()
    }

    private fun updateBar() {
        val httpClient = HttpClient()
        val url = "https://ljm-python.azurewebsites.net/get_month_data"
        httpClient.get(url) { responseData, exception ->
            if (exception != null) {
                exception.printStackTrace()
            } else {
                val backendData: String? = responseData
                try {
                    val jsonObject = JSONObject(backendData)
                    val registerCountsArray = jsonObject.getJSONArray("registerCounts")
                    val claimCountsArray = jsonObject.getJSONArray("claimCounts")
                    val registerCounts = ArrayList<Float>()
                    val claimCounts = ArrayList<Float>()
                    for (i in 0 until registerCountsArray.length()) {
                        val count = registerCountsArray.getDouble(i).toFloat()
                        registerCounts.add(count)
                    }
                    for (i in 0 until claimCountsArray.length()) {
                        val count = claimCountsArray.getDouble(i).toFloat()
                        claimCounts.add(count)
                    }
                    handler.post {
                        val months = arrayOf(
                            "Jan",
                            "Feb",
                            "Mar",
                            "Apr",
                            "May",
                            "Jun",
                            "Jul",
                            "Aug",
                            "Sep",
                            "Oct",
                            "Nov",
                            "Dec"
                        )
                        val barChart: BarChart = findViewById(R.id.barChart)
                        val registerEntries = ArrayList<BarEntry>()
                        val claimEntries = ArrayList<BarEntry>()
                        for (i in registerCounts.indices) {
                            registerEntries.add(BarEntry(i.toFloat(), registerCounts[i]))
                            claimEntries.add(BarEntry(i.toFloat(), claimCounts[i]))
                        }
                        val registerDataSet = BarDataSet(registerEntries, "登记数量")
                        val claimDataSet = BarDataSet(claimEntries, "认领数量")
                        registerDataSet.color = Color.BLUE
                        claimDataSet.color = Color.GREEN
                        val data = BarData(registerDataSet, claimDataSet)
                        val barSpace = 0.02f // 柱状图之间的间隔
                        val groupSpace = 0.3f // 每组柱状图之间的间隔
                        val groupCount = months.size // 组数
                        data.barWidth = 0.3f // 设置每个柱状图的宽度
                        barChart.data = data
                        barChart.xAxis.axisMinimum = 0f
                        barChart.xAxis.axisMaximum =
                            0f + data.getGroupWidth(groupSpace, barSpace) * groupCount
                        barChart.groupBars(0f, groupSpace, barSpace) // 分组并排显示柱状图
                        val xAxis = barChart.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(months)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.setDrawGridLines(false)
                        barChart.axisLeft.axisMinimum = 0f
                        barChart.axisRight.isEnabled = false
                        val legend = barChart.legend
                        legend.setDrawInside(false)
                        barChart.invalidate()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("aa", e.toString())
                }
            }
        }

    }

    private fun updatePie() {
        val httpClient = HttpClient()
        val url = "https://ljm-python.azurewebsites.net/get_type_data"
        httpClient.get(url) { responseData, exception ->
            if (exception != null) {
                exception.printStackTrace()
            } else {
                val backendData: String? = responseData
                val jsonArray = JSONArray(backendData)
                val list = ArrayList<PieEntry>()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val value = jsonObject.getInt("value") // 获取数量值
                    val label = jsonObject.getString("label") // 获取标签值

                    val pieEntry = PieEntry(value.toFloat(), label)
                    list.add(pieEntry)
                }
                handler.post {
                    val pie = findViewById<View>(R.id.pieChart) as PieChart
                    val pieDataSet = PieDataSet(list, "")
                    val pieData = PieData(pieDataSet)
                    pie.data = pieData
                    val pieColors = listOf(
                        Color.RED,
                        Color.BLUE,
                        Color.GREEN,
                        Color.YELLOW,
                        Color.CYAN,
                        Color.MAGENTA,
                        Color.GRAY,
                        Color.BLACK,
                        Color.LTGRAY,
                        Color.DKGRAY
                    )
                    pieDataSet.colors = pieColors
                    pie.invalidate()
                }
            }
        }
    }
}
