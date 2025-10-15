package com.example.libman.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.libman.controller.loan.ActiveLoanFragment
import com.example.libman.controller.loan.ReturnedLoanFragment

class LoanViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val activeFragment = ActiveLoanFragment()
    private val returnedFragment = ReturnedLoanFragment()

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> activeFragment
            1 -> returnedFragment
            else -> activeFragment
        }
    }

    fun getActiveFragment(): ActiveLoanFragment = activeFragment
    fun getReturnedFragment(): ReturnedLoanFragment = returnedFragment
}
