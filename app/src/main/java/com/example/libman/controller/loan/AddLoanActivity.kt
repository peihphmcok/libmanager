package com.example.libman.controller.loan

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.libman.R
import com.example.libman.models.Book
import com.example.libman.models.Loan
import com.example.libman.models.User
import com.example.libman.models.BorrowRequest
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddLoanActivity : AppCompatActivity() {

    private lateinit var etBook: MaterialAutoCompleteTextView
    private lateinit var etUser: MaterialAutoCompleteTextView
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var apiService: ApiService
    
    private var books: List<Book> = emptyList()
    private var users: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_loan)

        initViews()
        setupClickListeners()
        apiService = ApiClient.getRetrofit(this).create(ApiService::class.java)
        
        loadData()
    }

    private fun initViews() {
        etBook = findViewById(R.id.etBook)
        etUser = findViewById(R.id.etUser)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            createLoan()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Load books and users in parallel
                val booksResult = withContext(Dispatchers.IO) {
                    apiService.getBooks()
                }
                val usersResult = withContext(Dispatchers.IO) {
                    apiService.getUsers()
                }
                
                books = booksResult
                users = usersResult
                
                setupAutoComplete()
                
            } catch (e: Exception) {
                Toast.makeText(this@AddLoanActivity, "Lỗi khi tải dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupAutoComplete() {
        // Setup book autocomplete
        val bookTitles = books.map { "${it.title} - ${it.author}" }
        val bookAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, bookTitles)
        etBook.setAdapter(bookAdapter)
        
        // Setup user autocomplete
        val userNames = users.map { "${it.name} (${it.email})" }
        val userAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, userNames)
        etUser.setAdapter(userAdapter)
    }

    private fun createLoan() {
        val selectedBook = etBook.text.toString().trim()
        val selectedUser = etUser.text.toString().trim()

        if (selectedBook.isEmpty()) {
            etBook.error = "Vui lòng chọn sách"
            return
        }

        if (selectedUser.isEmpty()) {
            etUser.error = "Vui lòng chọn người mượn"
            return
        }

        // Find selected book and user
        val book = books.find { "${it.title} - ${it.author}" == selectedBook }
        val user = users.find { "${it.name} (${it.email})" == selectedUser }

        if (book == null) {
            etBook.error = "Không tìm thấy sách"
            return
        }

        if (user == null) {
            etUser.error = "Không tìm thấy người dùng"
            return
        }

        // For now, assume all books are available
        // if (book.available != true) {
        //     Toast.makeText(this, "Sách này hiện không có sẵn", Toast.LENGTH_SHORT).show()
        //     return
        // }

        // For now, this activity is for admin to create loans for users
        // But backend only supports user self-borrowing
        // We'll use BorrowRequest with the selected book
        val borrowRequest = BorrowRequest(
            bookId = book.id!!,
            dueDate = java.util.Date(System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000) // 14 days
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                btnSave.isEnabled = false
                btnSave.text = "Đang tạo..."
                
                withContext(Dispatchers.IO) {
                    apiService.addLoan(borrowRequest)
                }
                
                Toast.makeText(this@AddLoanActivity, "Tạo phiếu mượn thành công!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(this@AddLoanActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnSave.isEnabled = true
                btnSave.text = "Tạo"
            }
        }
    }
}
