package com.example.libman.controller.author

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.libman.R
import com.example.libman.models.Author
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditAuthorActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etBio: TextInputEditText
    private lateinit var etNationality: TextInputEditText
    private lateinit var etBirthYear: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnDelete: MaterialButton
    private lateinit var apiService: ApiService
    
    private var authorId: String? = null
    private var currentAuthor: Author? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_author)

        authorId = intent.getStringExtra("author_id")
        if (authorId == null) {
            Toast.makeText(this, "Không tìm thấy ID tác giả", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupClickListeners()
        apiService = ApiClient.getRetrofit(this).create(ApiService::class.java)
        
        loadAuthor()
    }

    private fun initViews() {
        etName = findViewById(R.id.etAuthorName)
        etBio = findViewById(R.id.etAuthorBio)
        etNationality = findViewById(R.id.etAuthorNationality)
        etBirthYear = findViewById(R.id.etAuthorBirthYear)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        btnDelete = findViewById(R.id.btnDelete)
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            updateAuthor()
        }

        btnCancel.setOnClickListener {
            finish()
        }
        
        btnDelete.setOnClickListener {
            deleteAuthor()
        }
    }

    private fun loadAuthor() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val author = withContext(Dispatchers.IO) {
                    apiService.getAuthor(authorId!!)
                }
                currentAuthor = author
                populateFields(author)
            } catch (e: Exception) {
                Toast.makeText(this@EditAuthorActivity, "Lỗi khi tải thông tin tác giả: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun populateFields(author: Author) {
        etName.setText(author.name)
        etBio.setText(author.bio)
        etNationality.setText(author.nationality)
        etBirthYear.setText(author.birthYear?.toString())
    }

    private fun updateAuthor() {
        val name = etName.text.toString().trim()
        val bio = etBio.text.toString().trim()
        val nationality = etNationality.text.toString().trim()
        val birthYearStr = etBirthYear.text.toString().trim()

        if (name.isEmpty()) {
            etName.error = "Vui lòng nhập tên tác giả"
            return
        }

        val birthYear = if (birthYearStr.isNotEmpty()) {
            try {
                birthYearStr.toInt()
            } catch (e: NumberFormatException) {
                etBirthYear.error = "Năm sinh không hợp lệ"
                return
            }
        } else null

        val updatedAuthor = currentAuthor?.copy(
            name = name,
            bio = bio.ifEmpty { null },
            nationality = nationality.ifEmpty { null },
            birthYear = birthYear
        ) ?: Author(
            name = name,
            bio = bio.ifEmpty { null },
            nationality = nationality.ifEmpty { null },
            birthYear = birthYear
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                btnSave.isEnabled = false
                btnSave.text = "Đang cập nhật..."
                
                withContext(Dispatchers.IO) {
                    apiService.updateAuthor(authorId!!, updatedAuthor)
                }
                
                Toast.makeText(this@EditAuthorActivity, "Cập nhật tác giả thành công!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(this@EditAuthorActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnSave.isEnabled = true
                btnSave.text = "Cập nhật"
            }
        }
    }

    private fun deleteAuthor() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                btnDelete.isEnabled = false
                btnDelete.text = "Đang xóa..."
                
                withContext(Dispatchers.IO) {
                    apiService.deleteAuthor(authorId!!)
                }
                
                Toast.makeText(this@EditAuthorActivity, "Xóa tác giả thành công!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(this@EditAuthorActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnDelete.isEnabled = true
                btnDelete.text = "Xóa"
            }
        }
    }
}