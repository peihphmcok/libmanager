package com.example.libman.controller.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.libman.R
import com.example.libman.controller.auth.LoginActivity
import com.example.libman.models.User
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.example.libman.utils.TokenManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class ProfileFragment : Fragment() {

    private lateinit var apiService: ApiService
    private lateinit var tokenManager: TokenManager
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnEditProfile: LinearLayout
    private lateinit var btnChangePassword: LinearLayout
    private lateinit var btnSettings: LinearLayout
    private lateinit var btnAbout: LinearLayout
    private lateinit var ivAvatar: ImageView
    private lateinit var tvBorrowedCount: TextView
    private lateinit var tvTotalBooksCount: TextView
    
    private var currentUser: User? = null
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        initViews(view)
        setupApi()
        setupClickListeners()
        loadUserProfile()

        return view
    }

    private fun initViews(view: View) {
        tvUsername = view.findViewById(R.id.tvUsername)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvRole = view.findViewById(R.id.tvRole)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        btnSettings = view.findViewById(R.id.btnSettings)
        btnAbout = view.findViewById(R.id.btnAbout)
        ivAvatar = view.findViewById(R.id.ivAvatar)
        tvBorrowedCount = view.findViewById(R.id.tvBorrowedCount)
        tvTotalBooksCount = view.findViewById(R.id.tvTotalBooksCount)
    }

    private fun setupApi() {
        apiService = ApiClient.getRetrofit(requireContext()).create(ApiService::class.java)
        tokenManager = TokenManager(requireContext())
    }

    private fun setupClickListeners() {
        btnLogout.setOnClickListener {
            logout()
        }
        
        btnEditProfile.setOnClickListener {
            // TODO: Implement edit profile functionality
            Toast.makeText(requireContext(), "Chức năng chỉnh sửa thông tin đang được phát triển", Toast.LENGTH_SHORT).show()
        }
        
        btnChangePassword.setOnClickListener {
            // TODO: Implement change password functionality
            Toast.makeText(requireContext(), "Chức năng đổi mật khẩu đang được phát triển", Toast.LENGTH_SHORT).show()
        }
        
        btnSettings.setOnClickListener {
            // TODO: Implement settings functionality
            Toast.makeText(requireContext(), "Chức năng cài đặt đang được phát triển", Toast.LENGTH_SHORT).show()
        }
        
        btnAbout.setOnClickListener {
            // TODO: Implement about functionality
            Toast.makeText(requireContext(), "Thư viện USTH v1.0.0", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserProfile() {
        // Show loading state
        tvUsername.text = "Loading..."
        tvEmail.text = "Loading..."
        tvRole.text = "Loading..."
        
        // Get user ID from JWT token
        val token = tokenManager.getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show()
            logout()
            return
        }
        
        try {
            // Decode JWT token to get user ID
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payload = parts[1]
                val decoded = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
                val json = JSONObject(String(decoded))
                userId = json.optString("id")
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Lỗi khi đọc thông tin đăng nhập", Toast.LENGTH_SHORT).show()
            logout()
            return
        }
        
        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show()
            logout()
            return
        }
        
        // Load user profile from API
        lifecycleScope.launch {
            try {
                val user = apiService.getUser(userId!!)
                currentUser = user
                displayUserProfile(user)
                loadUserStats()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi khi tải thông tin người dùng: ${e.message}", Toast.LENGTH_SHORT).show()
                // Fallback to basic info
                val role = tokenManager.getRole()
                tvRole.text = "Role: ${role ?: "Unknown"}"
                tvUsername.text = "Người dùng"
                tvEmail.text = "Không thể tải thông tin"
            }
        }
    }
    
    private fun displayUserProfile(user: User) {
        tvUsername.text = user.name ?: "Người dùng"
        tvEmail.text = user.email ?: "Không có email"
        tvRole.text = "Role: ${user.role ?: "Unknown"}"
        
        // TODO: Load profile picture if available
        // if (!user.profilePicture.isNullOrEmpty()) {
        //     // Load image using Glide or similar library
        // }
    }
    
    private fun loadUserStats() {
        if (userId.isNullOrEmpty()) return
        
        lifecycleScope.launch {
            try {
                // Load borrowed books count
                val loans = apiService.getUserLoans(userId!!)
                val borrowedCount = loans.count { it.status == "borrowed" }
                tvBorrowedCount.text = borrowedCount.toString()
                
                // Load total books count (this would be from a different API)
                // For now, just show placeholder
                tvTotalBooksCount.text = "0"
                
            } catch (e: Exception) {
                tvBorrowedCount.text = "0"
                tvTotalBooksCount.text = "0"
            }
        }
    }

    private fun logout() {
        // Clear stored token and role
        tokenManager.saveToken("")
        tokenManager.saveRole("")
        
        // Navigate to login
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}