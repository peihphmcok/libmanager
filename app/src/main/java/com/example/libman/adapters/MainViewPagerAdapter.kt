package com.example.libman.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.libman.controller.author.AuthorFragment
import com.example.libman.controller.book.BookListFragmentNew
import com.example.libman.controller.loan.LoanFragment
import com.example.libman.controller.user.ProfileFragment

class MainViewPagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BookListFragmentNew()
            1 -> AuthorFragment()
            2 -> LoanFragment()
            3 -> ProfileFragment()
            else -> BookListFragmentNew()
        }
    }
}
