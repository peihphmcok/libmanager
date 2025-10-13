package com.example.libman.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.LinearLayout
import android.view.View as AndroidView
import androidx.recyclerview.widget.RecyclerView
import com.example.libman.R
import com.example.libman.models.Book

class BookAdapter(private val books: List<Book>) :
    RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val author: TextView = itemView.findViewById(R.id.tvAuthor)
        val description: TextView = itemView.findViewById(R.id.tvDescription)
        val available: TextView = itemView.findViewById(R.id.tvAvailable)
        val statusContainer: LinearLayout = itemView.findViewById(R.id.statusContainer)
        val statusIndicator: AndroidView = itemView.findViewById(R.id.statusIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.title.text = book.title
        holder.author.text = book.author
        holder.description.text = book.description
        val ctx = holder.itemView.context
        if (book.available) {
            holder.available.text = ctx.getString(R.string.available_yes)
            holder.statusContainer.setBackgroundResource(R.drawable.bg_status_available)
            holder.statusIndicator.setBackgroundResource(R.drawable.circle_green)
        } else {
            holder.available.text = ctx.getString(R.string.available_no)
            holder.statusContainer.setBackgroundResource(R.drawable.bg_status_unavailable)
            holder.statusIndicator.setBackgroundResource(R.drawable.circle_red)
        }
    }

    override fun getItemCount(): Int = books.size
}
