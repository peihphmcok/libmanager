package com.example.libman.controller.book

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.libman.R
import com.example.libman.models.Book
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

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
    
    // Book cover upload
    private lateinit var ivBookCover: ImageView
    private lateinit var cardBookCover: MaterialCardView
    private var selectedCoverUri: Uri? = null
    
    private var bookId: String? = null
    private var currentBook: Book? = null
    
    // Image picker - using GetContent instead of StartActivityForResult
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri ->
        if (imageUri != null) {
            android.util.Log.d("UpdateBookActivity", "Image selected: $imageUri")
            selectedCoverUri = imageUri
            ivBookCover.setImageURI(imageUri)
            Toast.makeText(this, "Đã chọn ảnh thành công!", Toast.LENGTH_SHORT).show()
        }
    }

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
        
        // Book cover views
        ivBookCover = findViewById(R.id.ivBookCover)
        cardBookCover = findViewById(R.id.cardBookCover)
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
        
        // Book cover click listener
        cardBookCover.setOnClickListener {
            openImagePicker()
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
                
                // Upload book cover if selected
                selectedCoverUri?.let { coverUri ->
                    try {
                        uploadBookCover(bookId!!, coverUri)
                    } catch (e: Exception) {
                        android.util.Log.e("UpdateBookActivity", "Error uploading book cover: ${e.message}", e)
                        Toast.makeText(this@UpdateBookActivity, "Sách đã được cập nhật nhưng lỗi khi upload ảnh bìa", Toast.LENGTH_SHORT).show()
                    }
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
    
    private fun openImagePicker() {
        try {
            android.util.Log.d("UpdateBookActivity", "Opening image picker...")
            imagePickerLauncher.launch("image/*")
        } catch (e: Exception) {
            android.util.Log.e("UpdateBookActivity", "Error opening image picker: ${e.message}", e)
            Toast.makeText(this, "Lỗi khi mở chọn ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun uploadBookCover(bookId: String, coverUri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Get file from URI
                val inputStream = contentResolver.openInputStream(coverUri)
                val file = File(cacheDir, "book_cover_${System.currentTimeMillis()}.jpg")
                file.outputStream().use { output ->
                    inputStream?.copyTo(output)
                }
                
                // Create multipart body
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("cover", file.name, requestFile)
                
                // Upload to API
                val response = withContext(Dispatchers.IO) {
                    apiService.uploadBookCover(bookId, body)
                }
                
                android.util.Log.d("UpdateBookActivity", "Book cover uploaded successfully")
                
            } catch (e: Exception) {
                android.util.Log.e("UpdateBookActivity", "Error uploading book cover: ${e.message}", e)
                throw e
            }
        }
    }
}
