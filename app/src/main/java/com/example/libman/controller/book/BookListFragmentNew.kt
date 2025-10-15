package com.example.libman.controller.book

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.libman.R
import com.example.libman.adapters.HorizontalBookAdapter
import com.example.libman.controller.book.AddBookActivity
import com.example.libman.models.Book
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookListFragmentNew : Fragment() {

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var etSearch: TextInputEditText
    private lateinit var loadingLayout: View
    private lateinit var emptyLayout: View
    
    // RecyclerViews
    private lateinit var rvFeaturedBooks: RecyclerView
    private lateinit var rvRecentBooks: RecyclerView
    private lateinit var rvLiteratureBooks: RecyclerView
    private lateinit var rvScienceBooks: RecyclerView
    private lateinit var rvHistoryBooks: RecyclerView
    
    // View All buttons
    private lateinit var btnViewAllFeatured: MaterialButton
    private lateinit var btnViewAllRecent: MaterialButton
    private lateinit var btnViewAllLiterature: MaterialButton
    private lateinit var btnViewAllScience: MaterialButton
    private lateinit var btnViewAllHistory: MaterialButton

    private var allBooks: List<Book> = emptyList()
    private lateinit var apiService: ApiService
    
    // Selection mode for deletion
    private var isSelectionMode = false
    private val selectedBooks = mutableSetOf<String>()
    
    companion object {
        private const val REQUEST_CODE_ADD_BOOK = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_list_new, container, false)

        initViews(view)
        apiService = ApiClient.getRetrofit(requireContext()).create(ApiService::class.java)

        setupToolbar()
        setupRecyclerViews()
        setupSearch()
        setupClickListeners()
        loadBooks()

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        // Refresh books when returning from AddBookActivity
        loadBooks()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_ADD_BOOK && resultCode == Activity.RESULT_OK) {
            val addedBook = data?.getParcelableExtra<Book>("added_book")
            if (addedBook != null) {
                // Add the new book to the list
                allBooks = allBooks + addedBook
                bindBooks()
                Toast.makeText(requireContext(), "Đã thêm sách mới vào danh sách", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.book_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_book_menu -> {
                if (isSelectionMode) {
                    // Nếu đang ở selection mode, ẩn nút xóa và thoát selection mode
                    exitSelectionMode()
                } else {
                    // Nếu không ở selection mode, hiện menu bình thường
                    showToolbarMenu()
                }
                true
            }
            R.id.action_delete_books -> {
                // Xóa sách đã chọn
                confirmDeleteSelected()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        etSearch = view.findViewById(R.id.etSearch)
        loadingLayout = view.findViewById(R.id.loadingLayout)
        emptyLayout = view.findViewById(R.id.emptyLayout)
        
        // Initialize RecyclerViews
        rvFeaturedBooks = view.findViewById(R.id.rvFeaturedBooks)
        rvRecentBooks = view.findViewById(R.id.rvRecentBooks)
        rvLiteratureBooks = view.findViewById(R.id.rvLiteratureBooks)
        rvScienceBooks = view.findViewById(R.id.rvScienceBooks)
        rvHistoryBooks = view.findViewById(R.id.rvHistoryBooks)
        
        // Initialize View All buttons
        btnViewAllFeatured = view.findViewById(R.id.btnViewAllFeatured)
        btnViewAllRecent = view.findViewById(R.id.btnViewAllRecent)
        btnViewAllLiterature = view.findViewById(R.id.btnViewAllLiterature)
        btnViewAllScience = view.findViewById(R.id.btnViewAllScience)
        btnViewAllHistory = view.findViewById(R.id.btnViewAllHistory)
    }

    private fun setupToolbar() {
        (requireActivity() as androidx.appcompat.app.AppCompatActivity).setSupportActionBar(toolbar)
    }

    private fun setupRecyclerViews() {
        rvFeaturedBooks.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvRecentBooks.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvLiteratureBooks.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvScienceBooks.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvHistoryBooks.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupSearch() {
        etSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showSearchSuggestions()
            }
        }
        
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s?.toString()?.trim() ?: ""
                if (query.length >= 2) {
                    showSearchSuggestions()
                } else {
                    hideSearchSuggestions()
                }
            }
        })
    }

    private fun showSearchSuggestions() {
        val query = etSearch.text.toString().trim()
        if (query.length < 2) return

        val suggestions = allBooks.filter { book ->
            book.title?.contains(query, ignoreCase = true) == true ||
            book.author?.contains(query, ignoreCase = true) == true ||
            book.category?.contains(query, ignoreCase = true) == true
        }.take(5)

        if (suggestions.isNotEmpty()) {
            // Hiện suggestions trong một popup hoặc dropdown
            showSearchResults(suggestions)
        }
    }

    private fun hideSearchSuggestions() {
        // Ẩn suggestions
    }

    private fun showSearchResults(books: List<Book>) {
        val bookTitles = books.map { "${it.title} - ${it.author}" }.toTypedArray()
        
        AlertDialog.Builder(requireContext())
            .setTitle("Kết quả tìm kiếm")
            .setItems(bookTitles) { _, which ->
                val selectedBook = books[which]
                val intent = Intent(requireContext(), BookDetailActivity::class.java)
                intent.putExtra("book_id", selectedBook.id)
                intent.putExtra("book_title", selectedBook.title)
                startActivity(intent)
            }
            .setNegativeButton("Đóng", null)
            .show()
    }

    private fun setupClickListeners() {
        // View All buttons
        btnViewAllFeatured.setOnClickListener { showAllBooks("Sách Nổi Bật", allBooks.take(10)) }
        btnViewAllRecent.setOnClickListener { showAllBooks("Sách Mới Nhất", allBooks.take(10)) }
        btnViewAllLiterature.setOnClickListener { showAllBooks("Văn Học", allBooks.filter { it.category?.contains("Văn học", ignoreCase = true) == true }) }
        btnViewAllScience.setOnClickListener { showAllBooks("Khoa Học", allBooks.filter { it.category?.contains("Khoa học", ignoreCase = true) == true }) }
        btnViewAllHistory.setOnClickListener { showAllBooks("Lịch Sử", allBooks.filter { it.category?.contains("Lịch sử", ignoreCase = true) == true }) }
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

        // Literature books
        val literatureBooks = allBooks.filter { it.category?.contains("Văn học", ignoreCase = true) == true }
        setupRecyclerView(rvLiteratureBooks, literatureBooks)

        // Science books
        val scienceBooks = allBooks.filter { it.category?.contains("Khoa học", ignoreCase = true) == true }
        setupRecyclerView(rvScienceBooks, scienceBooks)

        // History books
        val historyBooks = allBooks.filter { it.category?.contains("Lịch sử", ignoreCase = true) == true }
        setupRecyclerView(rvHistoryBooks, historyBooks)
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
                        onBookSelected(bookId, !isSelected)
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

    private fun showAllBooks(title: String, books: List<Book>) {
        val intent = Intent(requireContext(), BookListActivity::class.java)
        intent.putExtra("title", title)
        intent.putParcelableArrayListExtra("books", ArrayList(books))
        startActivity(intent)
    }

    private fun toggleSelectionMode() {
        isSelectionMode = true
        selectedBooks.clear()
        
        toolbar.title = "Chọn sách để xóa (0)"
        toolbar.setBackgroundColor(requireContext().getColor(R.color.error))
        
        // Hiện nút xóa
        showDeleteButton()
        // Refresh all RecyclerViews
        bindBooks()
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedBooks.clear()

        toolbar.title = "Thư Viện Sách"
        toolbar.setBackgroundColor(requireContext().getColor(R.color.primary))
        
        // Ẩn nút xóa
        hideDeleteButton()
        bindBooks()
    }

    fun onBookSelected(bookId: String, isSelected: Boolean) {
        if (isSelected) {
            selectedBooks.add(bookId)
        } else {
            selectedBooks.remove(bookId)
        }
        
        toolbar.title = "Chọn sách để xóa (${selectedBooks.size})"
        
        // Refresh all RecyclerViews
        bindBooks()
        
        // Show delete button when books are selected
        if (selectedBooks.isNotEmpty()) {
            showDeleteButton()
        } else {
            hideDeleteButton()
        }
    }

    private fun showDeleteButton() {
        toolbar.menu.findItem(R.id.action_delete_books)?.isVisible = true
    }

    private fun hideDeleteButton() {
        toolbar.menu.findItem(R.id.action_delete_books)?.isVisible = false
    }

    private fun confirmDeleteSelected() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xóa sách đã chọn")
            .setMessage("Bạn có chắc muốn xóa ${selectedBooks.size} sách đã chọn?")
            .setPositiveButton("Xóa") { _, _ ->
                deleteSelectedBooks()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteSelectedBooks() {
        // Tạm thời xóa local khỏi danh sách hiển thị
        if (selectedBooks.isEmpty()) return
        allBooks = allBooks.filterNot { selectedBooks.contains(it.id) }
        selectedBooks.clear()
        
        // Tự động thoát selection mode sau khi xóa
        exitSelectionMode()
        Toast.makeText(requireContext(), "Đã xóa sách đã chọn", Toast.LENGTH_SHORT).show()
    }

    private fun showToolbarMenu() {
        val options = arrayOf("Thêm sách", "Xóa sách")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Sách")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Add book
                        val intent = Intent(requireContext(), AddBookActivity::class.java)
                        startActivityForResult(intent, REQUEST_CODE_ADD_BOOK)
                    }
                    1 -> {
                        // Enter selection mode for deletion
                        toggleSelectionMode()
                    }
                }
            }
            .show()
    }

    private fun showLoading(show: Boolean) {
        loadingLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmpty(show: Boolean) {
        emptyLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun getSampleBooks(): List<Book> {
        return listOf(
            // Văn học
            Book(id = "1", title = "Truyện Kiều", author = "Nguyễn Du", category = "Văn học", available = true),
            Book(id = "2", title = "Chí Phèo", author = "Nam Cao", category = "Văn học", available = true),
            Book(id = "3", title = "Số đỏ", author = "Vũ Trọng Phụng", category = "Văn học", available = false),
            Book(id = "4", title = "Dế Mèn phiêu lưu ký", author = "Tô Hoài", category = "Văn học", available = true),
            Book(id = "5", title = "Vợ nhặt", author = "Kim Lân", category = "Văn học", available = true),
            Book(id = "6", title = "Những ngôi sao xa xôi", author = "Lê Minh Khuê", category = "Văn học", available = false),
            
            // Khoa học
            Book(id = "7", title = "Vật lý cơ bản", author = "Nguyễn Văn A", category = "Khoa học", available = true),
            Book(id = "8", title = "Hóa học đại cương", author = "Trần Thị B", category = "Khoa học", available = true),
            Book(id = "9", title = "Toán học cao cấp", author = "Hoàng Văn E", category = "Khoa học", available = true),
            Book(id = "10", title = "Sinh học phân tử", author = "Phạm Thị F", category = "Khoa học", available = false),
            Book(id = "11", title = "Tin học ứng dụng", author = "Lê Văn G", category = "Khoa học", available = true),
            
            // Lịch sử
            Book(id = "12", title = "Lịch sử Việt Nam", author = "Lê Văn C", category = "Lịch sử", available = true),
            Book(id = "13", title = "Thế giới cổ đại", author = "Phạm Thị D", category = "Lịch sử", available = false),
            Book(id = "14", title = "Lịch sử thế giới", author = "Nguyễn Thị H", category = "Lịch sử", available = true),
            Book(id = "15", title = "Việt Nam sử lược", author = "Trần Trọng Kim", category = "Lịch sử", available = true),
            
            // Kịch
            Book(id = "16", title = "Vũ Như Tô", author = "Nguyễn Huy Tưởng", category = "Kịch", available = true),
            Book(id = "17", title = "Bắc Sơn", author = "Nguyễn Huy Tưởng", category = "Kịch", available = false),
            
            // Tiểu thuyết
            Book(id = "18", title = "Đất rừng phương Nam", author = "Đoàn Giỏi", category = "Tiểu thuyết", available = true),
            Book(id = "19", title = "Tuổi thơ dữ dội", author = "Phùng Quán", category = "Tiểu thuyết", available = true),
            Book(id = "20", title = "Những người con gái của mẹ", author = "Nguyễn Thị Thu Huệ", category = "Tiểu thuyết", available = false),
            
            // Thơ
            Book(id = "21", title = "Thơ Hồ Xuân Hương", author = "Hồ Xuân Hương", category = "Thơ", available = true),
            Book(id = "22", title = "Thơ Nguyễn Du", author = "Nguyễn Du", category = "Thơ", available = true),
            
            // Triết học
            Book(id = "23", title = "Triết học phương Đông", author = "Nguyễn Văn I", category = "Triết học", available = true),
            Book(id = "24", title = "Đạo đức học", author = "Trần Thị J", category = "Triết học", available = false)
        )
    }
}
