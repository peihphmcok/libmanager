package com.example.libman.controller.book

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
        val role = TokenManager(requireContext()).getRole()
        val isAdmin = role == "admin"
        fabAddBook.visibility = if (isAdmin) View.VISIBLE else View.GONE
        if (isAdmin) {
            fabAddBook.setOnClickListener {
                startActivity(android.content.Intent(requireContext(), AddBookActivity::class.java))
            }
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
                allBooks = emptyList()
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
            adapter = BookAdapter(books)
            recyclerView.adapter = adapter
        }
    }

    private fun filterBooks(query: String?) {
        val trimmed = query?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            bindBooks(allBooks)
            return
        }
        val lower = trimmed.lowercase()
        val filtered = allBooks.filter { b ->
            b.title.lowercase().contains(lower) || b.author.lowercase().contains(lower)
        }
        bindBooks(filtered)
    }

    private fun showLoading(show: Boolean) {
        loadingLayout.visibility = if (show) View.VISIBLE else View.GONE
    }
}