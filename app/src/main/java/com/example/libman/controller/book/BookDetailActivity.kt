package com.example.libman.controller.book

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.libman.R
import com.example.libman.controller.review.ReviewActivity
import com.example.libman.models.Book
import com.example.libman.models.Loan
import com.example.libman.models.BorrowRequest
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.example.libman.utils.TokenManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
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

    private var bookId: String? = null
    private var currentBook: Book? = null

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
        if (bookId != null) {
            val intent = Intent(this, EditBookActivity::class.java)
            intent.putExtra("book_id", bookId)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Không thể chỉnh sửa sách mẫu", Toast.LENGTH_SHORT).show()
        }
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