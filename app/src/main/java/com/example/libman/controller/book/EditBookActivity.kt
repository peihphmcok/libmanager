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

class EditBookActivity : AppCompatActivity() {

    private lateinit var etTitle: TextInputEditText
    private lateinit var etAuthor: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etIsbn: TextInputEditText
    private lateinit var etPublishedYear: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnDelete: MaterialButton
    private lateinit var apiService: ApiService
    
    private var bookId: String? = null
    private var currentBook: Book? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_book)

        bookId = intent.getStringExtra("book_id")
        if (bookId == null) {
            Toast.makeText(this, "Không tìm thấy ID sách", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupClickListeners()
        apiService = ApiClient.getRetrofit(this).create(ApiService::class.java)
        
        loadBook()
    }

    private fun initViews() {
        etTitle = findViewById(R.id.etBookTitle)
        etAuthor = findViewById(R.id.etBookAuthor)
        etDescription = findViewById(R.id.etBookDescription)
        etIsbn = findViewById(R.id.etBookIsbn)
        etPublishedYear = findViewById(R.id.etBookPublishedYear)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        btnDelete = findViewById(R.id.btnDelete)
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            updateBook()
        }

        btnCancel.setOnClickListener {
            finish()
        }
        
        btnDelete.setOnClickListener {
            deleteBook()
        }
    }

    private fun loadBook() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getBook(bookId!!)
                }
                val book = response.book
                if (book != null) {
                    currentBook = book
                    populateFields(book)
                } else {
                    Toast.makeText(this@EditBookActivity, "Không tìm thấy thông tin sách", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditBookActivity, "Lỗi khi tải thông tin sách: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun populateFields(book: Book) {
        etTitle.setText(book.title)
        etAuthor.setText(book.author)
        etDescription.setText(book.description)
        etIsbn.setText(book.isbn)
        etPublishedYear.setText(book.publishedYear?.toString())
    }

    private fun updateBook() {
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

        val updatedBook = currentBook?.copy(
            title = title,
            author = author,
            description = description.ifEmpty { null },
            isbn = isbn.ifEmpty { null },
            publishedYear = publishedYear
        ) ?: Book(
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
                btnSave.text = "Đang cập nhật..."
                
                withContext(Dispatchers.IO) {
                    apiService.updateBook(bookId!!, updatedBook)
                }
                
                Toast.makeText(this@EditBookActivity, "Cập nhật sách thành công!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(this@EditBookActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnSave.isEnabled = true
                btnSave.text = "Cập nhật"
            }
        }
    }

    private fun deleteBook() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                btnDelete.isEnabled = false
                btnDelete.text = "Đang xóa..."
                
                withContext(Dispatchers.IO) {
                    apiService.deleteBook(bookId!!)
                }
                
                Toast.makeText(this@EditBookActivity, "Xóa sách thành công!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(this@EditBookActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnDelete.isEnabled = true
                btnDelete.text = "Xóa"
            }
        }
    }
}