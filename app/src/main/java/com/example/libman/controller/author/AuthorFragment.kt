package com.example.libman.controller.author

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.example.libman.utils.VietnameseUtils

class AuthorFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_author, container, false)

        val recycler: RecyclerView = view.findViewById(R.id.rvAuthors)
        val loading: View = view.findViewById(R.id.loadingLayout)
        val empty: View = view.findViewById(R.id.emptyLayout)
        val search: SearchView = view.findViewById(R.id.svAuthors)
        val fab: FloatingActionButton = view.findViewById(R.id.fabAddAuthor)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        var allAuthors: List<Author> = emptyList()

        fun bind(list: List<Author>) {
            if (list.isEmpty()) {
                recycler.visibility = View.GONE
                empty.visibility = View.VISIBLE
            } else {
                empty.visibility = View.GONE
                recycler.visibility = View.VISIBLE
                recycler.adapter = AuthorAdapter(list) { author ->
                    // Navigate to edit author activity
                    val intent = Intent(requireContext(), EditAuthorActivity::class.java)
                    intent.putExtra("author_id", author.id)
                    startActivity(intent)
                }
            }
        }

        fun loadAuthors() {
            loading.visibility = View.VISIBLE
            val api = ApiClient.getRetrofit(requireContext()).create(ApiService::class.java)
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                try {
                    val data = api.getAuthors()
                    allAuthors = data
                    bind(allAuthors)
                } catch (_: Exception) {
                    // Fallback to sample data if API fails
                    allAuthors = getSampleAuthors()
                    bind(allAuthors)
                } finally {
                    loading.visibility = View.GONE
                }
            }
        }

        fun filter(q: String?) {
            val t = q?.trim().orEmpty()
            if (t.isEmpty()) {
                bind(allAuthors)
            } else {
                bind(allAuthors.filter { a ->
                    VietnameseUtils.matchesVietnamese(a.name, t) || 
                    VietnameseUtils.matchesVietnamese(a.bio, t)
                })
            }
        }

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }
        })

        // Show add author button for all users (temporarily removed admin check)
        fab.visibility = View.VISIBLE
        fab.setOnClickListener {
            startActivity(Intent(requireContext(), AddAuthorActivity::class.java))
        }

        loadAuthors()

        return view
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