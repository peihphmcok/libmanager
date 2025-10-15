package com.example.libman.controller.author

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.libman.R
import com.example.libman.models.Author
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textview.MaterialTextView

class AuthorDetailActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvAuthorName: MaterialTextView
    private lateinit var tvAuthorBio: MaterialTextView
    private lateinit var tvAuthorBirthYear: MaterialTextView
    private lateinit var tvAuthorNationality: MaterialTextView
    private lateinit var tvAuthorBooksCount: MaterialTextView

    private var author: Author? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_author_detail)

        initViews()
        setupToolbar()
        loadAuthorData()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tvAuthorName = findViewById(R.id.tvAuthorName)
        tvAuthorBio = findViewById(R.id.tvAuthorBio)
        tvAuthorBirthYear = findViewById(R.id.tvAuthorBirthYear)
        tvAuthorNationality = findViewById(R.id.tvAuthorNationality)
        tvAuthorBooksCount = findViewById(R.id.tvAuthorBooksCount)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun loadAuthorData() {
        // Get author data from intent
        val authorName = intent.getStringExtra("author_name")
        val authorBio = intent.getStringExtra("author_bio")
        val authorBirthYear = intent.getIntExtra("author_birth_year", 0)
        val authorNationality = intent.getStringExtra("author_nationality")
        val authorBooksCount = intent.getIntExtra("author_books_count", 0)

        // Display author information
        tvAuthorName.text = authorName ?: "Không có tên"
        tvAuthorBio.text = authorBio ?: "Không có tiểu sử"
        tvAuthorBirthYear.text = "Năm sinh: ${if (authorBirthYear > 0) authorBirthYear.toString() else "Không rõ"}"
        tvAuthorNationality.text = "Quốc tịch: ${authorNationality ?: "Không rõ"}"
        tvAuthorBooksCount.text = "Số sách: $authorBooksCount"

        supportActionBar?.title = authorName ?: "Chi tiết tác giả"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
