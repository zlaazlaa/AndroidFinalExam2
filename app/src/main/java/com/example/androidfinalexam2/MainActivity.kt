package com.example.androidfinalexam2

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LostItemsAdapter
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerClaimStatus: Spinner

    private val spinnerItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            val selectedCategory = spinnerCategory.selectedItem.toString()
            val selectedMonth = spinnerMonth.selectedItem.toString()
            val selectedClaimStatus = spinnerClaimStatus.selectedItem.toString()
            refreshRecyclerView(selectedCategory, selectedMonth, selectedClaimStatus)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // 不执行任何操作
        }
    }
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener {
            val intent = Intent(this, AddLost::class.java)
            startActivity(intent)
        }
        findViewById<MaterialButton>(R.id.statistics).setOnClickListener {
            val intent = Intent(this, Statistics::class.java)
            startActivity(intent)
        }
        val btnLightTheme: Button = findViewById(R.id.btnLightTheme)
        val btnDarkTheme: Button = findViewById(R.id.btnDarkTheme)
        val btnBlueTheme: Button = findViewById(R.id.btnBlueTheme)

        btnLightTheme.setOnClickListener {
            setTheme(R.style.AppTheme_Red)
            recreate()
        }

        btnDarkTheme.setOnClickListener {
            setTheme(R.style.AppTheme_Blue)
            recreate()
        }

        btnBlueTheme.setOnClickListener {
            setTheme(R.style.AppTheme_Blue)
            recreate()
        }
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // 获取下拉框的引用
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerMonth = findViewById(R.id.spinnerMonth)
        spinnerClaimStatus = findViewById(R.id.spinnerClaimStatus)

        val categoryData = listOf(
            "类型筛选", "书籍", "电子产品", "手机", "平板电脑", "电脑",
            "相机", "音乐播放器", "耳机", "充电器", "数据线", "证件"
        )
        val monthData = listOf("月份筛选", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")
        val claimStatusData = listOf("状态筛选", "未认领", "已认领")

        val categoryAdapter = MainAdapter(this, categoryData)
        val monthAdapter = MainAdapter(this, monthData)
        val claimStatusAdapter = MainAdapter(this, claimStatusData)

        spinnerCategory.adapter = categoryAdapter
        spinnerMonth.adapter = monthAdapter
        spinnerClaimStatus.adapter = claimStatusAdapter

        spinnerCategory.onItemSelectedListener = spinnerItemSelectedListener
        spinnerMonth.onItemSelectedListener = spinnerItemSelectedListener
        spinnerClaimStatus.onItemSelectedListener = spinnerItemSelectedListener

        refresh()
    }

    private fun refreshRecyclerView(category: String, month: String, claimStatus: String) {
        var modifiedCategory = category
        var modifiedMonth = month
        var modifiedClaimStatus = claimStatus
        if (modifiedCategory == "类型筛选") {
            modifiedCategory = ""
        }
        if (modifiedMonth == "月份筛选") {
            modifiedMonth = ""
        }
        if (modifiedClaimStatus == "状态筛选") {
            modifiedClaimStatus = ""
        }
        val httpClient = HttpClient()
        val url = "https://ljm-python.azurewebsites.net/get_lost_items?category=$modifiedCategory&month=$modifiedMonth&claim_status=$modifiedClaimStatus"
        httpClient.get(url) { responseData, exception ->
            if (exception != null) {
                exception.printStackTrace()
            } else {
                val items = Gson().fromJson(responseData, Array<LostItem>::class.java).toList()
                handler.post {
                    adapter = LostItemsAdapter(items)
                    recyclerView.adapter = adapter
                }
            }
        }
    }

    fun refresh() {
        val httpClient = HttpClient()
        val url = "https://ljm-python.azurewebsites.net/get_lost_items"
        httpClient.get(url) { responseData, exception ->
            if (exception != null) {
                exception.printStackTrace()
            } else {
                val lostItems = Gson().fromJson(responseData, Array<LostItem>::class.java).toList()
                handler.post {
                    adapter = LostItemsAdapter(lostItems)
                    recyclerView.adapter = adapter
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }
}
