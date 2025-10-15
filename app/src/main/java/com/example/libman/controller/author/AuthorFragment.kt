package com.example.libman.controller.author

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.example.libman.utils.VietnameseUtils

class AuthorFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingLayout: View
    private lateinit var emptyLayout: View
    private lateinit var searchView: SearchView
    private lateinit var fabAddAuthor: FloatingActionButton
    private lateinit var apiService: ApiService
    
    private var allAuthors: List<Author> = emptyList()
    private lateinit var adapter: AuthorAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_author, container, false)

        initViews(view)
        setupApi()
        setupSearch()
        setupFab()
        setupAdapter()
        fetchAuthors()

        return view
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.rvAuthors)
        loadingLayout = view.findViewById(R.id.loadingLayout)
        emptyLayout = view.findViewById(R.id.emptyLayout)
        searchView = view.findViewById(R.id.svAuthors)
        fabAddAuthor = view.findViewById(R.id.fabAddAuthor)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupApi() {
        apiService = ApiClient.getRetrofit(requireContext()).create(ApiService::class.java)
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

    private fun setupFab() {
        // Show add author button for all users (temporarily removed admin check)
        fabAddAuthor.visibility = View.VISIBLE
        fabAddAuthor.setOnClickListener {
            startActivity(Intent(requireContext(), AddAuthorActivity::class.java))
        }
    }

    private fun setupAdapter() {
        adapter = AuthorAdapter(allAuthors) { author ->
            // Handle author click - show context menu
            showAuthorContextMenu(author)
        }
        recyclerView.adapter = adapter
    }

    private fun showAuthorContextMenu(author: Author) {
        val options = arrayOf("Xem chi tiết", "Chỉnh sửa", "Xóa")
        
        AlertDialog.Builder(requireContext())
            .setTitle(author.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // View details - show author info
                        showAuthorDetails(author)
                    }
                    1 -> {
                        // Edit author
                        val intent = Intent(requireContext(), UpdateAuthorActivity::class.java)
                        intent.putExtra("author_id", author.id)
                        intent.putExtra("author", author)
                        startActivity(intent)
                    }
                    2 -> {
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
            adapter = AuthorAdapter(authors) { author ->
                // Navigate to edit author activity
                val intent = Intent(requireContext(), EditAuthorActivity::class.java)
                intent.putExtra("author_id", author.id)
                startActivity(intent)
            }
            recyclerView.adapter = adapter
        }
    }

    private fun filterAuthors(query: String?) {
        val trimmed = query?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            bindAuthors(allAuthors)
            return
        }
        val filtered = allAuthors.filter { author ->
            VietnameseUtils.matchesVietnamese(author.name, trimmed) || 
            VietnameseUtils.matchesVietnamese(author.bio, trimmed)
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
}