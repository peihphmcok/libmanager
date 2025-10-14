package com.example.libman.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.libman.R
import com.example.libman.models.Author

class AuthorAdapter(
    private val authors: List<Author>,
    private val onItemClick: (Author) -> Unit = {}
) : RecyclerView.Adapter<AuthorAdapter.AuthorViewHolder>() {

    class AuthorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvAuthorName)
        val bio: TextView = itemView.findViewById(R.id.tvAuthorBio)
        val extra: TextView = itemView.findViewById(R.id.tvAuthorExtra)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuthorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_author, parent, false)
        return AuthorViewHolder(view)
    }

    override fun onBindViewHolder(holder: AuthorViewHolder, position: Int) {
        val a = authors[position]
        holder.name.text = a.name ?: "Không có tên"
        holder.bio.text = a.bio ?: "Không có thông tin"
        val national = a.nationality ?: "Không xác định"
        val birth = a.birthYear?.toString() ?: "—"
        holder.extra.text = "$national • $birth"
        
        holder.itemView.setOnClickListener {
            onItemClick(a)
        }
    }

    override fun getItemCount(): Int = authors.size
}