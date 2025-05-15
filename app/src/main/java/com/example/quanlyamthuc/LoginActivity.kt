package com.example.quanlyamthuc

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.quanlyamthuc.admin.HomeAdminActivity
import com.example.quanlyamthuc.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var email:String
    private lateinit var password:String
    private lateinit var auth:FirebaseAuth
    private lateinit var database:DatabaseReference

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        database = Firebase.database.reference

        binding.forgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        binding.loginButton.setOnClickListener {
            //get text form edittext
            email = binding.email.text.toString().trim()
            password = binding.password.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please Fill All Details", Toast.LENGTH_SHORT).show()
            } else {
                loginUser()
            }

        }
        binding.dontHaveAccountButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Google Sign-In with Google Identity Services
        binding.buttonGG.setOnClickListener {
//            signInWithGoogle()\
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
        // Xử lý sự kiện toggle password
        binding.passwordLayout.setEndIconOnClickListener {
            val editText = binding.password
            val selection = editText.selectionEnd // Giữ vị trí con trỏ

            if (editText.transformationMethod == PasswordTransformationMethod.getInstance()) {
                // Hiển thị mật khẩu
                editText.transformationMethod = null
                binding.passwordLayout.endIconDrawable = ContextCompat.getDrawable(this, R.drawable.ic_visibility_off)
            } else {
                // Ẩn mật khẩu
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.passwordLayout.endIconDrawable = ContextCompat.getDrawable(this, R.drawable.ic_visibility)
            }

            editText.setSelection(selection) // Khôi phục vị trí con trỏ
        }
    }

    private fun loginUser() {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
                checkUserRole(auth.currentUser)
            } else {
                Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Quên mật khẩu")
            .setMessage("Nhập email để nhận liên kết đặt lại mật khẩu")

        val input = EditText(this).apply {
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            hint = "Email"
            setPadding(32, 32, 32, 32)
        }

        builder.setView(input)
            .setPositiveButton("Gửi") { _, _ ->
                val email = input.text.toString().trim()
                if (email.isNotEmpty()) {
                    sendPasswordResetEmail(email)
                } else {
                    Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Kiểm tra email $email để đặt lại mật khẩu",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Lỗi: ${task.exception?.message ?: "Không thể gửi email"}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun checkUserRole(user: FirebaseUser?) {
        if (user != null) {
            val userId = user.uid
            val db = FirebaseFirestore.getInstance()
            db.collection("nguoidung").document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val role = documentSnapshot.getString("role") ?: "user"
                        if (role == "admin") {
                            startActivity(Intent(this, HomeAdminActivity::class.java))
                        } else {
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                        finish()
                    } else {
                        Toast.makeText(this, "Không tìm thấy người dùng trong Firestore", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Không thể kiểm tra vai trò: ${it.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Firebase", "Failed to check role: ${it.message}")
                }
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){

            Log.d("LoginActivity", "User already signed in: ${currentUser.uid}")
        }
    }

}