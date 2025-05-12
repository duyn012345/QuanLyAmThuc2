package com.example.quanlyamthuc.admin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quanlyamthuc.LoginActivity
import com.example.quanlyamthuc.adapter.UserAdapter
import com.example.quanlyamthuc.databinding.DialogUserBinding
import com.example.quanlyamthuc.databinding.FragmentUserBinding
import com.example.quanlyamthuc.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<UserModel>()
    private var originalUserList = mutableListOf<UserModel>() // Danh sách gốc để lưu trữ

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        // Khởi tạo Adapter với emptyList()
        userAdapter = UserAdapter(mutableListOf()) { user -> deleteUser(user) }
        binding.userRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = userAdapter
        }


//        binding.provinceRecyclerView.apply {
//            layoutManager = GridLayoutManager(context, 2)
//            adapter = provinceAdapter
//        }
        // Thiết lập SearchView
        // Thiết lập SearchView
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsers(newText.orEmpty())
                return true
            }
        })


        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            firestore.collection("nguoidung").document(currentUser.uid).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val role = snapshot.getString("role") ?: "user"
                        if (role == "admin") {
                            fetchUsers()
                        } else {
                            Toast.makeText(context, "Chỉ admin mới có thể xem danh sách người dùng", Toast.LENGTH_SHORT).show()
                            userList.clear()
                            userAdapter.updateList(userList)
                        }
                    } else {
                        Toast.makeText(context, "Thông tin người dùng không tồn tại. Vui lòng liên hệ quản trị viên.", Toast.LENGTH_LONG).show()
                        userList.clear()
                        userAdapter.updateList(userList)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Không thể kiểm tra vai trò: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("UserFragment", "Failed to check role: ${e.message}")
                }
        } else {
            Toast.makeText(context, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
            userList.clear()
            userAdapter.updateList(userList)
            activity?.let {
                startActivity(Intent(it, LoginActivity::class.java))
                it.finish()
            }
        }
    }

    private fun fetchUsers() {
        firestore.collection("nguoidung").get()
            .addOnSuccessListener { querySnapshot ->
                // Tạo list tạm để tránh đồng bộ hóa phức tạp
                val newUserList = mutableListOf<UserModel>()
                val newOriginalList = mutableListOf<UserModel>()

                for (document in querySnapshot.documents) {
                    try {
                        val user = document.toObject(UserModel::class.java)?.apply {
                            idnd = document.id // Gán ID từ document
                        }
                        user?.let {
                            newUserList.add(it)
                            newOriginalList.add(it)
                            Log.d("UserFragment", "Added user: ${it.name}, ID: ${it.idnd}")
                        }
                    } catch (e: Exception) {
                        Log.e("UserFragment", "Error parsing document ${document.id}", e)
                    }
                }

                // Cập nhật dữ liệu trên main thread
                activity?.runOnUiThread {
                    // Đồng bộ hóa việc cập nhật list
                    synchronized(userList) {
                        userList.clear()
                        userList.addAll(newUserList)
                        originalUserList.clear()
                        originalUserList.addAll(newOriginalList)
                    }

                    // Cập nhật adapter với bản copy của list
                    userAdapter.updateList(ArrayList(userList))
                    updateEmptyView()

                    Log.d("UserFragment", "Update completed. Total users: ${userList.size}")
                }
            }
            .addOnFailureListener { e ->
                activity?.runOnUiThread {
                    Toast.makeText(context, "Lỗi tải dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("UserFragment", "Fetch failed", e)
                    userList.clear()
                    originalUserList.clear()
                    userAdapter.updateList(emptyList())
                    updateEmptyView()
                }
            }
    }
    private fun filterUsers(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalUserList.toList() // Trả về bản copy
        } else {
            originalUserList.filter { user ->
                user.name?.contains(query, ignoreCase = true) == true
            }
        }

        userAdapter.updateList(filteredList)
        updateEmptyView()
    }

    private fun updateEmptyView() {
        binding.emptyView.visibility = if (userAdapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    private fun deleteUser(user: UserModel) {
        user.idnd?.let { idnd ->
            firestore.collection("nguoidung").document(idnd).delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show()
                    fetchUsers()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to delete user: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("UserFragment", "Failed to delete user: ${e.message}")
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


//    private fun showAddDialog() {
//        val dialogBinding = DialogUserBinding.inflate(LayoutInflater.from(context))
//        val dialog = AlertDialog.Builder(requireContext())
//            .setTitle("Add User")
//            .setView(dialogBinding.root)
//            .setPositiveButton("Add") { _, _ ->
//                val name = dialogBinding.edtname.text.toString().trim()
//                val email = dialogBinding.edtemail.text.toString().trim()
//                val role = dialogBinding.edtrole.text.toString().trim()
//
//                if (name.isBlank() || email.isBlank() || role.isBlank()) {
//                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
//                    return@setPositiveButton
//                }
//
//                val newUser = UserModel(
//                    name = name,
//                    email = email,
//                    role = role
//                )
//                firestore.collection("nguoidung").add(newUser)
//                    .addOnSuccessListener { documentReference ->
//                        val userId = documentReference.id
//                        newUser.idnd = userId
//                        firestore.collection("nguoidung").document(userId).set(newUser)
//                        Toast.makeText(context, "User added successfully", Toast.LENGTH_SHORT).show()
//                        fetchUsers()
//                    }
//                    .addOnFailureListener { e ->
//                        Toast.makeText(context, "Failed to add user: ${e.message}", Toast.LENGTH_LONG).show()
//                        Log.e("UserFragment", "Failed to add user: ${e.message}")
//                    }
//            }
//            .setNegativeButton("Cancel", null)
//            .create()
//
//        dialog.show()
//    }

// private fun showEditDialog(user: UserModel) {
//        val dialogBinding = DialogUserBinding.inflate(LayoutInflater.from(context))
//        dialogBinding.edtname.setText(user.name)
//        dialogBinding.edtemail.setText(user.email)
//        dialogBinding.edtrole.setText(user.role)
//        dialogBinding.edtrole.setText(user.role)
//        dialogBinding.edtrole.setText(user.role)
//
//        val dialog = AlertDialog.Builder(requireContext())
//            .setTitle("Edit User")
//            .setView(dialogBinding.root)
//            .setPositiveButton("Save") { _, _ ->
//                val name = dialogBinding.edtname.text.toString().trim()
//                val email = dialogBinding.edtemail.text.toString().trim()
//                val role = dialogBinding.edtrole.text.toString().trim()
//
//                if (name.isBlank() || email.isBlank() || role.isBlank()) {
//                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
//                    return@setPositiveButton
//                }
//
//                val updatedUser = UserModel(
//                    idnd = user.idnd,
//                    name = name,
//                    email = email,
//                    role = role
//                )
//                user.idnd?.let { idnd ->
//                    firestore.collection("nguoidung").document(idnd).set(updatedUser)
//                        .addOnSuccessListener {
//                            Toast.makeText(context, "User updated successfully", Toast.LENGTH_SHORT).show()
//                            fetchUsers()
//                        }
//                        .addOnFailureListener { e ->
//                            Toast.makeText(context, "Failed to update user: ${e.message}", Toast.LENGTH_LONG).show()
//                            Log.e("UserFragment", "Failed to update user: ${e.message}")
//                        }
//                }
//            }
//            .setNegativeButton("Cancel", null)
//            .create()
//
//        dialog.show()
//    }