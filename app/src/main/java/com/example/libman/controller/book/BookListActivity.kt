package com.example.libman.controller.book

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.libman.R
import com.example.libman.adapters.BookAdapter
import com.example.libman.controller.book.AddBookActivity
import com.example.libman.models.Book
import com.google.android.material.appbar.MaterialToolbar

class BookListActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvBooks: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private var books: List<Book> = emptyList()
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)

        initViews()
        setupToolbar()
        setupRecyclerView()
        loadData()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        rvBooks = findViewById(R.id.rvBooks)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupRecyclerView() {
        rvBooks.layoutManager = GridLayoutManager(this, 2)
        bookAdapter = BookAdapter(books) { book ->
            val intent = Intent(this, BookDetailActivity::class.java)
            intent.putExtra("book_id", book.id)
            intent.putExtra("book_title", book.title)
            startActivity(intent)
        }
        rvBooks.adapter = bookAdapter
    }

    private fun loadData() {
        title = intent.getStringExtra("title") ?: "Danh sách sách"
        books = intent.getParcelableArrayListExtra<Book>("books") ?: emptyList()
        
        supportActionBar?.title = title
        bookAdapter.updateBooks(books)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.book_list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_book_menu -> {
                showToolbarMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showToolbarMenu() {
        val options = arrayOf("Thêm sách")
        
        AlertDialog.Builder(this)
            .setTitle("Sách")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Add book
                        startActivity(Intent(this, AddBookActivity::class.java))
                    }
                }
            }
            .show()
    }
}
