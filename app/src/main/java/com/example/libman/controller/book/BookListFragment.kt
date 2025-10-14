package com.example.libman.controller.book

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.libman.R
import com.example.libman.adapters.BookAdapter
import com.example.libman.models.Book
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.libman.utils.TokenManager
import com.example.libman.utils.VietnameseUtils
import com.example.libman.utils.TestDataGenerator

class BookListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingLayout: View
    private lateinit var emptyLayout: View
    private lateinit var searchView: SearchView
    private lateinit var fabAddBook: FloatingActionButton

    private var allBooks: List<Book> = emptyList()
    private var adapter: BookAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_list, container, false)

        recyclerView = view.findViewById(R.id.rvBooks)
        loadingLayout = view.findViewById(R.id.loadingLayout)
        emptyLayout = view.findViewById(R.id.emptyLayout)
        searchView = view.findViewById(R.id.svBooks)
        fabAddBook = view.findViewById(R.id.fabAddBook)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        setupSearch()
        setupFab()
        fetchBooks()

        return view
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterBooks(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterBooks(newText)
                return true
            }
        })
    }

    private fun setupFab() {
        // Show add book button for all users (temporarily removed admin check)
        fabAddBook.visibility = View.VISIBLE
        fabAddBook.setOnClickListener {
            startActivity(Intent(requireContext(), AddBookActivity::class.java))
        }
    }

    private fun fetchBooks() {
        showLoading(true)
        val api = ApiClient.getRetrofit(requireContext()).create(ApiService::class.java)

        // Use coroutines with lifecycleScope for suspend ApiService
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            try {
                val books = api.getBooks()
                allBooks = books
                bindBooks(books)
            } catch (e: Exception) {
                // Fallback to sample data if API fails
                allBooks = TestDataGenerator.generateSampleBooks()
                bindBooks(allBooks)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun bindBooks(books: List<Book>) {
        if (books.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyLayout.visibility = View.VISIBLE
        } else {
            emptyLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter = BookAdapter(books) { book ->
                // Navigate to book detail
                val intent = Intent(requireContext(), BookDetailActivity::class.java)
                intent.putExtra("book_id", book.id)
                intent.putExtra("book_title", book.title)
                startActivity(intent)
            }
            recyclerView.adapter = adapter
        }
    }

    private fun filterBooks(query: String?) {
        val trimmed = query?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            bindBooks(allBooks)
            return
        }
        val filtered = allBooks.filter { b ->
            VietnameseUtils.matchesVietnamese(b.title, trimmed) || 
            VietnameseUtils.matchesVietnamese(b.author, trimmed)
        }
        bindBooks(filtered)
    }

    private fun showLoading(show: Boolean) {
        loadingLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

}