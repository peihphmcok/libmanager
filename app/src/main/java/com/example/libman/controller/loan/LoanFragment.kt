package com.example.libman.controller.loan

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.libman.R
import com.example.libman.adapters.LoanAdapter
import com.example.libman.models.Loan
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.libman.utils.TokenManager
import com.example.libman.utils.VietnameseUtils
import com.google.android.material.snackbar.Snackbar

class LoanFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingLayout: View
    private lateinit var emptyLayout: View
    private lateinit var searchView: SearchView
    private lateinit var fabAddLoan: FloatingActionButton

    private var allLoans: List<Loan> = emptyList()
    private var adapter: LoanAdapter? = null
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_loan, container, false)

        recyclerView = view.findViewById(R.id.rvLoans)
        loadingLayout = view.findViewById(R.id.loadingLayout)
        emptyLayout = view.findViewById(R.id.emptyLayout)
        searchView = view.findViewById(R.id.svLoans)
        fabAddLoan = view.findViewById(R.id.fabAddLoan)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        setupSearch()
        setupFab()
        apiService = ApiClient.getRetrofit(requireContext()).create(ApiService::class.java)
        fetchLoans()

        return view
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterLoans(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterLoans(newText)
                return true
            }
        })
    }

    private fun setupFab() {
        // Show add loan button for all users (temporarily removed admin check)
        fabAddLoan.visibility = View.VISIBLE
        fabAddLoan.setOnClickListener {
            val intent = Intent(requireContext(), AddLoanActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchLoans() {
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            try {
                val loans = apiService.getLoans()
                allLoans = loans
                bindLoans(loans)
            } catch (e: Exception) {
                allLoans = emptyList()
                bindLoans(allLoans)
                Snackbar.make(requireView(), "Lỗi khi tải danh sách phiếu mượn", Snackbar.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun bindLoans(loans: List<Loan>) {
        if (loans.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyLayout.visibility = View.VISIBLE
        } else {
            emptyLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter = LoanAdapter(
                loans = loans,
                onReturnClick = { loan ->
                    handleReturnBook(loan)
                },
                onExtendClick = { loan ->
                    handleExtendLoan(loan)
                }
            )
            recyclerView.adapter = adapter
        }
    }

    private fun filterLoans(query: String?) {
        val trimmed = query?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            bindLoans(allLoans)
            return
        }
        val filtered = allLoans.filter { loan ->
            VietnameseUtils.matchesVietnamese(loan.book?.title, trimmed) ||
            VietnameseUtils.matchesVietnamese(loan.book?.author, trimmed) ||
            VietnameseUtils.matchesVietnamese(loan.user?.name, trimmed)
        }
        bindLoans(filtered)
    }

    private fun handleReturnBook(loan: Loan) {
        if (loan.id == null) {
            Snackbar.make(requireView(), "Không thể trả sách mẫu", Snackbar.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            try {
                withContext(Dispatchers.IO) {
                    apiService.updateLoan(loan.id, mapOf<String, Any>("status" to "returned"))
                }
                
                Snackbar.make(requireView(), "Trả sách thành công!", Snackbar.LENGTH_SHORT).show()
                // Refresh the loan list
                fetchLoans()
                
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Lỗi khi trả sách: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleExtendLoan(loan: Loan) {
        if (loan.id == null) {
            Snackbar.make(requireView(), "Không thể gia hạn sách mẫu", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Extend loan by 7 days
        val newDueDate = java.util.Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)
        
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            try {
                withContext(Dispatchers.IO) {
                    apiService.updateLoan(loan.id, mapOf<String, Any>("dueDate" to newDueDate))
                }
                
                Snackbar.make(requireView(), "Gia hạn thành công! Hạn mới: 7 ngày", Snackbar.LENGTH_LONG).show()
                // Refresh the loan list
                fetchLoans()
                
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Lỗi khi gia hạn: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        loadingLayout.visibility = if (show) View.VISIBLE else View.GONE
    }
}