package com.example.libman.controller.book

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
                val response = apiService.getBooks()
                allBooks = response.books ?: emptyList()
                bindBooks()
            } catch (e: Exception) {
                // Use sample data if API fails
                allBooks = getSampleBooks()
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
                        val intent = Intent(requireContext(), BookDetailActivity::class.java)
                        intent.putExtra("book_id", book.id)
                        intent.putExtra("book_title", book.title)
                        startActivity(intent)
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

    private fun getSampleBooks(): List<Book> {
        return listOf(
            Book(id = "1", title = "Truyện Kiều", author = "Nguyễn Du", category = "Văn học", available = true),
            Book(id = "2", title = "Chí Phèo", author = "Nam Cao", category = "Văn học", available = true),
            Book(id = "3", title = "Dế Mèn phiêu lưu ký", author = "Tô Hoài", category = "Văn học", available = false),
            Book(id = "4", title = "Romeo và Juliet", author = "William Shakespeare", category = "Kịch", available = true),
            Book(id = "5", title = "Chiến tranh và Hòa bình", author = "Leo Tolstoy", category = "Tiểu thuyết", available = true),
            Book(id = "6", title = "Lịch sử Việt Nam", author = "Trần Trọng Kim", category = "Lịch sử", available = true),
            Book(id = "7", title = "Vật lý cơ bản", author = "Nguyễn Văn A", category = "Khoa học", available = false),
            Book(id = "8", title = "Toán học cao cấp", author = "Lê Văn B", category = "Khoa học", available = true)
        )
    }
}
