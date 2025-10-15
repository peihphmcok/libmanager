package com.example.libman.controller.review

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.libman.R
import com.example.libman.adapters.ReviewAdapter
import com.example.libman.models.Book
import com.example.libman.models.Review
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReviewActivity : AppCompatActivity() {

    private lateinit var tvBookTitle: MaterialTextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddReview: FloatingActionButton
    private lateinit var toolbar: Toolbar
    private lateinit var apiService: ApiService
    
    private var bookId: String? = null
    private var bookTitle: String? = null
    private var reviews: List<Review> = emptyList()
    private var adapter: ReviewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        bookId = intent.getStringExtra("book_id")
        bookTitle = intent.getStringExtra("book_title")
        
        if (bookId == null) {
            Toast.makeText(this, "Không tìm thấy ID sách", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupRecyclerView()
        setupToolbar()
        apiService = ApiClient.getRetrofit(this).create(ApiService::class.java)
        
        loadReviews()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tvBookTitle = findViewById(R.id.tvBookTitle)
        recyclerView = findViewById(R.id.rvReviews)
        fabAddReview = findViewById(R.id.fabAddReview)
        
        tvBookTitle.text = bookTitle ?: "Đánh giá sách"
        
        fabAddReview.setOnClickListener {
            showAddReviewDialog()
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Đánh giá sách"
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ReviewAdapter(
            reviews = reviews,
            onEditClick = { review -> editReview(review) },
            onDeleteClick = { review -> deleteReview(review) }
        )
        recyclerView.adapter = adapter
    }

    private fun loadReviews() {
        lifecycleScope.launch {
            try {
                reviews = withContext(Dispatchers.IO) {
                    apiService.getBookReviews(bookId!!)
                }
                // Sort reviews by creation date (newest first)
                reviews = reviews.sortedByDescending { it.createdAt }
                adapter?.updateReviews(reviews)
                
                // Show summary of reviews
                showReviewSummary()
            } catch (e: Exception) {
                Toast.makeText(this@ReviewActivity, "Lỗi khi tải đánh giá: ${e.message}", Toast.LENGTH_SHORT).show()
                // Show sample reviews if API fails
                showSampleReviews()
            }
        }
    }

    private fun showReviewSummary() {
        if (reviews.isNotEmpty()) {
            val averageRating = reviews.mapNotNull { it.rating }.average()
            val totalReviews = reviews.size
            val recentReviews = reviews.take(3)
            
            // Update toolbar subtitle with summary
            supportActionBar?.subtitle = "Điểm TB: ${String.format("%.1f", averageRating)}/5 (${totalReviews} đánh giá)"
        } else {
            supportActionBar?.subtitle = "Chưa có đánh giá nào"
        }
    }

    private fun showSampleReviews() {
        // Show sample reviews when API fails
        reviews = listOf(
            Review(
                id = "sample1",
                user = com.example.libman.models.User(name = "Nguyễn Văn A"),
                rating = 5,
                comment = "Sách rất hay, nội dung sâu sắc và ý nghĩa. Tôi rất thích cách tác giả xây dựng nhân vật.",
                createdAt = "2024-01-15T10:30:00Z"
            ),
            Review(
                id = "sample2", 
                user = com.example.libman.models.User(name = "Trần Thị B"),
                rating = 4,
                comment = "Cuốn sách khá thú vị, tuy nhiên một số phần hơi dài dòng. Nhìn chung là đáng đọc.",
                createdAt = "2024-01-10T14:20:00Z"
            ),
            Review(
                id = "sample3",
                user = com.example.libman.models.User(name = "Lê Văn C"),
                rating = 5,
                comment = "Tuyệt vời! Đây là một trong những cuốn sách hay nhất tôi từng đọc. Rất khuyến khích mọi người đọc.",
                createdAt = "2024-01-05T09:15:00Z"
            )
        )
        adapter?.updateReviews(reviews)
        showReviewSummary()
    }

    private fun showAddReviewDialog() {
        val dialog = AddReviewDialogFragment.newInstance(bookId!!)
        dialog.onReviewAdded = { review ->
            reviews = reviews + review
            // Sort reviews by creation date (newest first)
            reviews = reviews.sortedByDescending { it.createdAt }
            adapter?.updateReviews(reviews)
            showReviewSummary()
        }
        dialog.show(supportFragmentManager, "AddReviewDialog")
    }

    private fun editReview(review: Review) {
        val dialog = EditReviewDialogFragment.newInstance(bookId!!, review)
        dialog.onReviewUpdated = { updatedReview ->
            reviews = reviews.map { if (it.id == updatedReview.id) updatedReview else it }
            // Sort reviews by creation date (newest first)
            reviews = reviews.sortedByDescending { it.createdAt }
            adapter?.updateReviews(reviews)
            showReviewSummary()
        }
        dialog.show(supportFragmentManager, "EditReviewDialog")
    }

    private fun deleteReview(review: Review) {
        if (review.id == null) {
            Toast.makeText(this, "Không thể xóa đánh giá mẫu", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    apiService.deleteBookReview(bookId!!, review.id)
                }
                
                reviews = reviews.filter { it.id != review.id }
                adapter?.updateReviews(reviews)
                showReviewSummary()
                Toast.makeText(this@ReviewActivity, "Xóa đánh giá thành công!", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@ReviewActivity, "Lỗi khi xóa đánh giá: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
