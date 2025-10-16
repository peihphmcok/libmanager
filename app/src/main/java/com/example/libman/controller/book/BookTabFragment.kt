package com.example.libman.controller.book

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.libman.R
import com.example.libman.adapters.HorizontalBookAdapter
import com.example.libman.models.Book
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookTabFragment : Fragment() {

    private lateinit var rvFeaturedBooks: RecyclerView
    private lateinit var rvRecentBooks: RecyclerView
    private lateinit var rvCategoryBooks: RecyclerView
    private lateinit var loadingLayout: View
    private lateinit var emptyLayout: View
    private lateinit var apiService: ApiService

    private var category: String = ""
    private var allBooks: List<Book> = emptyList()
    
    // Selection mode support
    private var isSelectionMode = false
    private var selectedBooks = emptySet<String>()
    private var onBookSelectionListener: ((String, Boolean) -> Unit)? = null

    companion object {
        fun newInstance(category: String): BookTabFragment {
            val fragment = BookTabFragment()
            val args = Bundle()
            args.putString("category", category)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = arguments?.getString("category") ?: "Tất cả"
        apiService = ApiClient.getRetrofit(requireContext()).create(ApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_tab, container, false)
        
        initViews(view)
        setupRecyclerViews()
        loadBooks()
        
        // Set up selection listener
        onBookSelectionListener = { bookId, isSelected ->
            // Notify parent fragment about selection
            (parentFragment as? BookListFragment)?.onBookSelected(bookId, isSelected)
        }
        
        return view
    }

    private fun initViews(view: View) {
        rvFeaturedBooks = view.findViewById(R.id.rvFeaturedBooks)
        rvRecentBooks = view.findViewById(R.id.rvRecentBooks)
        rvCategoryBooks = view.findViewById(R.id.rvCategoryBooks)
        loadingLayout = view.findViewById(R.id.loadingLayout)
        emptyLayout = view.findViewById(R.id.emptyLayout)
    }

    private fun setupRecyclerViews() {
        rvFeaturedBooks.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvRecentBooks.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvCategoryBooks.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun loadBooks() {
        showLoading(true)
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                android.util.Log.d("BookTabFragment", "Loading books for category: $category")
                
                val response = apiService.getBooks()
                allBooks = response.books ?: emptyList()
                
                android.util.Log.d("BookTabFragment", "Books response: ${allBooks.size} books found for category: $category")
                android.util.Log.d("BookTabFragment", "Total books: ${response.total}")
                android.util.Log.d("BookTabFragment", "Current page: ${response.currentPage}")
                
                bindBooks()
            } catch (e: Exception) {
                android.util.Log.e("BookTabFragment", "Error loading books for category $category: ${e.message}", e)
                Toast.makeText(requireContext(), "Lỗi khi tải danh sách sách: ${e.message}", Toast.LENGTH_LONG).show()
                allBooks = emptyList()
                bindBooks()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun bindBooks() {
        if (allBooks.isEmpty()) {
            showEmpty(true)
            return
        }

        showEmpty(false)

        // Featured books (first 5)
        val featuredBooks = allBooks.take(5)
        setupRecyclerView(rvFeaturedBooks, featuredBooks)

        // Recent books (first 8)
        val recentBooks = allBooks.take(8)
        setupRecyclerView(rvRecentBooks, recentBooks)

        // Category books
        val categoryBooks = if (category == "Tất cả") {
            allBooks
        } else {
            allBooks.filter { it.category?.contains(category, ignoreCase = true) == true }
        }
        setupRecyclerView(rvCategoryBooks, categoryBooks)
    }

    private fun setupRecyclerView(recyclerView: RecyclerView, books: List<Book>) {
        if (books.isNotEmpty()) {
            recyclerView.visibility = View.VISIBLE
            val adapter = HorizontalBookAdapter(
                books = books,
                onBookClick = { book ->
                    if (!isSelectionMode) {
                        if (book.id != null) {
                            val intent = Intent(requireContext(), BookDetailActivity::class.java)
                            intent.putExtra("book_id", book.id)
                            intent.putExtra("book_title", book.title)
                            startActivity(intent)
                        } else {
                            Toast.makeText(requireContext(), "Không thể xem chi tiết sách này", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onBookLongClick = { book ->
                    book.id?.let { bookId ->
                        val isSelected = selectedBooks.contains(bookId)
                        onBookSelectionListener?.invoke(bookId, !isSelected)
                    }
                },
                isSelectionMode = isSelectionMode,
                selectedBooks = selectedBooks
            )
            recyclerView.adapter = adapter
        } else {
            recyclerView.visibility = View.GONE
        }
    }

    private fun showLoading(show: Boolean) {
        loadingLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmpty(show: Boolean) {
        emptyLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun updateSelectionMode(isSelectionMode: Boolean, selectedBooks: Set<String>) {
        this.isSelectionMode = isSelectionMode
        this.selectedBooks = selectedBooks
        bindBooks() // Refresh the adapters
    }
    
    fun refreshBooks() {
        loadBooks()
    }

}
