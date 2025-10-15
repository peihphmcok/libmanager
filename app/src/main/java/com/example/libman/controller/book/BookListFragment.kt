package com.example.libman.controller.book

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingLayout: View
    private lateinit var emptyLayout: View
    private lateinit var searchView: SearchView
    private lateinit var fabAddBook: FloatingActionButton

    private var allBooks: List<Book> = emptyList()
    private var adapter: BookAdapter? = null
    private lateinit var apiService: ApiService

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
        apiService = ApiClient.getRetrofit(requireContext()).create(ApiService::class.java)

        setupSearch()
        setupFab()
        setupAdapter()
        fetchBooks()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from AddBookActivity
        // Add a delay to ensure the activity has finished
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            kotlinx.coroutines.delay(500) // Increased delay
            android.util.Log.d("BookListFragment", "onResume: Refreshing books data")
            fetchBooks()
        }
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

    private fun setupAdapter() {
        adapter = BookAdapter(allBooks) { book ->
            // Handle book click - show context menu
            showBookContextMenu(book)
        }
        recyclerView.adapter = adapter
    }

    private fun showBookContextMenu(book: Book) {
        val options = arrayOf("Xem chi tiết", "Chỉnh sửa", "Xóa")
        
        AlertDialog.Builder(requireContext())
            .setTitle(book.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // View details - navigate to book detail
                        val intent = Intent(requireContext(), BookDetailActivity::class.java)
                        intent.putExtra("book", book)
                        startActivity(intent)
                    }
                    1 -> {
                        // Edit book
                        val intent = Intent(requireContext(), UpdateBookActivity::class.java)
                        intent.putExtra("book_id", book.id)
                        intent.putExtra("book", book)
                        startActivity(intent)
                    }
                    2 -> {
                        // Delete book
                        showDeleteConfirmation(book)
                    }
                }
            }
            .show()
    }

    private fun showDeleteConfirmation(book: Book) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa sách \"${book.title}\"?")
            .setPositiveButton("Xóa") { _, _ ->
                deleteBook(book)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteBook(book: Book) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    apiService.deleteBook(book.id!!)
                }
                
                android.util.Log.d("BookListFragment", "Book deleted successfully: ${book.title}")
                fetchBooks() // Refresh the list
                
            } catch (e: Exception) {
                android.util.Log.e("BookListFragment", "Error deleting book: ${e.message}", e)
            }
        }
    }

    private fun fetchBooks() {
        showLoading(true)

        // Use coroutines with lifecycleScope for suspend ApiService
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            try {
                android.util.Log.d("BookListFragment", "fetchBooks: Starting API call")
                val response = apiService.getBooks()
                val books = response.books ?: emptyList()
                allBooks = books
                bindBooks(books)
                // Log success
                android.util.Log.d("BookListFragment", "API Success: Loaded ${books.size} books")
                android.util.Log.d("BookListFragment", "First book: ${books.firstOrNull()?.title}")
            } catch (e: Exception) {
                // Log error details
                android.util.Log.e("BookListFragment", "API Error: ${e.message}", e)
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