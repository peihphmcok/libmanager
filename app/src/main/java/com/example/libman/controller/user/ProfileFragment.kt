package com.example.libman.controller.user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.libman.R
import com.example.libman.controller.auth.LoginActivity
import com.example.libman.models.User
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.example.libman.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

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
    private lateinit var avatarCard: com.google.android.material.card.MaterialCardView
    private lateinit var tvBorrowedCount: TextView
    private lateinit var tvTotalBooksCount: TextView

    private var currentUser: User? = null
    private var userId: String? = null
    private var selectedImageUri: Uri? = null
    
    // Image picker - using GetContent instead of StartActivityForResult
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri ->
        if (imageUri != null) {
            android.util.Log.d("ProfileFragment", "Image selected: $imageUri")
            uploadProfileImage(imageUri)
        }
    }

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
        avatarCard = view.findViewById(R.id.avatarCard)
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
        
        // Avatar click listener
        avatarCard.setOnClickListener {
            openImagePicker()
        }

        btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }


        btnSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Chức năng cài đặt đang được phát triển", Toast.LENGTH_SHORT).show()
        }


        btnAbout.setOnClickListener {
            val intent = Intent(requireContext(), AboutAppActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserProfileFromToken() {
        // Show loading state
        tvUsername.text = "Loading..."
        tvEmail.text = "Loading..."
        tvRole.text = "Loading..."

        android.util.Log.d("ProfileFragment", "Loading profile from token since userId is null")
        
        lifecycleScope.launch {
            try {
                // Try to get current user info from a general endpoint
                // Since we don't have userId, we'll try to get user info from token
                // This is a fallback method - ideally we should have userId stored
                
                // For now, show basic info from TokenManager
                val role = tokenManager.getRole()
                val userName = tokenManager.getUserName()
                val userEmail = tokenManager.getUserEmail()
                
                if (!userName.isNullOrEmpty() && !userEmail.isNullOrEmpty()) {
                    tvUsername.text = userName
                    tvEmail.text = userEmail
                    tvRole.text = "Role: ${role ?: "Unknown"}"
                    android.util.Log.d("ProfileFragment", "Loaded profile from TokenManager cache")
                } else {
                    android.util.Log.w("ProfileFragment", "No cached user info available - showing placeholder")
                    tvUsername.text = "Người dùng"
                    tvEmail.text = "Email không khả dụng"
                    tvRole.text = "Role: Unknown"
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileFragment", "Error loading profile from token: ${e.message}")
                tvUsername.text = "Lỗi tải thông tin"
                tvEmail.text = "Không thể tải"
                tvRole.text = "Role: Unknown"
            }
        }
    }

    private fun loadUserProfile() {
        // Show loading state
        tvUsername.text = "Loading..."
        tvEmail.text = "Loading..."
        tvRole.text = "Loading..."

        // Get user ID from TokenManager (stored during login)
        userId = tokenManager.getUserId()
        val token = tokenManager.getToken()
        
        android.util.Log.d("ProfileFragment", "=== DEBUG AUTH CHECK ===")
        android.util.Log.d("ProfileFragment", "User ID: '$userId'")
        android.util.Log.d("ProfileFragment", "Token: '${token?.substring(0, 50)}...'")
        android.util.Log.d("ProfileFragment", "TokenManager.isLoggedIn(): ${tokenManager.isLoggedIn()}")
        android.util.Log.d("ProfileFragment", "=== END DEBUG AUTH CHECK ===")
        
        if (token.isNullOrEmpty()) {
            android.util.Log.e("ProfileFragment", "No token found - redirecting to login")
            Toast.makeText(requireContext(), "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show()
            logout()
            return
        }
        
        if (userId.isNullOrEmpty()) {
            android.util.Log.w("ProfileFragment", "User ID is null but token exists - trying to load from API")
            // Try to get user info from API using token
            loadUserProfileFromToken()
            return
        }
        
        android.util.Log.d("ProfileFragment", "Loading profile for user ID: $userId")
        
        // Debug all TokenManager data
        tokenManager.debugUserInfo()

        // Load user profile from API
        lifecycleScope.launch {
            try {
                android.util.Log.d("ProfileFragment", "Calling API: getUser($userId)")
                val userResponse = apiService.getUser(userId!!)
                android.util.Log.d("ProfileFragment", "API response: userResponse = $userResponse")
                
                currentUser = userResponse.user
                displayUserProfile(userResponse.user)
                loadUserStats()
            } catch (e: Exception) {
                android.util.Log.e("ProfileFragment", "Error loading user profile: ${e.message}", e)
                Toast.makeText(requireContext(), "Lỗi khi tải thông tin người dùng: ${e.message}", Toast.LENGTH_SHORT).show()
                
                // Fallback to basic info from TokenManager
                val role = tokenManager.getRole()
                val userName = tokenManager.getUserName()
                val userEmail = tokenManager.getUserEmail()
                
                tvRole.text = "Role: ${role ?: "Unknown"}"
                tvUsername.text = userName ?: "Người dùng"
                tvEmail.text = userEmail ?: "Không có email"
            }
        }
    }

    private fun displayUserProfile(user: User) {
        tvUsername.text = user.fullname ?: user.name ?: "Người dùng"
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
                val response = apiService.getUserLoans(userId!!)
                val loans = response.loans ?: emptyList()
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
        // Clear stored token and role properly
        tokenManager.clearUserData()

        // Navigate to login
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
    
    private fun openImagePicker() {
        try {
            android.util.Log.d("ProfileFragment", "Opening image picker...")
            imagePickerLauncher.launch("image/*")
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "Error opening image picker: ${e.message}", e)
            Toast.makeText(requireContext(), "Lỗi khi mở chọn ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun uploadProfileImage(imageUri: Uri) {
        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show loading
        Toast.makeText(requireContext(), "Đang upload ảnh đại diện...", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            try {
                // Get file from URI
                val inputStream = requireContext().contentResolver.openInputStream(imageUri)
                val file = File(requireContext().cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
                file.outputStream().use { output ->
                    inputStream?.copyTo(output)
                }
                
                // Create multipart body
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("profile", file.name, requestFile)
                
                // Upload to API
                val response = apiService.uploadUserProfilePicture(userId!!, body)
                
                // Update avatar immediately
                ivAvatar.setImageURI(imageUri)
                Toast.makeText(requireContext(), "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                android.util.Log.e("ProfileFragment", "Error uploading profile image: ${e.message}", e)
                Toast.makeText(requireContext(), "Lỗi khi upload ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.password_change, null)
        val etOldPassword = dialogView.findViewById<android.widget.EditText>(R.id.etOldPassword)
        val etNewPassword = dialogView.findViewById<android.widget.EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<android.widget.EditText>(R.id.etConfirmPassword)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Đổi mật khẩu")
            .setView(dialogView)
            .setPositiveButton("Xác nhận", null)
            .setNegativeButton("Hủy", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val oldPassword = etOldPassword.text.toString().trim()
                val newPassword = etNewPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim()

                if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(requireContext(), "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // ✅ Call API to change password
                lifecycleScope.launch {
                    try {
                        val request = ApiService.ChangePasswordRequest(
                            userId = userId!!,
                            oldPassword = oldPassword,
                            newPassword = newPassword
                        )
                        val response = apiService.changePassword(request)

                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Lỗi: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }

        dialog.show()
    }
    private fun showEditProfileDialog() {
        if (currentUser == null || userId == null) {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.profile_edit, null)
        val etFullname = dialogView.findViewById<android.widget.EditText>(R.id.etFullname)
        val etEmail = dialogView.findViewById<android.widget.EditText>(R.id.etEmail)
        val ivProfilePicture = dialogView.findViewById<android.widget.ImageView>(R.id.ivProfilePicture)
        val btnChangeProfilePicture = dialogView.findViewById<android.widget.Button>(R.id.btnChangeProfilePicture)

        etFullname.setText(currentUser?.fullname ?: "")
        etEmail.setText(currentUser?.email ?: "")
        
        // Load current profile picture if available
        // TODO: Load profile picture using Glide or similar library
        
        // Set click listener for profile picture upload
        btnChangeProfilePicture.setOnClickListener {
            selectProfilePicture()
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Chỉnh sửa thông tin")
            .setView(dialogView)
            .setPositiveButton("Lưu", null)
            .setNegativeButton("Hủy", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val newName = etFullname.text.toString().trim()
                val newEmail = etEmail.text.toString().trim()

                if (newName.isEmpty() || newEmail.isEmpty()) {
                    Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    try {
                        // Update user info
                        val response = apiService.updateUser(
                            ApiService.UpdateUserRequest(
                                userId = userId!!,
                                fullname = newName,
                                email = newEmail
                            )
                        )

                        if (response.isSuccessful) {
                            val updatedUser = response.body()
                            if (updatedUser != null) {
                                currentUser = updatedUser
                                displayUserProfile(updatedUser)
                            }
                            
                            // Upload profile picture if selected
                            selectedImageUri?.let { uri ->
                                try {
                                    val file = File(uri.path ?: "")
                                    if (file.exists()) {
                                        uploadProfilePicture(file)
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("ProfileFragment", "Error handling image file: ${e.message}", e)
                                }
                            }
                            
                            Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Lỗi cập nhật: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        dialog.show()
    }
    
    private fun selectProfilePicture() {
        try {
            android.util.Log.d("ProfileFragment", "Opening image picker from dialog...")
            imagePickerLauncher.launch("image/*")
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "Error opening image picker: ${e.message}", e)
            Toast.makeText(requireContext(), "Lỗi khi mở chọn ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateProfilePictureInDialog(uri: Uri) {
        // This will be called when user selects an image
        // The actual upload will happen when user clicks "Lưu" in the dialog
        Toast.makeText(requireContext(), "Ảnh đã được chọn. Nhấn 'Lưu' để upload.", Toast.LENGTH_SHORT).show()
    }
    
    private fun uploadProfilePicture(file: File) {
        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val profilePicture = MultipartBody.Part.createFormData("profile", file.name, requestFile)
                
                android.util.Log.d("ProfileFragment", "Uploading profile picture for user: $userId")
                val response = apiService.uploadUserProfilePicture(userId!!, profilePicture)
                android.util.Log.d("ProfileFragment", "Upload response: $response")
                
                Toast.makeText(requireContext(), "Upload ảnh profile thành công!", Toast.LENGTH_SHORT).show()
                
                // Reload user profile to get updated info
                loadUserProfile()
                
            } catch (e: Exception) {
                android.util.Log.e("ProfileFragment", "Error uploading profile picture: ${e.message}", e)
                Toast.makeText(requireContext(), "Lỗi khi upload ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}