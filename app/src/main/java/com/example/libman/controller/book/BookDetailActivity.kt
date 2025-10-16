package com.example.libman.controller.book

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.libman.R
import com.example.libman.controller.review.ReviewActivity
import com.example.libman.models.Book
import com.example.libman.models.Loan
import com.example.libman.models.BorrowRequest
import com.example.libman.models.Review
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.example.libman.utils.TokenManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookDetailActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvAuthor: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvIsbn: TextView
    private lateinit var tvPublishedYear: TextView
    private lateinit var chipAvailability: Chip
    private lateinit var btnBorrow: MaterialButton
    private lateinit var btnEdit: MaterialButton
    private lateinit var btnReviews: MaterialButton
    private lateinit var toolbar: Toolbar
    private lateinit var apiService: ApiService
    private lateinit var tokenManager: TokenManager
    
    // Review summary views (optional - may not exist in layout)
    private var tvReviewSummary: MaterialTextView? = null
    private var tvRecentReviews: MaterialTextView? = null

    private var bookId: String? = null
    private var currentBook: Book? = null
    private var reviews: List<Review> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)

        initViews()
        setupClickListeners()
        apiService = ApiClient.getRetrofit(this).create(ApiService::class.java)
        tokenManager = TokenManager(this)

        // Get book ID from intent
        bookId = intent.getStringExtra("book_id")
        
        if (bookId == null) {
            Toast.makeText(this, "Không tìm thấy ID sách", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Load book from API
        loadBook(bookId!!)
        
        // Load reviews for this book
        loadBookReviews()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tvTitle = findViewById(R.id.tvBookTitle)
        tvAuthor = findViewById(R.id.tvBookAuthor)
        tvDescription = findViewById(R.id.tvBookDescription)
        tvIsbn = findViewById(R.id.tvBookIsbn)
        tvPublishedYear = findViewById(R.id.tvBookPublishedYear)
        chipAvailability = findViewById(R.id.chipAvailability)
        btnBorrow = findViewById(R.id.btnBorrow)
        btnEdit = findViewById(R.id.btnEdit)
        btnReviews = findViewById(R.id.btnReviews)
        
        // Initialize review summary views (these don't exist in layout yet)
        tvReviewSummary = null
        tvRecentReviews = null
        
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupClickListeners() {
        btnBorrow.setOnClickListener {
            borrowBook()
        }

        btnEdit.setOnClickListener {
            editBook()
        }

        btnReviews.setOnClickListener {
            viewReviews()
        }
        
        // Add delete button to toolbar
        toolbar.inflateMenu(R.menu.book_detail_menu)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete_book -> {
                    showDeleteBookDialog()
                    true
                }
                else -> false
            }
        }
    }


    private fun loadBook(id: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                android.util.Log.d("BookDetailActivity", "Loading book with ID: $id")
                
                // Show loading state
                tvTitle.text = "Đang tải..."
                tvAuthor.text = "Đang tải thông tin sách..."
                tvDescription.text = "Đang tải..."
                tvIsbn.text = "Đang tải..."
                tvPublishedYear.text = "Đang tải..."
                
                val response = withContext(Dispatchers.IO) {
                    apiService.getBook(id)
                }
                val book = response.book
                if (book != null) {
                    android.util.Log.d("BookDetailActivity", "Book loaded: ${book.title}")
                    currentBook = book
                    displayBook(book)
                } else {
                    android.util.Log.e("BookDetailActivity", "Book is null in response")
                    Toast.makeText(this@BookDetailActivity, "Không tìm thấy thông tin sách", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("BookDetailActivity", "Error loading book: ${e.message}", e)
                Toast.makeText(this@BookDetailActivity, "Lỗi khi tải thông tin sách: ${e.message}", Toast.LENGTH_LONG).show()
                
                // Show error state
                tvTitle.text = "Lỗi tải dữ liệu"
                tvAuthor.text = "Không thể tải thông tin sách"
                tvDescription.text = "Vui lòng kiểm tra kết nối mạng và thử lại"
                tvIsbn.text = "N/A"
                tvPublishedYear.text = "N/A"
                chipAvailability.text = "Không xác định"
            }
        }
    }

    private fun displayBook(book: Book) {
        android.util.Log.d("BookDetailActivity", "Displaying book: ${book.title}")
        
        tvTitle.text = book.title ?: "Không có tiêu đề"
        tvAuthor.text = book.author ?: "Không có tác giả"
        tvDescription.text = book.description ?: "Không có mô tả"
        tvIsbn.text = book.isbn ?: "Không có ISBN"
        tvPublishedYear.text = book.publishedYear?.toString() ?: "Không có năm xuất bản"

        // Set availability status based on actual data
        if (book.available == true) {
            chipAvailability.text = "Có sẵn"
            chipAvailability.setChipBackgroundColorResource(R.color.purple_700)
            btnBorrow.isEnabled = true
        } else {
            chipAvailability.text = "Không có sẵn"
            chipAvailability.setChipBackgroundColorResource(R.color.error)
            btnBorrow.isEnabled = false
        }

        // Show edit button for all users (temporarily removed admin check)
        btnEdit.visibility = View.VISIBLE
        
        // Show reviews button for all users
        btnReviews.visibility = View.VISIBLE
        
        // Update toolbar title
        supportActionBar?.title = book.title ?: "Chi tiết sách"
    }

    private fun borrowBook() {
        if (currentBook == null) {
            Toast.makeText(this, "Không có thông tin sách", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentBook?.available != true) {
            Toast.makeText(this, "Sách này hiện không có sẵn", Toast.LENGTH_SHORT).show()
            return
        }

        if (bookId == null) {
            Toast.makeText(this, "Không thể mượn sách", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Create loan request with proper data structure
                val borrowRequest = BorrowRequest(
                    bookId = bookId!!,
                    dueDate = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date(System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000)) // 14 days from now
                )

                withContext(Dispatchers.IO) {
                    apiService.addLoan(borrowRequest)
                }

                Toast.makeText(this@BookDetailActivity, "Mượn sách thành công! Hạn trả: 14 ngày", Toast.LENGTH_LONG).show()
                
                // Update book availability
                currentBook?.let { book ->
                    val updatedBook = book.copy(available = false)
                    currentBook = updatedBook
                    displayBook(updatedBook)
                }
                
            } catch (e: Exception) {
                Toast.makeText(this@BookDetailActivity, "Lỗi khi mượn sách: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editBook() {
        showEditBookDialog()
    }

    private fun showEditBookDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_book, null)
        
        val etTitle = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEditTitle)
        val etAuthor = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEditAuthor)
        val etCategory = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEditCategory)
        val etDescription = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEditDescription)
        val etIsbn = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEditIsbn)
        val etPublishedYear = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEditPublishedYear)
        
        // Fill current data
        etTitle.setText(currentBook?.title ?: "")
        etAuthor.setText(currentBook?.author ?: "")
        etCategory.setText(currentBook?.category ?: "")
        etDescription.setText(currentBook?.description ?: "")
        etIsbn.setText(currentBook?.isbn ?: "")
        etPublishedYear.setText(currentBook?.publishedYear?.toString() ?: "")
        
        AlertDialog.Builder(this)
            .setTitle("Chỉnh sửa thông tin sách")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                // Update book data via API
                updateBook(
                    etTitle.text.toString(),
                    etAuthor.text.toString(),
                    etCategory.text.toString(),
                    etDescription.text.toString(),
                    etIsbn.text.toString(),
                    etPublishedYear.text.toString()
                )
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updateBook(title: String, author: String, category: String, description: String, isbn: String, publishedYear: String) {
        if (bookId == null) {
            Toast.makeText(this, "Không có ID sách để cập nhật", Toast.LENGTH_SHORT).show()
            return
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                android.util.Log.d("BookDetailActivity", "Updating book with ID: $bookId")
                val updatedBook = currentBook?.copy(
                    title = title,
                    author = author,
                    category = category,
                    description = description,
                    isbn = isbn,
                    publishedYear = publishedYear.toIntOrNull()
                )
                
                if (updatedBook != null) {
                    val response = withContext(Dispatchers.IO) {
                        apiService.updateBook(bookId!!, updatedBook)
                    }
                    
                    val result = response.book
                    if (result != null) {
                        currentBook = result
                        displayBook(result)
                        Toast.makeText(this@BookDetailActivity, "Đã cập nhật thông tin sách", Toast.LENGTH_SHORT).show()
                        android.util.Log.d("BookDetailActivity", "Book updated successfully: ${result.title}")
                    } else {
                        Toast.makeText(this@BookDetailActivity, "Không thể cập nhật thông tin sách", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@BookDetailActivity, "Không thể tạo dữ liệu sách để cập nhật", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("BookDetailActivity", "Error updating book: ${e.message}", e)
                Toast.makeText(this@BookDetailActivity, "Lỗi khi cập nhật sách: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showDeleteBookDialog() {
        AlertDialog.Builder(this)
            .setTitle("Xóa sách")
            .setMessage("Bạn có chắc chắn muốn xóa sách \"${currentBook?.title}\"? Hành động này không thể hoàn tác.")
            .setPositiveButton("Xóa") { _, _ ->
                deleteBook()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteBook() {
        if (bookId == null) return
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    apiService.deleteBook(bookId!!)
                }
                
                Toast.makeText(this@BookDetailActivity, "Đã xóa sách \"${currentBook?.title}\"", Toast.LENGTH_SHORT).show()
                finish() // Close the detail activity
            } catch (e: Exception) {
                Toast.makeText(this@BookDetailActivity, "Lỗi khi xóa sách: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun viewReviews() {
        if (bookId != null) {
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("book_id", bookId)
            intent.putExtra("book_title", currentBook?.title ?: "Sách")
            startActivity(intent)
        } else {
            Toast.makeText(this, "Không thể xem đánh giá sách", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh reviews when returning from ReviewActivity
        loadBookReviews()
    }

    private fun loadBookReviews() {
        if (bookId == null) {
            android.util.Log.d("BookDetailActivity", "No book ID for reviews")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                android.util.Log.d("BookDetailActivity", "Loading reviews for book ID: $bookId")
                val response = withContext(Dispatchers.IO) {
                    apiService.getBookReviews(bookId!!)
                }
                reviews = response.reviews ?: emptyList()
                android.util.Log.d("BookDetailActivity", "Loaded ${reviews.size} reviews")
                updateReviewSummary()
            } catch (e: Exception) {
                android.util.Log.e("BookDetailActivity", "Error loading reviews: ${e.message}", e)
                Toast.makeText(this@BookDetailActivity, "Không thể tải đánh giá: ${e.message}", Toast.LENGTH_SHORT).show()
                // Set empty reviews
                reviews = emptyList()
                updateReviewSummary()
            }
        }
    }


    private fun updateReviewSummary() {
        if (reviews.isNotEmpty()) {
            val averageRating = reviews.mapNotNull { it.rating }.average()
            val totalReviews = reviews.size
            
            // Update button text to show review summary
            btnReviews.text = "Đánh giá (${String.format("%.1f", averageRating)}/5 - ${totalReviews} đánh giá)"
            
            // Update text views if they exist
            tvReviewSummary?.text = "Điểm TB: ${String.format("%.1f", averageRating)}/5 (${totalReviews} đánh giá)"
            
            val recentReviews = reviews.take(2)
            val recentReviewsText = recentReviews.joinToString("\n") { review ->
                "• ${review.user?.name}: ${review.rating}⭐ - ${review.comment?.take(50)}${if (review.comment?.length ?: 0 > 50) "..." else ""}"
            }
            tvRecentReviews?.text = recentReviewsText
            
        } else {
            btnReviews.text = "Đánh giá (Chưa có đánh giá)"
            tvReviewSummary?.text = "Chưa có đánh giá nào"
            tvRecentReviews?.text = ""
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}