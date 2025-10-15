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

class UpdateBookActivity : AppCompatActivity() {

    private lateinit var etTitle: TextInputEditText
    private lateinit var etAuthor: TextInputEditText
    private lateinit var etCategory: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etIsbn: TextInputEditText
    private lateinit var etPublishedYear: TextInputEditText
    private lateinit var btnUpdate: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var apiService: ApiService
    
    private var bookId: String? = null
    private var currentBook: Book? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_book)

        // Get book data from intent
        bookId = intent.getStringExtra("book_id")
        currentBook = intent.getParcelableExtra("book")
        
        if (bookId == null || currentBook == null) {
            Toast.makeText(this, "Không có thông tin sách", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupClickListeners()
        populateFields()
        apiService = ApiClient.getRetrofit(this).create(ApiService::class.java)
    }

    private fun initViews() {
        etTitle = findViewById(R.id.etBookTitle)
        etAuthor = findViewById(R.id.etBookAuthor)
        etCategory = findViewById(R.id.etBookCategory)
        etDescription = findViewById(R.id.etBookDescription)
        etIsbn = findViewById(R.id.etBookIsbn)
        etPublishedYear = findViewById(R.id.etBookPublishedYear)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun populateFields() {
        currentBook?.let { book ->
            etTitle.setText(book.title)
            etAuthor.setText(book.author)
            etCategory.setText(book.category)
            etDescription.setText(book.description)
            etIsbn.setText(book.isbn)
            etPublishedYear.setText(book.publishedYear?.toString())
        }
    }

    private fun setupClickListeners() {
        btnUpdate.setOnClickListener {
            updateBook()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun updateBook() {
        val title = etTitle.text.toString().trim()
        val author = etAuthor.text.toString().trim()
        val category = etCategory.text.toString().trim()
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

        val updatedBook = Book(
            id = bookId,
            title = title,
            author = author,
            category = category.ifEmpty { null },
            description = description.ifEmpty { null },
            isbn = isbn.ifEmpty { null },
            publishedYear = publishedYear,
            available = currentBook?.available ?: true
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                btnUpdate.isEnabled = false
                btnUpdate.text = "Đang cập nhật..."
                
                val response = withContext(Dispatchers.IO) {
                    apiService.updateBook(bookId!!, updatedBook)
                }
                
                Toast.makeText(this@UpdateBookActivity, "Cập nhật sách thành công!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(this@UpdateBookActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnUpdate.isEnabled = true
                btnUpdate.text = "Cập nhật"
            }
        }
    }
}
