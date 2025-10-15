package com.example.libman.controller.book

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.libman.R
import com.example.libman.adapters.BookAdapter
import com.example.libman.adapters.HorizontalBookAdapter
import com.example.libman.adapters.BookViewPagerAdapter
import com.example.libman.models.Book
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.libman.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookListFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var fabQuickActions: FloatingActionButton

    private var allBooks: List<Book> = emptyList()
    private lateinit var apiService: ApiService
    private lateinit var viewPagerAdapter: BookViewPagerAdapter
    
    private val categories = listOf("Tất cả", "Văn học", "Khoa học", "Lịch sử", "Kịch", "Tiểu thuyết")
    
    // Selection mode for deletion
    private var isSelectionMode = false
    private val selectedBooks = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_list_new, container, false)

        searchView = view.findViewById(R.id.svBooks)
        toolbar = view.findViewById(R.id.toolbar)
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
        fabQuickActions = view.findViewById(R.id.fabQuickActions)

        apiService = ApiClient.getRetrofit(requireContext()).create(ApiService::class.java)

        setupToolbar()
        setupSearch()
        setupViewPager()
        setupFab()

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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

    private fun setupToolbar() {
        // Set up toolbar for fragment
        (requireActivity() as androidx.appcompat.app.AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.title = "Thư Viện Sách"
    }

    private fun setupViewPager() {
        viewPagerAdapter = BookViewPagerAdapter(requireActivity(), categories)
        viewPager.adapter = viewPagerAdapter
        
        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = categories[position]
        }.attach()
    }

    private fun setupFab() {
        fabQuickActions.setOnClickListener {
            showQuickActionsBottomSheet()
        }
    }

    private fun showQuickActionsBottomSheet() {
        // Simple dialog for quick actions
        val options = arrayOf("Thêm sách mới", "Quét mã QR", "Tìm kiếm nâng cao", "Lọc theo tác giả")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Thao tác nhanh")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startActivity(Intent(requireContext(), AddBookActivity::class.java))
                    1 -> Toast.makeText(requireContext(), "Chức năng quét QR đang được phát triển", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(requireContext(), "Chức năng tìm kiếm nâng cao đang được phát triển", Toast.LENGTH_SHORT).show()
                    3 -> Toast.makeText(requireContext(), "Chức năng lọc theo tác giả đang được phát triển", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh ViewPager adapter when returning from other activities
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            kotlinx.coroutines.delay(500)
            viewPagerAdapter.notifyDataSetChanged()
        }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Search functionality will be handled by individual tabs
                Toast.makeText(requireContext(), "Tìm kiếm: $query", Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Real-time search can be implemented here
                return true
            }
        })
    }

    private fun toggleSelectionMode() {
        isSelectionMode = true
        selectedBooks.clear()
        
        toolbar.title = "Chọn sách để xóa (0)"
        toolbar.setBackgroundColor(requireContext().getColor(R.color.error))
        
        // Hiện nút xóa
        showDeleteButton()
        // Notify ViewPager adapter to update selection mode
        viewPagerAdapter.updateSelectionMode(isSelectionMode, selectedBooks)
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedBooks.clear()

        toolbar.title = "Thư Viện Sách"
        toolbar.setBackgroundColor(requireContext().getColor(R.color.primary))
        
        // Ẩn nút xóa
        hideDeleteButton()
        // Notify ViewPager adapter to update selection mode
        viewPagerAdapter.updateSelectionMode(isSelectionMode, selectedBooks)
    }

    fun onBookSelected(bookId: String, isSelected: Boolean) {
        if (isSelected) {
            selectedBooks.add(bookId)
        } else {
            selectedBooks.remove(bookId)
        }
        
        toolbar.title = "Chọn sách để xóa (${selectedBooks.size})"
        
        // Update ViewPager adapter with new selection
        viewPagerAdapter.updateSelectionMode(isSelectionMode, selectedBooks)
        
        // Show delete button when books are selected
        if (selectedBooks.isNotEmpty()) {
            showDeleteButton()
        } else {
            hideDeleteButton()
        }
    }

    private fun showDeleteConfirmation() {
        if (selectedBooks.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn ít nhất một sách để xóa", Toast.LENGTH_SHORT).show()
            return
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa ${selectedBooks.size} sách đã chọn?")
            .setPositiveButton("Xóa") { _, _ ->
                deleteSelectedBooks()
            }
            .setNegativeButton("Hủy") { _, _ ->
                toggleSelectionMode()
            }
            .show()
    }

    private fun confirmDeleteSelected() {
        if (selectedBooks.isEmpty()) return

        AlertDialog.Builder(requireContext())
            .setTitle("Xóa sách đã chọn")
            .setMessage("Bạn có chắc muốn xóa ${selectedBooks.size} sách đã chọn?")
            .setPositiveButton("Xóa") { _, _ ->
                deleteSelectedBooks()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteSelectedBooks() {
        // TODO: Implement actual deletion logic
        Toast.makeText(requireContext(), "Đã xóa ${selectedBooks.size} sách", Toast.LENGTH_SHORT).show()
        selectedBooks.clear()
        
        // Tự động thoát selection mode sau khi xóa
        exitSelectionMode()
    }

    fun isBookSelected(bookId: String): Boolean {
        return selectedBooks.contains(bookId)
    }

    fun isInSelectionMode(): Boolean {
        return isSelectionMode
    }

    private fun showDeleteButton() {
        toolbar.menu.findItem(R.id.action_delete_books)?.isVisible = true
    }

    private fun hideDeleteButton() {
        toolbar.menu.findItem(R.id.action_delete_books)?.isVisible = false
    }

    private fun showToolbarMenu() {
        val options = arrayOf("Thêm sách", "Xóa sách")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Sách")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Add book
                        startActivity(Intent(requireContext(), AddBookActivity::class.java))
                    }
                    1 -> {
                        // Enter selection mode for deletion
                        toggleSelectionMode()
                    }
                }
            }
            .show()
    }

}