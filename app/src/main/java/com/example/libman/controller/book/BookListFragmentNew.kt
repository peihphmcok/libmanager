package com.example.libman.controller.book

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
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
import com.google.android.material.card.MaterialCardView
import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookListFragmentNew : Fragment() {

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var searchView: SearchView
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
        searchView = view.findViewById(R.id.searchView)
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
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.trim() ?: ""
                if (query.isNotEmpty()) {
                    filterBooks(query)
                } else {
                    showAllBooks()
                }
                return true
            }
        })
    }

    private fun filterBooks(query: String) {
        val filteredBooks = allBooks.filter { book ->
            book.title?.contains(query, ignoreCase = true) == true ||
            book.author?.contains(query, ignoreCase = true) == true ||
            book.category?.contains(query, ignoreCase = true) == true
        }
        
        // Update all sections with filtered results
        setupRecyclerView(rvFeaturedBooks, filteredBooks.take(5)) // Featured books
        setupRecyclerView(rvRecentBooks, filteredBooks.take(8))   // Recent books
        
        // Apply same filtering logic as bindBooks
        val availableCategories = filteredBooks.mapNotNull { it.category }.distinct().sorted()
        createDynamicCategorySections(availableCategories, filteredBooks)
    }
    
    private fun showAllBooks() {
        // Restore original book lists
        bindBooks()
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
                android.util.Log.d("BookListFragmentNew", "Loading books from server...")
                
                val response = apiService.getBooks()
                allBooks = response.books ?: emptyList()
                
                android.util.Log.d("BookListFragmentNew", "Books response: ${allBooks.size} books found")
                android.util.Log.d("BookListFragmentNew", "Total books: ${response.total}")
                android.util.Log.d("BookListFragmentNew", "Current page: ${response.currentPage}")
                
                // Log all books for debugging
                allBooks.forEachIndexed { index, book ->
                    android.util.Log.d("BookListFragmentNew", "Book $index: ${book.title} - Category: ${book.category}")
                }
                
                // Log categories found
                val categories = allBooks.mapNotNull { it.category }.distinct()
                android.util.Log.d("BookListFragmentNew", "Categories found: $categories")
                
                bindBooks()
                
                Toast.makeText(requireContext(), "Đã tải ${allBooks.size} sách với ${categories.size} thể loại", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                android.util.Log.e("BookListFragmentNew", "Error loading books: ${e.message}", e)
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

        // Dynamic category books based on available categories
        val availableCategories = allBooks.mapNotNull { it.category }.distinct().sorted()
        android.util.Log.d("BookListFragmentNew", "Available categories for sections: $availableCategories")
        
        // Create dynamic sections for all categories
        createDynamicCategorySections(availableCategories, allBooks)
    }
    
    private fun createDynamicCategorySections(categories: List<String>, books: List<Book>) {
        // Create sections for each category that has books
        val categoriesWithBooks = categories.filter { category ->
            books.any { it.category == category }
        }
        
        android.util.Log.d("BookListFragmentNew", "Categories with books: $categoriesWithBooks")
        
        // Create dynamic sections for all categories
        createDynamicLayout(categoriesWithBooks, books)
    }
    
    private fun createDynamicLayout(categories: List<String>, books: List<Book>) {
        // Get the main container from the layout
        val mainContainer = view?.findViewById<LinearLayout>(R.id.mainContainer)
        if (mainContainer == null) {
            android.util.Log.e("BookListFragmentNew", "Main container not found!")
            return
        }
        
        // Clear existing category sections (keep featured, recent, search)
        val sectionsToRemove = mutableListOf<View>()
        for (i in 0 until mainContainer.childCount) {
            val child = mainContainer.getChildAt(i)
            if (child is MaterialCardView && child.id in listOf(R.id.literatureCard, R.id.scienceCard, R.id.historyCard)) {
                sectionsToRemove.add(child)
            }
        }
        sectionsToRemove.forEach { mainContainer.removeView(it) }
        
        // Create sections for each category
        categories.forEachIndexed { index, category ->
            val categoryBooks = books.filter { it.category == category }
            if (categoryBooks.isNotEmpty()) {
                val categorySection = createCategorySection(category, categoryBooks)
                mainContainer.addView(categorySection)
                android.util.Log.d("BookListFragmentNew", "Created section for '$category': ${categoryBooks.size} books")
            }
        }
        
        android.util.Log.d("BookListFragmentNew", "Created ${categories.size} category sections")
    }
    
    private fun createCategorySection(categoryName: String, books: List<Book>): MaterialCardView {
        val context = requireContext()
        val inflater = LayoutInflater.from(context)
        
        // Create the card container
        val cardView = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16.dpToPx(context)
            }
            radius = 12f
            elevation = 4f
        }
        
        // Create the content
        val contentView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dpToPx(context), 16.dpToPx(context), 16.dpToPx(context), 16.dpToPx(context))
        }
        
        // Add header
        val headerView = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 12.dpToPx(context))
        }
        
        val titleView = TextView(context).apply {
            text = categoryName
            textSize = 18f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(context.getColor(R.color.text_primary))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        val countView = TextView(context).apply {
            text = "${books.size} sách"
            textSize = 14f
            setTextColor(context.getColor(R.color.text_secondary))
        }
        
        headerView.addView(titleView)
        headerView.addView(countView)
        
        // Add RecyclerView
        val recyclerView = RecyclerView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        
        // Setup RecyclerView with adapter
        val adapter = HorizontalBookAdapter(
            books = books,
            onBookClick = { book ->
                if (book.id != null) {
                    val intent = Intent(context, BookDetailActivity::class.java)
                    intent.putExtra("book_id", book.id)
                    intent.putExtra("book_title", book.title)
                    startActivity(intent)
                } else {
                    Toast.makeText(context, "Không thể xem chi tiết sách này", Toast.LENGTH_SHORT).show()
                }
            }
        )
        recyclerView.adapter = adapter
        
        contentView.addView(headerView)
        contentView.addView(recyclerView)
        cardView.addView(contentView)
        
        return cardView
    }
    
    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
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

}
