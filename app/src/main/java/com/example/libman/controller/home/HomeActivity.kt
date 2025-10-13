package com.example.libman.controller.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.libman.R
import com.example.libman.controller.auth.LoginActivity
import com.example.libman.controller.book.BookListFragment
import com.example.libman.controller.author.AuthorFragment
import com.example.libman.controller.loan.LoanFragment
import com.example.libman.controller.user.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bottomNav = findViewById(R.id.bottom_navigation)

        // Mặc định hiển thị BookList
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, BookListFragment())
            .commit()

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_books -> loadFragment(BookListFragment())
                R.id.nav_authors -> loadFragment(AuthorFragment())
                R.id.nav_loans -> loadFragment(LoanFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
