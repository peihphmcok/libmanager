package com.example.libman.controller.loan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.libman.R
import com.example.libman.adapters.LoanAdapter
import com.example.libman.models.Loan

class ReturnedLoanFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var adapter: LoanAdapter? = null
    private var onReturnClick: ((Loan) -> Unit)? = null
    private var onExtendClick: ((Loan) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_loan_tab, container, false)
        
        recyclerView = view.findViewById(R.id.rvLoans)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        return view
    }

    fun setLoans(loans: List<Loan>) {
        if (::recyclerView.isInitialized) {
            adapter = LoanAdapter(
                loans = loans,
                onReturnClick = onReturnClick ?: {},
                onExtendClick = onExtendClick ?: {}
            )
            recyclerView.adapter = adapter
        }
    }

    fun setOnReturnClick(listener: (Loan) -> Unit) {
        onReturnClick = listener
    }

    fun setOnExtendClick(listener: (Loan) -> Unit) {
        onExtendClick = listener
    }
}
