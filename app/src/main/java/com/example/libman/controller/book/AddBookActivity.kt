package com.example.libman.controller.book

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.libman.R
import com.example.libman.models.Book
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddBookActivity : AppCompatActivity() {

    private lateinit var etTitle: TextInputEditText
    private lateinit var etAuthor: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etIsbn: TextInputEditText
    private lateinit var etPublishedYear: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        initViews()
        setupClickListeners()
        apiService = ApiClient.getRetrofit(this).create(ApiService::class.java)
    }

    private fun initViews() {
        etTitle = findViewById(R.id.etBookTitle)
        etAuthor = findViewById(R.id.etBookAuthor)
        etDescription = findViewById(R.id.etBookDescription)
        etIsbn = findViewById(R.id.etBookIsbn)
        etPublishedYear = findViewById(R.id.etBookPublishedYear)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            saveBook()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveBook() {
        val title = etTitle.text.toString().trim()
        val author = etAuthor.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val isbn = etIsbn.text.toString().trim()
        val publishedYearStr = etPublishedYear.text.toString().trim()

        if (title.isEmpty()) {
            etTitle.error = "Vui lòng nhập tên sách"
            return
        }

        if (author.isEmpty()) {
            etAuthor.error = "Vui lòng nhập tác giả"
            return
        }

        val publishedYear = if (publishedYearStr.isNotEmpty()) {
            try {
                publishedYearStr.toInt()
            } catch (e: NumberFormatException) {
                etPublishedYear.error = "Năm xuất bản không hợp lệ"
                return
            }
        } else null

        val book = Book(
            title = title,
            author = author,
            description = description.ifEmpty { null },
            isbn = isbn.ifEmpty { null },
            publishedYear = publishedYear,
            available = true
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                btnSave.isEnabled = false
                btnSave.text = "Đang lưu..."
                
                val response = withContext(Dispatchers.IO) {
                    apiService.addBook(book)
                }
                
                Toast.makeText(this@AddBookActivity, "Thêm sách thành công!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(this@AddBookActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnSave.isEnabled = true
                btnSave.text = "Lưu"
            }
        }
    }
}