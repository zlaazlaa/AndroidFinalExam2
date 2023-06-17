package com.example.androidfinalexam2

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AlertDialog
import java.io.FileOutputStream


class AddLost : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var findTimeEditText: TextView
    private lateinit var findLocationEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var saveLocationTextView: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var submitButton: Button
    private lateinit var takePhotoButton: MaterialButton
    private lateinit var imageView: ImageView
    private lateinit var buttonSelectTime: MaterialButton
    private val client = OkHttpClient()

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val GALLERY_REQUEST = 1001
    }

    private val CAMERA_PERMISSION_REQUEST = 100
    private val CAMERA_REQUEST = 101

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val themeValue = sharedPrefs.getInt("theme", R.style.AppTheme_Blue)
        setTheme(themeValue)

        setContentView(R.layout.activity_add_lost)

        titleEditText = findViewById(R.id.editTextTitle)
        findTimeEditText = findViewById(R.id.time_now)
        findLocationEditText = findViewById(R.id.editTextFindLocation)
        descriptionEditText = findViewById(R.id.editTextDescription)
        saveLocationTextView = findViewById(R.id.editTextSaveLocation)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        submitButton = findViewById(R.id.buttonSubmit)
        takePhotoButton = findViewById(R.id.take_photo)
        buttonSelectTime = findViewById(R.id.buttonSelectTime)
        imageView = findViewById(R.id.image)

        submitButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val findTime = findTimeEditText.text.toString()
            val findLocation = findLocationEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val saveLocation = saveLocationTextView.text.toString()
//            val type = typeEditText.text.toString()
            val type = spinnerCategory.selectedItem.toString()
            addLostItem(title, findTime, findLocation, convertImageToBase64(imageView), description, saveLocation, type)
//            val filePath = "image.jpg"
//            saveImageToFile(imageView, filePath)
            finish()
        }

        buttonSelectTime.setOnClickListener {
            showTimePickerDialog()
        }

        takePhotoButton.setOnClickListener {
            val options = arrayOf<CharSequence>("拍照", "选择上传")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("选择操作")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.CAMERA),
                                CAMERA_PERMISSION_REQUEST
                            )
                        } else {
                            openCamera()
                        }
                    }
                    1 -> openGallery()
                }
            }
            builder.show()
        }

        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)

        // 准备类别数据
        val categories = arrayOf(
            "书籍",
            "电子产品",
            "手机",
            "平板电脑",
            "电脑",
            "相机",
            "音乐播放器",
            "耳机",
            "充电器",
            "数据线",
            "证件"
        )

        // 创建适配器并设置数据
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // 设置适配器
        spinnerCategory.adapter = adapter
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                val timePickerDialog = TimePickerDialog(
                    this,
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                        val selectedTime = String.format("%04d-%02d-%02d %02d:%02d", year, month + 1, dayOfMonth, hourOfDay, minute)
                        findTimeEditText.text = selectedTime
                    },
                    hour,
                    minute,
                    true
                )
                timePickerDialog.show()
            },
            year,
            month,
            dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST)
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            val photo: Bitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(photo)
        }
        if (requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun saveImageToFile(imageView: ImageView, filePath: String) {
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        try {
            val fileOutputStream = FileOutputStream(filePath)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            Log.d("saveImageToFile", "Image saved successfully.")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("saveImageToFile", "Failed to save image.")
        }
    }


    private fun convertImageToBase64(imageView: ImageView): String {
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
//        Log.e("photo", Base64.encodeToString(byteArray, Base64.DEFAULT))
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun addLostItem(
        title: String,
        findTime: String,
        findLocation: String,
        imageBase64: String,
        description: String,
        saveLocation: String,
        type: String
    ) {
        val url = "https://ljm-python.azurewebsites.net/add_lost"
        val formBody = FormBody.Builder()
            .add("title", title)
            .add("find_time", findTime)
            .add("find_location", findLocation)
            .add("photo", imageBase64)
            .add("description", description)
            .add("save_location", saveLocation)
            .add("type", type)
            .add("item_status", "0")
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                // 处理响应数据
            }
        })
    }
}
