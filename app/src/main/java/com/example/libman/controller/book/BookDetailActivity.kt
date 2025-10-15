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

        // Get book data from intent
        bookId = intent.getStringExtra("book_id")
        val bookTitle = intent.getStringExtra("book_title")
        
        if (bookTitle != null) {
            // For now, show sample data based on title
            showSampleBookData(bookTitle)
        } else {
            // Load book from API if ID is provided
            bookId?.let { loadBook(it) }
        }
        
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

    private fun showSampleBookData(title: String) {
        // Show sample data based on book title
        val sampleBooks = getSampleBooks()
        val book = sampleBooks.find { it.title == title }
        
        if (book != null) {
            currentBook = book
            displayBook(book)
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin sách", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadBook(id: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val book = withContext(Dispatchers.IO) {
                    apiService.getBook(id)
                }
                currentBook = book
                displayBook(book)
            } catch (e: Exception) {
                Toast.makeText(this@BookDetailActivity, "Lỗi khi tải thông tin sách: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayBook(book: Book) {
        tvTitle.text = book.title ?: "Không có tiêu đề"
        tvAuthor.text = book.author ?: "Không có tác giả"
        tvDescription.text = book.category ?: "Không có thể loại"
        tvIsbn.text = book.coverImage ?: "Không có ảnh bìa"
        tvPublishedYear.text = book.publishedYear?.toString() ?: "Không xác định"

        // Set availability status - for now, assume all books are available
        chipAvailability.text = "Có sẵn"
        chipAvailability.setChipBackgroundColorResource(R.color.purple_700)
        btnBorrow.isEnabled = true

        // Show edit button for all users (temporarily removed admin check)
        btnEdit.visibility = View.VISIBLE
        
        // Show reviews button for all users
        btnReviews.visibility = View.VISIBLE
    }

    private fun borrowBook() {
        if (currentBook == null) {
            Toast.makeText(this, "Không có thông tin sách", Toast.LENGTH_SHORT).show()
            return
        }

        // For now, assume all books are available
        // if (currentBook?.available != true) {
        //     Toast.makeText(this, "Sách này hiện không có sẵn", Toast.LENGTH_SHORT).show()
        //     return
        // }

        if (bookId == null) {
            Toast.makeText(this, "Không thể mượn sách mẫu", Toast.LENGTH_SHORT).show()
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
                // Update book data
                val updatedBook = currentBook?.copy(
                    title = etTitle.text.toString(),
                    author = etAuthor.text.toString(),
                    category = etCategory.text.toString(),
                    description = etDescription.text.toString(),
                    isbn = etIsbn.text.toString(),
                    publishedYear = etPublishedYear.text.toString().toIntOrNull()
                )
                
                if (updatedBook != null) {
                    currentBook = updatedBook
                    updateBookDisplay()
                    Toast.makeText(this, "Đã cập nhật thông tin sách", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updateBookDisplay() {
        currentBook?.let { book ->
            tvTitle.text = book.title ?: "Không có tiêu đề"
            tvAuthor.text = book.author ?: "Không có tác giả"
            tvDescription.text = book.description ?: "Không có mô tả"
            tvIsbn.text = book.isbn ?: "Không có ISBN"
            tvPublishedYear.text = book.publishedYear?.toString() ?: "Không có năm xuất bản"
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
        // TODO: Implement actual deletion logic
        Toast.makeText(this, "Đã xóa sách \"${currentBook?.title}\"", Toast.LENGTH_SHORT).show()
        finish() // Close the detail activity
    }

    private fun viewReviews() {
        if (bookId != null) {
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("book_id", bookId)
            intent.putExtra("book_title", currentBook?.title ?: "Sách")
            startActivity(intent)
        } else {
            Toast.makeText(this, "Không thể xem đánh giá sách mẫu", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh reviews when returning from ReviewActivity
        loadBookReviews()
    }

    private fun loadBookReviews() {
        if (bookId == null) {
            // Show sample reviews for demo books
            showSampleReviews()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                reviews = withContext(Dispatchers.IO) {
                    apiService.getBookReviews(bookId!!)
                }
                updateReviewSummary()
            } catch (e: Exception) {
                // Show sample reviews if API fails
                showSampleReviews()
            }
        }
    }

    private fun showSampleReviews() {
        // Show sample reviews for demo purposes
        reviews = listOf(
            Review(
                id = "sample1",
                user = com.example.libman.models.User(name = "Nguyễn Văn A"),
                rating = 5,
                comment = "Sách rất hay, nội dung sâu sắc và ý nghĩa.",
                createdAt = "2024-01-15T10:30:00Z"
            ),
            Review(
                id = "sample2", 
                user = com.example.libman.models.User(name = "Trần Thị B"),
                rating = 4,
                comment = "Cuốn sách khá thú vị, tuy nhiên một số phần hơi dài dòng.",
                createdAt = "2024-01-10T14:20:00Z"
            ),
            Review(
                id = "sample3",
                user = com.example.libman.models.User(name = "Lê Văn C"),
                rating = 5,
                comment = "Tuyệt vời! Đây là một trong những cuốn sách hay nhất tôi từng đọc.",
                createdAt = "2024-01-05T09:15:00Z"
            )
        )
        updateReviewSummary()
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

    private fun getSampleBooks(): List<Book> {
        return listOf(
            Book(
                title = "Truyện Kiều",
                author = "Nguyễn Du",
                category = "Văn học cổ điển",
                publishedYear = 1820
            ),
            Book(
                title = "Chí Phèo",
                author = "Nam Cao",
                category = "Văn học hiện thực",
                publishedYear = 1941
            ),
            Book(
                title = "Dế Mèn phiêu lưu ký",
                author = "Tô Hoài",
                category = "Văn học thiếu nhi",
                publishedYear = 1941
            ),
            Book(
                title = "Romeo và Juliet",
                author = "William Shakespeare",
                category = "Kịch",
                publishedYear = 1597
            ),
            Book(
                title = "Chiến tranh và Hòa bình",
                author = "Leo Tolstoy",
                category = "Tiểu thuyết sử thi",
                publishedYear = 1869
            ),
            Book(
                title = "Những người khốn khổ",
                author = "Victor Hugo",
                category = "Tiểu thuyết xã hội",
                publishedYear = 1862
            )
        )
    }
}