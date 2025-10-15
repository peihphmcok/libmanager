package com.example.libman.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.libman.controller.book.BookTabFragment

class BookViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val categories: List<String>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        return BookTabFragment.newInstance(categories[position])
    }

    fun updateSelectionMode(isSelectionMode: Boolean, selectedBooks: Set<String>) {
        // Update all fragments with new selection mode
        for (i in 0 until itemCount) {
            val fragment = getFragment(i)
            if (fragment is BookTabFragment) {
                fragment.updateSelectionMode(isSelectionMode, selectedBooks)
            }
        }
    }

    private fun getFragment(position: Int): Fragment? {
        return try {
            // This is a workaround to get fragment from ViewPager2
            // In a real implementation, you might want to store fragments in a list
            null
        } catch (e: Exception) {
            null
        }
    }
}
