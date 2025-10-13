package com.example.libman.controller.author

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
                recycler.adapter = AuthorAdapter(list)
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
                    allAuthors = emptyList()
                    bind(allAuthors)
                } finally {
                    loading.visibility = View.GONE
                }
            }
        }

        fun filter(q: String?) {
            val t = q?.trim().orEmpty().lowercase()
            if (t.isEmpty()) {
                bind(allAuthors)
            } else {
                bind(allAuthors.filter { a ->
                    a.name.lowercase().contains(t) || (a.bio ?: "").lowercase().contains(t)
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

        val role = TokenManager(requireContext()).getRole()
        val isAdmin = role == "admin"
        fab.visibility = if (isAdmin) View.VISIBLE else View.GONE
        if (isAdmin) {
            fab.setOnClickListener {
                startActivity(android.content.Intent(requireContext(), AddAuthorActivity::class.java))
            }
        }

        loadAuthors()

        return view
    }
}