package com.example.libman.controller.review

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.libman.R
import com.example.libman.models.Review
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
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

        if (rating == 0) {
            Toast.makeText(requireContext(), "Vui lòng chọn điểm đánh giá", Toast.LENGTH_SHORT).show()
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
                
                val savedReview = withContext(Dispatchers.IO) {
                    apiService.addBookReview(bookId!!, review)
                }
                
                onReviewAdded?.invoke(savedReview)
                Toast.makeText(requireContext(), "Thêm đánh giá thành công!", Toast.LENGTH_SHORT).show()
                dismiss()
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnSave.isEnabled = true
                btnSave.text = "Lưu"
            }
        }
    }
}
