package com.example.androidfinalexam2

import okhttp3.*
import java.io.IOException

class HttpClient {
    private val client: OkHttpClient = OkHttpClient()

    fun get(url: String, callback: (String?, Exception?) -> Unit) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                callback(responseData, null)
            }
        })
    }

    fun post(url: String, requestBody: RequestBody, callback: (String?, Exception?) -> Unit) {
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                callback(responseData, null)
            }
        })
    }

    // 可以添加其他请求方法，如 PUT、DELETE 等

    fun cancelAllRequests() {
        client.dispatcher.cancelAll()
    }
}
