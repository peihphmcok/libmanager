package com.example.libman.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.libman.R
import com.example.libman.models.Loan
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*

class LoanAdapter(
    private val loans: List<Loan>,
    private val onReturnClick: (Loan) -> Unit = {},
    private val onExtendClick: (Loan) -> Unit = {}
) : RecyclerView.Adapter<LoanAdapter.LoanViewHolder>() {

    class LoanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBookTitle: TextView = itemView.findViewById(R.id.tvBookTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        val tvBorrowerName: TextView = itemView.findViewById(R.id.tvBorrowerName)
        val tvBorrowDate: TextView = itemView.findViewById(R.id.tvBorrowDate)
        val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        val btnReturn: MaterialButton = itemView.findViewById(R.id.btnReturn)
        val btnExtend: MaterialButton = itemView.findViewById(R.id.btnExtend)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_loan, parent, false)
        return LoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {
        val loan = loans[position]
        
        holder.tvBookTitle.text = loan.book?.title ?: "Không có thông tin"
        holder.tvAuthor.text = loan.book?.author ?: "Không có thông tin"
        holder.tvBorrowerName.text = loan.user?.fullname ?: loan.user?.name ?: "Không có thông tin"
        
        // Format dates
        holder.tvBorrowDate.text = loan.borrowDate ?: "N/A"
        holder.tvDueDate.text = loan.dueDate ?: "N/A"
        
        // Set status
        when (loan.status?.lowercase()) {
            "borrowed" -> {
                holder.chipStatus.text = "Đang mượn"
                holder.chipStatus.setChipBackgroundColorResource(R.color.purple_700)
                holder.chipStatus.setTextColor(holder.itemView.context.getColor(android.R.color.white))
            }
            "returned" -> {
                holder.chipStatus.text = "Đã trả"
                holder.chipStatus.setChipBackgroundColorResource(android.R.color.holo_green_dark)
                holder.chipStatus.setTextColor(holder.itemView.context.getColor(android.R.color.white))
            }
            "overdue" -> {
                holder.chipStatus.text = "Quá hạn"
                holder.chipStatus.setChipBackgroundColorResource(android.R.color.holo_red_dark)
                holder.chipStatus.setTextColor(holder.itemView.context.getColor(android.R.color.white))
            }
            else -> {
                holder.chipStatus.text = "Không xác định"
                holder.chipStatus.setChipBackgroundColorResource(android.R.color.darker_gray)
                holder.chipStatus.setTextColor(holder.itemView.context.getColor(android.R.color.white))
            }
        }
        
        // Set button visibility based on status
        if (loan.status?.lowercase() == "borrowed") {
            holder.btnReturn.visibility = View.VISIBLE
            holder.btnExtend.visibility = View.VISIBLE
        } else {
            holder.btnReturn.visibility = View.GONE
            holder.btnExtend.visibility = View.GONE
        }
        
        holder.btnReturn.setOnClickListener {
            onReturnClick(loan)
        }
        
        holder.btnExtend.setOnClickListener {
            onExtendClick(loan)
        }
    }

    override fun getItemCount(): Int = loans.size
}