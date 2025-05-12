package com.example.quanlyamthuc.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlyamthuc.LoginActivity
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.adapter.BaiDangAdapter
import com.example.quanlyamthuc.model.DangBai
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class DangBaiFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BaiDangAdapter
    private val listBaiDang = mutableListOf<DangBai>()

    private val REQUEST_CODE_LOGIN = 1001 // Define a constant for the login request code

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_dang_bai, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewBaiDang)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        adapter = BaiDangAdapter(listBaiDang)
        recyclerView.adapter = adapter

        docDuLieuFirebase()

        // Gắn sự kiện thêm bài đăng
        val btnThemBaiDang = view.findViewById<ImageButton>(R.id.btnThemBaiDang)
        btnThemBaiDang.setOnClickListener {
            // Trong btnThemBaiDang.setOnClickListener
            val currentUser = FirebaseAuth.getInstance().currentUser
            Log.d("DangBaiFragment", "Current user: $currentUser")

            if (currentUser == null) {
                // Nếu chưa đăng nhập, yêu cầu đăng nhập
                Toast.makeText(context, "Bạn cần đăng nhập để thêm bài đăng", Toast.LENGTH_SHORT).show()
                // Mở màn hình đăng nhập và chờ kết quả
                val intent = Intent(context, LoginActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_LOGIN)
            } else {
                // Nếu đã đăng nhập, hiển thị hộp thoại thêm bài đăng
                Log.d("DangBaiFragment", "Người dùng đã đăng nhập: ${currentUser.displayName}")
                showDialogAddPost(currentUser)
            }

        }

        return view
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_LOGIN && resultCode == Activity.RESULT_OK) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            Log.d("DangBaiFragment", "Kết quả đăng nhập người dùng: ${currentUser?.displayName}")
            if (currentUser != null) {
                showDialogAddPost(currentUser)
            }
        }
    }

    private fun showDialogAddPost(currentUser: FirebaseUser) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_them_bai_dang, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Thêm", null) // Để set sau
            .setNegativeButton("Hủy", null)
            .create()

        dialog.setOnShowListener {
            val btnThem = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val btnHuy = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            btnThem.setTextColor(resources.getColor(R.color.colorNegative, null)) // đổi thành màu bạn muốn
            btnHuy.setTextColor(resources.getColor(android.R.color.holo_red_dark, null)) // hoặc custom màu

            btnThem.setOnClickListener {
                val tenMonAn = dialogView.findViewById<EditText>(R.id.edtTenMonAn).text.toString()
                val tinhThanh = dialogView.findViewById<EditText>(R.id.edtTinhThanh).text.toString()
                val noiDung = dialogView.findViewById<EditText>(R.id.edtNoiDung).text.toString()
                val hinhAnh = dialogView.findViewById<EditText>(R.id.edtHinhAnh).text.toString()

                val idbd = FirebaseDatabase.getInstance().reference.push().key ?: ""
                val idnd = currentUser.uid

                val baiDang = DangBai(
                    idbd = idbd,
                    idnd = idnd,
                    tenmonan = tenMonAn,
                    tinhthanh = tinhThanh,
                    noidung = noiDung,
                    hinhanh_ma = hinhAnh,
                    so_like = "0",
                    created_at = layGioHienTai(),
                    tenNguoiDung = currentUser.displayName ?: "Người dùng"
                )

                FirebaseDatabase
                    .getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("2/data")
                    .child(idbd)
                    .setValue(baiDang)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Đã thêm bài đăng", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Lỗi khi thêm bài đăng", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        dialog.show()

    }
    private fun layGioHienTai(): String {
        val currentTimeMillis = System.currentTimeMillis()
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(currentTimeMillis))
    }

    private fun docDuLieuFirebase() {
        val ref = FirebaseDatabase
            .getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("2/data")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listBaiDang.clear()
                for (item in snapshot.children) {
                    val baiDang = item.getValue(DangBai::class.java)
                    baiDang?.let { listBaiDang.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Lỗi đọc dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
