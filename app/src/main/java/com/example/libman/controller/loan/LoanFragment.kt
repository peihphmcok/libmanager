package com.example.libman.controller.loan

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
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
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import com.example.libman.adapters.LoanViewPagerAdapter

class LoanFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var loadingLayout: View
    private lateinit var emptyLayout: View
    private lateinit var searchView: SearchView
    private lateinit var toolbar: Toolbar

    private var allLoans: List<Loan> = emptyList()
    private var activeLoans: List<Loan> = emptyList()
    private var returnedLoans: List<Loan> = emptyList()
    private lateinit var viewPagerAdapter: LoanViewPagerAdapter
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_loan, container, false)

        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
        loadingLayout = view.findViewById(R.id.loadingLayout)
        emptyLayout = view.findViewById(R.id.emptyLayout)
        searchView = view.findViewById(R.id.svLoans)
        toolbar = view.findViewById(R.id.toolbar)

        // Setup ViewPager2
        viewPagerAdapter = LoanViewPagerAdapter(requireActivity())
        viewPager.adapter = viewPagerAdapter

        // Setup TabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Đang mượn"
                1 -> "Đã trả"
                else -> ""
            }
        }.attach()

        setupToolbar()
        setupSearch()
        apiService = ApiClient.getRetrofit(requireContext()).create(ApiService::class.java)
        fetchLoans()

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.loan_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_loan_menu -> {
                showToolbarMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupToolbar() {
        // Set up toolbar for fragment
        (requireActivity() as androidx.appcompat.app.AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.title = "Quản Lý Mượn Sách"
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from AddLoanActivity
        // Add a small delay to ensure the activity has finished
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            kotlinx.coroutines.delay(100) // Small delay
            fetchLoans()
        }
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


    private fun fetchLoans() {
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            try {
                val response = apiService.getLoans()
                val loans = response.loans ?: emptyList()
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
        // Phân chia loans thành active và returned
        activeLoans = loans.filter { it.isReturned != true }
        returnedLoans = loans.filter { it.isReturned == true }
        
        if (loans.isEmpty()) {
            viewPager.visibility = View.GONE
            emptyLayout.visibility = View.VISIBLE
        } else {
            emptyLayout.visibility = View.GONE
            viewPager.visibility = View.VISIBLE
            
            // Setup active loans fragment
            val activeFragment = viewPagerAdapter.getActiveFragment()
            activeFragment.setOnReturnClick { loan -> handleReturnBook(loan) }
            activeFragment.setOnExtendClick { loan -> handleExtendLoan(loan) }
            activeFragment.setLoans(activeLoans)
            
            // Setup returned loans fragment
            val returnedFragment = viewPagerAdapter.getReturnedFragment()
            returnedFragment.setOnReturnClick { loan -> handleReturnBook(loan) }
            returnedFragment.setOnExtendClick { loan -> handleExtendLoan(loan) }
            returnedFragment.setLoans(returnedLoans)
        }
    }

    private fun filterLoans(query: String?) {
        val trimmed = query?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            bindLoans(allLoans)
            return
        }
        val filtered = allLoans.filter { loan ->
            loan.book?.title?.contains(trimmed, ignoreCase = true) == true ||
            loan.book?.author?.contains(trimmed, ignoreCase = true) == true ||
            loan.user?.fullname?.contains(trimmed, ignoreCase = true) == true ||
            loan.user?.name?.contains(trimmed, ignoreCase = true) == true
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
        val newDueDate = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
        
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

    private fun showSelectLoansDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Chọn phiếu mượn để xóa")
            .setMessage("Nhấn vào phiếu mượn bạn muốn xóa. Phiếu mượn được chọn sẽ có viền đỏ.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showToolbarMenu() {
        val options = arrayOf("Thêm phiếu mượn", "Xóa phiếu mượn")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Phiếu mượn")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Add loan
                        startActivity(Intent(requireContext(), AddLoanActivity::class.java))
                    }
                    1 -> {
                        // Delete loans
                        showSelectLoansDialog()
                    }
                }
            }
            .show()
    }
}