package com.example.libman.controller.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.libman.R
import com.example.libman.adapters.MainViewPagerAdapter
import com.example.libman.controller.auth.LoginActivity
import com.example.libman.utils.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.viewpager2.widget.ViewPager2

class HomeActivityWithViewPager : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var viewPager: ViewPager2
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_with_viewpager)

        tokenManager = TokenManager(this)
        
        // Check if user is logged in
        if (tokenManager.getToken().isNullOrEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        initViews()
        setupViewPager()
        setupBottomNavigation()
    }

    private fun initViews() {
        bottomNav = findViewById(R.id.bottom_navigation)
        viewPager = findViewById(R.id.viewPager)
    }

    private fun setupViewPager() {
        val adapter = MainViewPagerAdapter(this)
        viewPager.adapter = adapter
        
        // Disable swipe between pages
        viewPager.isUserInputEnabled = false
    }

    private fun setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_books -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.nav_authors -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.nav_loans -> {
                    viewPager.currentItem = 2
                    true
                }
                R.id.nav_profile -> {
                    viewPager.currentItem = 3
                    true
                }
                else -> false
            }
        }
    }
}
