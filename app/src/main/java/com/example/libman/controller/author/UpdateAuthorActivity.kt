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

class UpdateAuthorActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etBio: TextInputEditText
    private lateinit var etNationality: TextInputEditText
    private lateinit var etBirthYear: TextInputEditText
    private lateinit var btnUpdate: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var apiService: ApiService
    
    private var authorId: String? = null
    private var currentAuthor: Author? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_author)

        // Get author data from intent
        authorId = intent.getStringExtra("author_id")
        currentAuthor = intent.getParcelableExtra("author")
        
        if (authorId == null || currentAuthor == null) {
            Toast.makeText(this, "Không có thông tin tác giả", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupClickListeners()
        populateFields()
        apiService = ApiClient.getRetrofit(this).create(ApiService::class.java)
    }

    private fun initViews() {
        etName = findViewById(R.id.etAuthorName)
        etBio = findViewById(R.id.etAuthorBio)
        etNationality = findViewById(R.id.etAuthorNationality)
        etBirthYear = findViewById(R.id.etAuthorBirthYear)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun populateFields() {
        currentAuthor?.let { author ->
            etName.setText(author.name)
            etBio.setText(author.bio)
            etNationality.setText(author.nationality)
            etBirthYear.setText(author.birthYear?.toString())
        }
    }

    private fun setupClickListeners() {
        btnUpdate.setOnClickListener {
            updateAuthor()
        }

        btnCancel.setOnClickListener {
            finish()
        }
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

        val updatedAuthor = Author(
            id = authorId,
            name = name,
            bio = bio.ifEmpty { null },
            nationality = nationality.ifEmpty { null },
            birthYear = birthYear
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                btnUpdate.isEnabled = false
                btnUpdate.text = "Đang cập nhật..."
                
                val response = withContext(Dispatchers.IO) {
                    apiService.updateAuthor(authorId!!, updatedAuthor)
                }
                
                Toast.makeText(this@UpdateAuthorActivity, "Cập nhật tác giả thành công!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(this@UpdateAuthorActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnUpdate.isEnabled = true
                btnUpdate.text = "Cập nhật"
            }
        }
    }
}
