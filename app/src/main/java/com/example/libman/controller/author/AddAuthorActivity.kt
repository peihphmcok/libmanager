package com.example.libman.controller.author

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.libman.R
import com.example.libman.models.Author
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddAuthorActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etBio: TextInputEditText
    private lateinit var etNationality: TextInputEditText
    private lateinit var etBirthYear: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_author)

        initViews()
        setupClickListeners()
        apiService = ApiClient.getRetrofit(this).create(ApiService::class.java)
    }

    private fun initViews() {
        etName = findViewById(R.id.etAuthorName)
        etBio = findViewById(R.id.etAuthorBio)
        etNationality = findViewById(R.id.etAuthorNationality)
        etBirthYear = findViewById(R.id.etAuthorBirthYear)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            saveAuthor()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveAuthor() {
        val name = etName.text.toString().trim()
        val bio = etBio.text.toString().trim()
        val nationality = etNationality.text.toString().trim()
        val birthYearStr = etBirthYear.text.toString().trim()

        if (name.isEmpty()) {
            etName.error = "Vui lòng nhập tên tác giả"
            return
        }

        val birthYear = if (birthYearStr.isNotEmpty()) {
            try {
                birthYearStr.toInt()
            } catch (e: NumberFormatException) {
                etBirthYear.error = "Năm sinh không hợp lệ"
                return
            }
        } else null

        val author = Author(
            name = name,
            bio = bio.ifEmpty { null },
            nationality = nationality.ifEmpty { null },
            birthYear = birthYear
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                btnSave.isEnabled = false
                btnSave.text = "Đang lưu..."
                
                val response = withContext(Dispatchers.IO) {
                    apiService.addAuthor(author)
                }
                
                Toast.makeText(this@AddAuthorActivity, "Thêm tác giả thành công!", Toast.LENGTH_SHORT).show()
                
                // Return the added author data
                val resultIntent = Intent()
                resultIntent.putExtra("added_author", author)
                setResult(RESULT_OK, resultIntent)
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(this@AddAuthorActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnSave.isEnabled = true
                btnSave.text = "Lưu"
            }
        }
    }
}