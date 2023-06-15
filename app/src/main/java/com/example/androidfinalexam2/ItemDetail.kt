package com.example.androidfinalexam2

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ItemDetail : AppCompatActivity() {
    lateinit var textTitle: TextView
    lateinit var textTime: TextView
    lateinit var textLocation: TextView
    lateinit var textSaveLocation: TextView
    lateinit var imagePhoto: ImageView
    lateinit var imageCategory: ImageView
    lateinit var imageStatus: ImageView
    lateinit var itemId: TextView
    private val handler = Handler(Looper.getMainLooper())

    @SuppressLint("SimpleDateFormat", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)
        textTitle = findViewById(R.id.textTitle)
        textTime = findViewById(R.id.textTime)
        textLocation = findViewById(R.id.textLocation)
        textSaveLocation = findViewById(R.id.textSaveLocation)
        imagePhoto = findViewById(R.id.photo)
        imageCategory = findViewById(R.id.itemCate)
        imageStatus = findViewById(R.id.item_status)
        itemId = findViewById(R.id.item_id)
        val id = intent.getStringExtra("id")
        if (id != null) {
            refresh(id)
        }
        findViewById<MaterialButton>(R.id.claim).setOnClickListener {
            val client = OkHttpClient()
            val url = "https://ljm-python.azurewebsites.net/update_item_status"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val currentTimeStr: String = dateFormat.format(Date())
            val formBody = id?.let { it1 ->
                FormBody.Builder()
                    .add("id", it1)
                    .add("item_status", "1")
                    .add("claim_time", currentTimeStr)
                    .build()
            }

            val request = formBody?.let { it1 ->
                Request.Builder()
                    .url(url)
                    .post(it1)
                    .build()
            }

            if (request != null) {
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseData = response.body?.string()
                        finish()
                    }
                })
            }

        }
    }

    @SuppressLint("WrongViewCast", "CutPasteId")
    private fun refresh(id: String) {
        val httpClient = HttpClient()
        val url = "https://ljm-python.azurewebsites.net/get_lost_items?id=$id"
        httpClient.get(url) { responseData, exception ->
            if (exception != null) {
                exception.printStackTrace()
            } else {
                val lostItems = Gson().fromJson(responseData, Array<LostItem>::class.java).toList()
                val item = lostItems[0]
                handler.post {
                    textTitle.text = item.title
                    textTime.text = item.find_time
                    textLocation.text = item.find_location
                    textSaveLocation.text = item.save_location
                    displayImageFromBase64(item.photo, imagePhoto)
                    imageCategory.setImageResource(getCategoryIcon(item.type))
                    imageStatus.setImageResource(getCategoryIcon(item.item_status))
                    if (item.item_status == "0") {
                        findViewById<MaterialButton>(R.id.claim).isVisible = true
                    } else {
                        val claimTextView = findViewById<TextView>(R.id.claim_time)
                        val claimTextViewLayout = findViewById<LinearLayout>(R.id.claim_time_layout)
                        claimTextView.text = item.claim_time
                        claimTextViewLayout.isVisible = true
                    }
                }
            }
        }
    }

    private fun displayImageFromBase64(base64Data: String, imageView: ImageView) {
        val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        imageView.setImageBitmap(bitmap)
    }

    private fun getCategoryIcon(category: String): Int {
        return when (category) {
            "书籍" -> R.drawable.shuji
            "证件" -> R.drawable.zhengjian
            "电子产品" -> R.drawable.dianzi
            "平板电脑" -> R.drawable.pingbandiannao
            "电脑" -> R.drawable.diannao
            "相机" -> R.drawable.xiangji
            "音乐播放器" -> R.drawable.yinyuebofangqi
            "耳机" -> R.drawable.erji
            "充电器" -> R.drawable.chongdianqi
            "1" -> R.drawable.yes
            "0" -> R.drawable.no
            else -> R.drawable.shuji
        }
    }
}