package com.example.libman.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.libman.R
import com.example.libman.models.Book
import com.google.android.material.chip.Chip

class BookAdapter(
    private var books: List<Book>,
    private val onItemClick: (Book) -> Unit = {}
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val author: TextView = itemView.findViewById(R.id.tvAuthor)
        val description: TextView = itemView.findViewById(R.id.tvDescription)
        val rating: TextView = itemView.findViewById(R.id.tvBookRating)
        val statusContainer: Chip = itemView.findViewById(R.id.statusContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.title.text = book.title ?: "Không có tiêu đề"
        holder.author.text = book.author ?: "Không có tác giả"
        holder.description.text = book.category ?: "Không có thể loại"
        holder.rating.text = "4.5" // Placeholder rating
        
        if (book.available == true) {
            holder.statusContainer.text = "Có sẵn"
            holder.statusContainer.setChipBackgroundColorResource(R.color.primary)
        } else {
            holder.statusContainer.text = "Đã mượn"
            holder.statusContainer.setChipBackgroundColorResource(R.color.error)
        }
        
        holder.itemView.setOnClickListener {
            onItemClick(book)
        }
    }

    override fun getItemCount(): Int = books.size

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
}
