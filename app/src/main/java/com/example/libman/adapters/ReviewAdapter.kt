package com.example.libman.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.libman.R
import com.example.libman.models.Review
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class ReviewAdapter(
    private var reviews: List<Review>,
    private val onEditClick: (Review) -> Unit,
    private val onDeleteClick: (Review) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardReview)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val tvComment: TextView = itemView.findViewById(R.id.tvComment)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        
        holder.tvRating.text = "${review.rating ?: 0}/5"
        holder.ratingBar.rating = (review.rating ?: 0).toFloat()
        holder.tvComment.text = review.comment ?: "Không có bình luận"
        
        // Format date
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.tvDate.text = review.createdAt ?: "Không xác định"
        
        // Set click listeners
        holder.btnEdit.setOnClickListener {
            onEditClick(review)
        }
        
        holder.btnDelete.setOnClickListener {
            onDeleteClick(review)
        }
        
        // Show/hide edit/delete buttons based on review ID
        val hasId = !review.id.isNullOrEmpty()
        holder.btnEdit.visibility = if (hasId) View.VISIBLE else View.GONE
        holder.btnDelete.visibility = if (hasId) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = reviews.size

    fun updateReviews(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}
