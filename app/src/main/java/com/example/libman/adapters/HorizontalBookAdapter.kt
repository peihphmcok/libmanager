package com.example.libman.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.libman.R
import com.example.libman.models.Book
import com.google.android.material.chip.Chip

class HorizontalBookAdapter(
    private val books: List<Book>,
    private val onBookClick: (Book) -> Unit,
    private val onBookLongClick: ((Book) -> Unit)? = null,
    private val isSelectionMode: Boolean = false,
    private val selectedBooks: Set<String> = emptySet()
) : RecyclerView.Adapter<HorizontalBookAdapter.BookViewHolder>() {

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivBookCover: ImageView = itemView.findViewById(R.id.ivBookCover)
        val tvBookTitle: TextView = itemView.findViewById(R.id.tvBookTitle)
        val tvBookAuthor: TextView = itemView.findViewById(R.id.tvBookAuthor)
        val tvBookRating: TextView = itemView.findViewById(R.id.tvBookRating)
        val chipAvailability: Chip = itemView.findViewById(R.id.chipAvailability)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book_horizontal, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        
        holder.tvBookTitle.text = book.title ?: "Không có tiêu đề"
        holder.tvBookAuthor.text = book.author ?: "Không có tác giả"
        
        // Set rating (mock data for now)
        holder.tvBookRating.text = "4.5 (12)"
        
        // Set availability
        if (book.available == true) {
            holder.chipAvailability.text = "Có sẵn"
            holder.chipAvailability.setChipBackgroundColorResource(R.color.primary)
        } else {
            holder.chipAvailability.text = "Đã mượn"
            holder.chipAvailability.setChipBackgroundColorResource(R.color.text_secondary)
        }
        
        // Set book cover (placeholder for now)
        holder.ivBookCover.setImageResource(R.drawable.ic_book_placeholder)
        
        // Handle selection mode
        val isSelected = selectedBooks.contains(book.id)
        if (isSelectionMode) {
            holder.itemView.setBackgroundColor(
                if (isSelected) holder.itemView.context.getColor(R.color.error) 
                else holder.itemView.context.getColor(android.R.color.transparent)
            )
        } else {
            holder.itemView.setBackgroundColor(holder.itemView.context.getColor(android.R.color.transparent))
        }
        
        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                // Toggle selection
                onBookLongClick?.invoke(book)
            } else {
                onBookClick(book)
            }
        }
        
        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) {
                // Enter selection mode
                onBookLongClick?.invoke(book)
                true
            } else {
                false
            }
        }
    }

    override fun getItemCount(): Int = books.size

    fun updateBooks(newBooks: List<Book>) {
        // This would be used to update the adapter with new data
        // For now, we'll recreate the adapter when data changes
    }
}
