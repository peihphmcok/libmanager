package com.example.libman.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.libman.controller.loan.ActiveLoanFragment
import com.example.libman.controller.loan.ReturnedLoanFragment

class LoanViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private var activeFragment: ActiveLoanFragment? = null
    private var returnedFragment: ReturnedLoanFragment? = null

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                activeFragment = ActiveLoanFragment()
                activeFragment!!
            }
            1 -> {
                returnedFragment = ReturnedLoanFragment()
                returnedFragment!!
            }
            else -> ActiveLoanFragment()
        }
    }

    fun getActiveFragment(): ActiveLoanFragment? = activeFragment
    fun getReturnedFragment(): ReturnedLoanFragment? = returnedFragment
}
