package com.example.libman.controller.review

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.libman.R
import com.example.libman.models.Review
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.example.libman.utils.TokenManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.widget.RatingBar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddReviewDialogFragment : DialogFragment() {

    private lateinit var ratingBar: RatingBar
    private lateinit var etComment: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var apiService: ApiService
    private lateinit var tokenManager: TokenManager
    
    var onReviewAdded: ((Review) -> Unit)? = null
    
    private var bookId: String? = null

    companion object {
        fun newInstance(bookId: String): AddReviewDialogFragment {
            val fragment = AddReviewDialogFragment()
            val args = Bundle()
            args.putString("book_id", bookId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookId = arguments?.getString("book_id")
        apiService = ApiClient.getRetrofit(requireContext()).create(ApiService::class.java)
        tokenManager = TokenManager(requireContext())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_add_review, null)
        
        initViews(view)
        setupClickListeners()
        
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Thêm đánh giá")
            .setView(view)
            .create()
    }

    private fun initViews(view: android.view.View) {
        ratingBar = view.findViewById(R.id.ratingBar)
        etComment = view.findViewById(R.id.etComment)
        btnSave = view.findViewById(R.id.btnSave)
        btnCancel = view.findViewById(R.id.btnCancel)
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            saveReview()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun saveReview() {
        val rating = ratingBar.rating.toInt()
        val comment = etComment.text.toString().trim()

        android.util.Log.d("AddReviewDialog", "Saving review - Rating: $rating, Comment: $comment, BookId: $bookId")

        if (rating == 0) {
            Toast.makeText(requireContext(), "Vui lòng chọn điểm đánh giá", Toast.LENGTH_SHORT).show()
            return
        }

        if (bookId == null) {
            Toast.makeText(requireContext(), "Không có ID sách", Toast.LENGTH_SHORT).show()
            return
        }

        // Debug user info
        tokenManager.debugUserInfo()
        
        val userId = tokenManager.getUserId()
        android.util.Log.d("AddReviewDialog", "Retrieved user ID: $userId")
        
        // Check if user is logged in
        if (userId == null) {
            android.util.Log.e("AddReviewDialog", "User ID is null - user not logged in properly")
            
            // Show more detailed error message
            val token = tokenManager.getToken()
            val userName = tokenManager.getUserName()
            val isLoggedIn = tokenManager.isLoggedIn()
            
            val errorMsg = "Debug Info:\n" +
                    "Token: ${if (token != null) "Có (${token.substring(0, 20)}...)" else "Không"}\n" +
                    "User ID: ${userId ?: "Null"}\n" +
                    "User Name: ${userName ?: "Null"}\n" +
                    "Is Logged In: $isLoggedIn\n\n" +
                    "Vui lòng đăng nhập lại!\n\n" +
                    "Tip: Thử force logout và login lại"
            
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
            
            // Auto force logout after showing error
            android.util.Log.d("AddReviewDialog", "Auto force logout due to missing user ID")
            tokenManager.forceLogout()
            return
        }
        
        // Check if token exists
        val token = tokenManager.getToken()
        if (token == null) {
            android.util.Log.e("AddReviewDialog", "Token is null")
            Toast.makeText(requireContext(), "Không tìm thấy token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show()
            return
        }
        
        // Additional check: verify user data consistency
        val userName = tokenManager.getUserName()
        if (userName == null) {
            android.util.Log.e("AddReviewDialog", "User name is null - data inconsistency")
            Toast.makeText(requireContext(), "Dữ liệu người dùng không nhất quán. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show()
            return
        }

        val review = Review(
            rating = rating,
            comment = comment.ifEmpty { null }
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                btnSave.isEnabled = false
                btnSave.text = "Đang lưu..."
                
                android.util.Log.d("AddReviewDialog", "Calling API to add review")
                val response = withContext(Dispatchers.IO) {
                    apiService.addBookReview(bookId!!, review)
                }
                
                val savedReview = response.review
                if (savedReview != null) {
                    android.util.Log.d("AddReviewDialog", "Review saved successfully: ${savedReview.id}")
                    onReviewAdded?.invoke(savedReview)
                } else {
                    Toast.makeText(requireContext(), "Không thể lưu đánh giá", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(requireContext(), "Thêm đánh giá thành công!", Toast.LENGTH_SHORT).show()
                dismiss()
                
            } catch (e: Exception) {
                android.util.Log.e("AddReviewDialog", "Error saving review: ${e.message}", e)
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                btnSave.isEnabled = true
                btnSave.text = "Lưu"
            }
        }
    }
}
