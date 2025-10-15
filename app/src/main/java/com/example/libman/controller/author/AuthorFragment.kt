package com.example.libman.controller.author

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.libman.R
import com.example.libman.adapters.AuthorAdapter
import com.example.libman.models.Author
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.libman.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthorFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingLayout: View
    private lateinit var emptyLayout: View
    private lateinit var searchView: SearchView
    private lateinit var toolbar: Toolbar
    private lateinit var apiService: ApiService
    
    private var allAuthors: List<Author> = emptyList()
    private lateinit var adapter: AuthorAdapter
    
    // Selection mode for deletion
    private var isSelectionMode = false
    private val selectedAuthors = mutableSetOf<String>()
    
    companion object {
        private const val REQUEST_CODE_ADD_AUTHOR = 1002
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_author, container, false)

        initViews(view)
        setupApi()
        setupSearch()
        setupAdapter()
        fetchAuthors()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.author_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
        
        // Debug: Log to see if menu is being created
        android.util.Log.d("AuthorFragment", "Menu created with ${menu.size()} items")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        android.util.Log.d("AuthorFragment", "Menu item clicked: ${item.itemId}")
        return when (item.itemId) {
            R.id.action_author_menu -> {
                if (isSelectionMode) {
                    exitSelectionMode()
                } else {
                    showToolbarMenu()
                }
                true
            }
            R.id.action_delete_authors -> {
                confirmDeleteSelected()
                true
            }
            else -> {
                android.util.Log.d("AuthorFragment", "Unknown menu item: ${item.itemId}")
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.rvAuthors)
        loadingLayout = view.findViewById(R.id.loadingLayout)
        emptyLayout = view.findViewById(R.id.emptyLayout)
        searchView = view.findViewById(R.id.svAuthors)
        toolbar = view.findViewById(R.id.toolbar)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupApi() {
        apiService = ApiClient.getRetrofit(requireContext()).create(ApiService::class.java)
    }

    private fun setupToolbar() {
        // Set up toolbar for fragment
        val activity = requireActivity() as androidx.appcompat.app.AppCompatActivity
        activity.setSupportActionBar(toolbar)
        toolbar.title = "Tác Giả"
        
        // Ensure menu is visible
        setHasOptionsMenu(true)
        
        // Force invalidate options menu
        activity.invalidateOptionsMenu()
        
        // Debug: Log toolbar setup
        android.util.Log.d("AuthorFragment", "Toolbar setup completed")
        
        // Test: Add a test menu item to see if menu works
        toolbar.inflateMenu(R.menu.author_list_menu)
        toolbar.setOnMenuItemClickListener { item ->
            android.util.Log.d("AuthorFragment", "Toolbar menu item clicked: ${item.itemId}")
            when (item.itemId) {
                R.id.action_author_menu -> {
                    if (isSelectionMode) {
                        exitSelectionMode()
                    } else {
                        showToolbarMenu()
                    }
                    true
                }
                R.id.action_delete_authors -> {
                    confirmDeleteSelected()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterAuthors(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterAuthors(newText)
                return true
            }
        })
    }


    private fun setupAdapter() {
        adapter = AuthorAdapter(
            authors = allAuthors,
            onItemClick = { author ->
                if (isSelectionMode) {
                    // Toggle selection
                    author.id?.let { authorId ->
                        val isSelected = selectedAuthors.contains(authorId)
                        onAuthorSelected(authorId, !isSelected)
                    }
                } else {
                    // Navigate to author detail
                    navigateToAuthorDetail(author)
                }
            },
            onMenuClick = { author ->
                // Show context menu for edit/delete
                showAuthorContextMenu(author)
            },
            isSelectionMode = isSelectionMode,
            selectedAuthors = selectedAuthors
        )
        recyclerView.adapter = adapter
    }

    private fun navigateToAuthorDetail(author: Author) {
        val intent = Intent(requireContext(), AuthorDetailActivity::class.java)
        intent.putExtra("author_name", author.name)
        intent.putExtra("author_bio", author.bio)
        intent.putExtra("author_birth_year", author.birthYear ?: 0)
        intent.putExtra("author_nationality", author.nationality)
        intent.putExtra("author_books_count", 5) // Mock data
        startActivity(intent)
    }

    private fun showAuthorContextMenu(author: Author) {
        val options = arrayOf("Chỉnh sửa", "Xóa")
        
        AlertDialog.Builder(requireContext())
            .setTitle(author.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Edit author
                        val intent = Intent(requireContext(), UpdateAuthorActivity::class.java)
                        intent.putExtra("author_id", author.id)
                        intent.putExtra("author", author)
                        startActivity(intent)
                    }
                    1 -> {
                        // Delete author
                        showDeleteConfirmation(author)
                    }
                }
            }
            .show()
    }

    private fun showAuthorDetails(author: Author) {
        val message = buildString {
            append("Tên: ${author.name}\n")
            append("Tiểu sử: ${author.bio ?: "Không có"}\n")
            append("Quốc tịch: ${author.nationality ?: "Không có"}\n")
            append("Năm sinh: ${author.birthYear ?: "Không có"}")
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Thông tin tác giả")
            .setMessage(message)
            .setPositiveButton("Đóng", null)
            .show()
    }

    private fun showDeleteConfirmation(author: Author) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa tác giả \"${author.name}\"?")
            .setPositiveButton("Xóa") { _, _ ->
                deleteAuthor(author)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteAuthor(author: Author) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    apiService.deleteAuthor(author.id!!)
                }
                
                android.util.Log.d("AuthorFragment", "Author deleted successfully: ${author.name}")
                fetchAuthors() // Refresh the list
                
            } catch (e: Exception) {
                android.util.Log.e("AuthorFragment", "Error deleting author: ${e.message}", e)
            }
        }
    }

    private fun fetchAuthors() {
        showLoading(true)
        
        // Use coroutines with lifecycleScope for suspend ApiService
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            try {
                android.util.Log.d("AuthorFragment", "fetchAuthors: Starting API call")
                val response = apiService.getAuthors()
                val authors = response.authors ?: emptyList()
                allAuthors = authors
                bindAuthors(authors)
                // Log success
                android.util.Log.d("AuthorFragment", "API Success: Loaded ${authors.size} authors")
                android.util.Log.d("AuthorFragment", "First author: ${authors.firstOrNull()?.name}")
            } catch (e: Exception) {
                // Log error details
                android.util.Log.e("AuthorFragment", "API Error: ${e.message}", e)
                // Fallback to sample data if API fails
                allAuthors = getSampleAuthors()
                bindAuthors(allAuthors)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun bindAuthors(authors: List<Author>) {
        if (authors.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyLayout.visibility = View.VISIBLE
        } else {
            emptyLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            setupAdapter()
        }
    }

    private fun filterAuthors(query: String?) {
        val trimmed = query?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            bindAuthors(allAuthors)
            return
        }
        val filtered = allAuthors.filter { author ->
            author.name?.contains(trimmed, ignoreCase = true) == true || 
            author.bio?.contains(trimmed, ignoreCase = true) == true
        }
        bindAuthors(filtered)
    }

    private fun showLoading(show: Boolean) {
        loadingLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from AddAuthorActivity
        // Add a delay to ensure the activity has finished
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            kotlinx.coroutines.delay(500) // Increased delay
            android.util.Log.d("AuthorFragment", "onResume: Refreshing authors data")
            fetchAuthors()
        }
    }

    private fun getSampleAuthors(): List<Author> {
        return listOf(
            Author(
                name = "Nguyễn Du",
                bio = "Đại thi hào dân tộc Việt Nam, tác giả của Truyện Kiều",
                nationality = "Việt Nam",
                birthYear = 1765
            ),
            Author(
                name = "Nam Cao",
                bio = "Nhà văn hiện thực xuất sắc của văn học Việt Nam",
                nationality = "Việt Nam", 
                birthYear = 1915
            ),
            Author(
                name = "Tô Hoài",
                bio = "Nhà văn nổi tiếng với tác phẩm Dế Mèn phiêu lưu ký",
                nationality = "Việt Nam",
                birthYear = 1920
            ),
            Author(
                name = "William Shakespeare",
                bio = "Nhà thơ và nhà viết kịch vĩ đại nhất của nước Anh",
                nationality = "Anh",
                birthYear = 1564
            ),
            Author(
                name = "Leo Tolstoy",
                bio = "Tiểu thuyết gia Nga nổi tiếng với Chiến tranh và Hòa bình",
                nationality = "Nga",
                birthYear = 1828
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_ADD_AUTHOR && resultCode == Activity.RESULT_OK) {
            val addedAuthor = data?.getParcelableExtra<Author>("added_author")
            if (addedAuthor != null) {
                // Add the new author to the list
                allAuthors = allAuthors + addedAuthor
                setupAdapter()
                Toast.makeText(requireContext(), "Đã thêm tác giả mới vào danh sách", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleSelectionMode() {
        isSelectionMode = true
        selectedAuthors.clear()

        toolbar.title = "Chọn tác giả để xóa (0)"
        toolbar.setBackgroundColor(requireContext().getColor(R.color.error))
        
        showDeleteButton()
        setupAdapter() // Refresh adapter
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedAuthors.clear()

        toolbar.title = "Tác Giả"
        toolbar.setBackgroundColor(requireContext().getColor(R.color.primary))
        
        hideDeleteButton()
        setupAdapter() // Refresh adapter
    }

    private fun onAuthorSelected(authorId: String, isSelected: Boolean) {
        if (isSelected) {
            selectedAuthors.add(authorId)
        } else {
            selectedAuthors.remove(authorId)
        }
        toolbar.title = "Chọn tác giả để xóa (${selectedAuthors.size})"
        setupAdapter() // Refresh adapter to show selection

        if (selectedAuthors.isNotEmpty()) {
            showDeleteButton()
        } else {
            hideDeleteButton()
        }
    }

    private fun showDeleteButton() {
        toolbar.menu.findItem(R.id.action_delete_authors)?.isVisible = true
    }

    private fun hideDeleteButton() {
        toolbar.menu.findItem(R.id.action_delete_authors)?.isVisible = false
    }

    private fun confirmDeleteSelected() {
        if (selectedAuthors.isEmpty()) return

        AlertDialog.Builder(requireContext())
            .setTitle("Xóa tác giả")
            .setMessage("Bạn có chắc chắn muốn xóa ${selectedAuthors.size} tác giả đã chọn?")
            .setPositiveButton("Xóa") { _, _ ->
                deleteSelectedAuthors()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteSelectedAuthors() {
        if (selectedAuthors.isEmpty()) return
        allAuthors = allAuthors.filterNot { selectedAuthors.contains(it.id) }
        selectedAuthors.clear()
        
        exitSelectionMode() // Auto exit selection mode after deletion
        Toast.makeText(requireContext(), "Đã xóa tác giả đã chọn", Toast.LENGTH_SHORT).show()
    }

    private fun showToolbarMenu() {
        android.util.Log.d("AuthorFragment", "showToolbarMenu called")
        val options = arrayOf("Thêm tác giả", "Xóa tác giả")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Tác giả")
            .setItems(options) { _, which ->
                android.util.Log.d("AuthorFragment", "Menu option selected: $which")
                when (which) {
                    0 -> {
                        android.util.Log.d("AuthorFragment", "Add author selected")
                        // Add author
                        val intent = Intent(requireContext(), AddAuthorActivity::class.java)
                        startActivityForResult(intent, REQUEST_CODE_ADD_AUTHOR)
                    }
                    1 -> {
                        android.util.Log.d("AuthorFragment", "Delete authors selected")
                        // Delete authors - enter selection mode
                        toggleSelectionMode()
                    }
                }
            }
            .show()
    }
}