package com.example.libman.controller.book

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.libman.R
import com.example.libman.models.Book
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.example.libman.utils.TokenManager
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

class AddBookActivity : AppCompatActivity() {

    private lateinit var etTitle: TextInputEditText
    private lateinit var etAuthor: TextInputEditText
    private lateinit var etCategory: AutoCompleteTextView
    private lateinit var etDescription: TextInputEditText
    private lateinit var etIsbn: TextInputEditText
    private lateinit var etPublishedYear: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var apiService: ApiService
    
    // Book cover upload
    private lateinit var ivBookCover: ImageView
    private lateinit var cardBookCover: MaterialCardView
    private var selectedCoverUri: Uri? = null
    
    private var categories = listOf<String>()
    
    // Image picker - using GetContent instead of StartActivityForResult
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri ->
        if (imageUri != null) {
            android.util.Log.d("AddBookActivity", "Image selected: $imageUri")
            selectedCoverUri = imageUri
            ivBookCover.setImageURI(imageUri)
            Toast.makeText(this, "Đã chọn ảnh thành công!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        initViews()
        setupClickListeners()
        apiService = ApiClient.getRetrofit(this).create(ApiService::class.java)
        
        // Check authentication
        val tokenManager = TokenManager(this)
        val token = tokenManager.getToken()
        val userId = tokenManager.getUserId()
        android.util.Log.d("AddBookActivity", "Token: $token")
        android.util.Log.d("AddBookActivity", "UserId: $userId")
        
        // Test API connection
        testApiConnection()
        
        loadCategories()
    }

    private fun initViews() {
        etTitle = findViewById(R.id.etBookTitle)
        etAuthor = findViewById(R.id.etBookAuthor)
        etCategory = findViewById(R.id.etBookCategory)
        etDescription = findViewById(R.id.etBookDescription)
        etIsbn = findViewById(R.id.etBookIsbn)
        etPublishedYear = findViewById(R.id.etBookPublishedYear)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        
        // Book cover views
        ivBookCover = findViewById(R.id.ivBookCover)
        cardBookCover = findViewById(R.id.cardBookCover)
    }

    private fun testApiConnection() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                android.util.Log.d("AddBookActivity", "Testing API connection...")
                
                val response = withContext(Dispatchers.IO) {
                    apiService.getBooks()
                }
                
                android.util.Log.d("AddBookActivity", "API Test - Books: ${response.books?.size}")
                android.util.Log.d("AddBookActivity", "API Test - Total: ${response.total}")
                android.util.Log.d("AddBookActivity", "API Test - Message: ${response.message}")
                
                Toast.makeText(this@AddBookActivity, "API OK: ${response.books?.size} books", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                android.util.Log.e("AddBookActivity", "API Test Failed: ${e.message}", e)
                Toast.makeText(this@AddBookActivity, "API Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun loadCategories() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                android.util.Log.d("AddBookActivity", "Loading categories from server...")
                
                val response = withContext(Dispatchers.IO) {
                    apiService.getBooks()
                }
                
                android.util.Log.d("AddBookActivity", "Books response: ${response.books?.size} books found")
                android.util.Log.d("AddBookActivity", "Total books: ${response.total}")
                
                // Log all books for debugging
                response.books?.forEachIndexed { index, book ->
                    android.util.Log.d("AddBookActivity", "Book $index: ${book.title} - Category: ${book.category}")
                }
                
                // Extract unique categories from books
                val uniqueCategories = response.books
                    ?.mapNotNull { it.category }
                    ?.filter { it.isNotBlank() }
                    ?.distinct()
                    ?.sorted()
                    ?: emptyList()
                
                android.util.Log.d("AddBookActivity", "Unique categories: $uniqueCategories")
                
                // If no categories found, use default ones
                categories = if (uniqueCategories.isEmpty()) {
                    android.util.Log.w("AddBookActivity", "No categories found, using defaults")
                    listOf("Văn học", "Khoa học", "Lịch sử", "Kịch", "Tiểu thuyết", "Khác")
                } else {
                    uniqueCategories
                }
                
                android.util.Log.d("AddBookActivity", "Final categories: $categories")
                
                // Setup category spinner with loaded categories
                setupCategorySpinner()
                
                Toast.makeText(this@AddBookActivity, "Đã tải ${categories.size} thể loại", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                android.util.Log.e("AddBookActivity", "Error loading categories: ${e.message}", e)
                // Fallback to default categories if API fails
                categories = listOf("Văn học", "Khoa học", "Lịch sử", "Kịch", "Tiểu thuyết", "Khác")
                setupCategorySpinner()
                Toast.makeText(this@AddBookActivity, "Không thể tải danh sách thể loại: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        etCategory.setAdapter(adapter)
        
        // Set default selection if categories are available
        if (categories.isNotEmpty()) {
            etCategory.setText(categories[0], false)
        }
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            saveBook()
        }

        btnCancel.setOnClickListener {
            finish()
        }
        
        // Book cover click listener
        cardBookCover.setOnClickListener {
            android.util.Log.d("AddBookActivity", "Book cover card clicked")
            Toast.makeText(this, "Đang mở chọn ảnh...", Toast.LENGTH_SHORT).show()
            openImagePicker()
        }
    }

    private fun saveBook() {
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

        val book = Book(
            title = title,
            author = author,
            category = category.ifEmpty { null },
            description = description.ifEmpty { null },
            isbn = isbn.ifEmpty { null },
            publishedYear = publishedYear,
            available = true
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                btnSave.isEnabled = false
                btnSave.text = "Đang lưu..."
                
                // Debug logging
                android.util.Log.d("AddBookActivity", "Adding book: $book")
                
                val response = withContext(Dispatchers.IO) {
                    apiService.addBook(book)
                }
                
                android.util.Log.d("AddBookActivity", "Book added successfully: $response")
                
                // Upload book cover if selected
                selectedCoverUri?.let { coverUri ->
                    try {
                        uploadBookCover(response.id ?: "", coverUri)
                    } catch (e: Exception) {
                        android.util.Log.e("AddBookActivity", "Error uploading book cover: ${e.message}", e)
                        Toast.makeText(this@AddBookActivity, "Sách đã được thêm nhưng lỗi khi upload ảnh bìa", Toast.LENGTH_SHORT).show()
                    }
                }
                
                Toast.makeText(this@AddBookActivity, "Thêm sách thành công!", Toast.LENGTH_SHORT).show()
                
                // Return the added book data
                val resultIntent = Intent()
                resultIntent.putExtra("added_book", response)
                setResult(RESULT_OK, resultIntent)
                finish()
                
            } catch (e: Exception) {
                android.util.Log.e("AddBookActivity", "Error adding book: ${e.message}", e)
                Toast.makeText(this@AddBookActivity, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                btnSave.isEnabled = true
                btnSave.text = "Lưu"
            }
        }
    }
    
    private fun openImagePicker() {
        try {
            android.util.Log.d("AddBookActivity", "Opening image picker...")
            imagePickerLauncher.launch("image/*")
        } catch (e: Exception) {
            android.util.Log.e("AddBookActivity", "Error opening image picker: ${e.message}", e)
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
                
                android.util.Log.d("AddBookActivity", "Book cover uploaded successfully")
                
            } catch (e: Exception) {
                android.util.Log.e("AddBookActivity", "Error uploading book cover: ${e.message}", e)
                throw e
            }
        }
    }
}