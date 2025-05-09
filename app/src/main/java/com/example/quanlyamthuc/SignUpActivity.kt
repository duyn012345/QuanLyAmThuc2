package com.example.quanlyamthuc

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quanlyamthuc.databinding.ActivitySignUpBinding
import com.example.quanlyamthuc.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var userName: String
    private lateinit var repassword: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private val binding: ActivitySignUpBinding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.createUserButton.setOnClickListener {
            userName = binding.name.text.toString().trim()
            email = binding.emailAdd.text.toString().trim()
            password = binding.password.text.toString().trim()
            repassword = binding.repassword.text.toString().trim()

            if (userName.isBlank() || email.isBlank() || password.isBlank() || repassword.isBlank()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this, "Mật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show()
            } else if (password != repassword) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
            } else {
                createAccount(email, password)
            }
        }

        binding.alreadyHaveAccountButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.buttonGG.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun createAccount(email: String, password: String) {
        Log.d("DEBUG", "Bắt đầu đăng ký tài khoản với $email")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show()
                    Log.d("DEBUG", "Tạo tài khoản thành công → gọi saveUserData()")
                    saveUserData()
                } else {
                    Toast.makeText(this, "Tạo tài khoản thất bại", Toast.LENGTH_SHORT).show()
                    Log.e("Firebase", "Lỗi khi tạo tài khoản: ${task.exception?.message}")
                }
            }
    }

    private fun saveUserData() {
        val user = UserModel(name = userName, email = email, role = "user")

        val userId = auth.currentUser?.uid
        Log.d("DEBUG", "Gọi saveUserData() - UID hiện tại: $userId")

        if (userId == null) {
            Log.e("DEBUG", "auth.currentUser == null, không thể lưu dữ liệu")
            return
        }

        // Lưu dữ liệu vào Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("nguoidung").document(userId).set(user)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("Firebase", "Lưu dữ liệu người dùng thành công: $userId")
                    auth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("Firebase", "Lỗi khi lưu dữ liệu: ${it.exception?.message}")
                    Toast.makeText(this, "Lưu dữ liệu thất bại", Toast.LENGTH_SHORT).show()
                }
            }
    }



}
