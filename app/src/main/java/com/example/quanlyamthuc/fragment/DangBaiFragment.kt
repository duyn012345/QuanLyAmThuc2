package com.example.quanlyamthuc.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlyamthuc.LoginActivity
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.adapter.BaiDangAdapter
import com.example.quanlyamthuc.model.DangBai
import com.example.quanlyamthuc.services.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DangBaiFragment : Fragment() {

    private val REQUEST_CODE_LOGIN = 1001
    private val REQUEST_CODE_IMAGE_PICK = 1002

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BaiDangAdapter
    private val listBaiDang = mutableListOf<DangBai>()

    private var selectedImageUri: Uri? = null
    private var currentDialog: AlertDialog? = null
    private var progressDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dang_bai, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewBaiDang)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        adapter = BaiDangAdapter(listBaiDang)
        recyclerView.adapter = adapter

        docDuLieuFirebase()

        view.findViewById<ImageButton>(R.id.btnThemBaiDang).setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(context, "Bạn cần đăng nhập để thêm bài đăng", Toast.LENGTH_SHORT).show()
                startActivityForResult(
                    Intent(context, LoginActivity::class.java),
                    REQUEST_CODE_LOGIN
                )
            } else {
                showDialogAddPost(currentUser)
            }
        }

        return view
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        when (requestCode) {
//            REQUEST_CODE_LOGIN -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    FirebaseAuth.getInstance().currentUser?.let {
//                        showDialogAddPost(it)
//                    }
//                }
//            }
//            REQUEST_CODE_IMAGE_PICK -> {
//                if (resultCode == Activity.RESULT_OK && data != null) {
//                    selectedImageUri = data.data
//                    currentDialog?.findViewById<ImageView>(R.id.imgSelectedImage)?.let { imageView ->
//                        imageView.setImageURI(selectedImageUri)
//                        imageView.visibility = View.VISIBLE
//                    }
//                }
//            }
//        }
//    }

    private fun showDialogAddPost(currentUser: FirebaseUser) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_them_bai_dang, null)
        val imgSelected = dialogView.findViewById<ImageView>(R.id.imgSelectedImage)
        imgSelected.visibility = View.GONE

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Thêm", null)
            .setNegativeButton("Hủy", null)
            .create()

        currentDialog = dialog

        dialog.setOnShowListener {
            val btnThem = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val btnHuy = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            btnThem.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorNegative))
            btnHuy.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))

            dialogView.findViewById<Button>(R.id.btnChooseImage).setOnClickListener {
                openImagePicker()
            }

            btnThem.setOnClickListener {
                val tenMonAn = dialogView.findViewById<EditText>(R.id.edtTenMonAn).text.toString()
                val tinhThanh = dialogView.findViewById<EditText>(R.id.edtTinhThanh).text.toString()
                val noiDung = dialogView.findViewById<EditText>(R.id.edtNoiDung).text.toString()

                if (selectedImageUri == null) {
                    Toast.makeText(context, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (tenMonAn.isBlank() || tinhThanh.isBlank() || noiDung.isBlank()) {
                    Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                uploadImageAndCreatePost(currentUser, tenMonAn, tinhThanh, noiDung)
            }
        }

        dialog.show()
    }
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        }
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK)
    }

    private fun uploadImageAndCreatePost(
        currentUser: FirebaseUser,
        tenMonAn: String,
        tinhThanh: String,
        noiDung: String
    ) {
        if (selectedImageUri == null) {
            Log.e("Upload", "selectedImageUri is null")
            Toast.makeText(context, "Không có ảnh được chọn", Toast.LENGTH_SHORT).show()
            return
        }

        showProgressDialog("Đang tải ảnh lên...")
        Log.d("Upload", "Starting upload with URI: $selectedImageUri")

        FirebaseService.uploadImage(requireContext(), selectedImageUri!!) { imageUrl ->
            requireActivity().runOnUiThread {
                dismissProgressDialog()

                if (imageUrl != null) {
                    Log.d("Upload", "Image uploaded successfully. URL: $imageUrl")
                    val idbd = FirebaseDatabase.getInstance().reference.push().key ?: ""
                    val baiDang = DangBai(
                        idbd = idbd,
                        idnd = currentUser.uid,
                        tenmonan = tenMonAn,
                        tinhthanh = tinhThanh,
                        noidung = noiDung,
                        hinhanh_ma = imageUrl,
                        so_like = "0",
                        created_at = layGioHienTai(),
                        tenNguoiDung = currentUser.displayName ?: "Người dùng"
                    )

                    val databaseRef = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference("2/data")

                    val path = databaseRef.toString().substringAfter("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    Log.d("Firebase", "Attempting to save post at path: $path/$idbd")
                    
                    databaseRef.child(idbd)
                        .setValue(baiDang)
                        .addOnSuccessListener {
                            Log.d("Firebase", "Post saved successfully")
                            Toast.makeText(
                                context,
                                "Đã thêm bài đăng thành công",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Error saving post", e)
                            Toast.makeText(
                                context,
                                "Lỗi khi thêm bài đăng: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Log.e("Upload", "Image upload failed")
                    Toast.makeText(context, "Lỗi khi tải ảnh lên", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun showProgressDialog(message: String) {
        progressDialog?.dismiss()
        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage(message)
            setCancelable(false)
            show()
        }
    }

    private fun dismissProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                // Hiển thị ảnh đã chọn
                currentDialog?.findViewById<ImageView>(R.id.imgSelectedImage)?.apply {
                    visibility = View.VISIBLE
                    setImageURI(uri)
                }
            }
        }
    }

    private fun layGioHienTai(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date())
    }

    private fun docDuLieuFirebase() {
        FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("2/data")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listBaiDang.clear()
                    snapshot.children.mapNotNull { it.getValue(DangBai::class.java) }
                        .forEach { listBaiDang.add(it) }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Lỗi đọc dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}


//class DangBaiFragment : Fragment() {
//
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: BaiDangAdapter
//    private val listBaiDang = mutableListOf<DangBai>()
//
//    private val REQUEST_CODE_LOGIN = 1001 // Define a constant for the login request code
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val view = inflater.inflate(R.layout.fragment_dang_bai, container, false)
//
//        recyclerView = view.findViewById(R.id.recyclerViewBaiDang)
//        recyclerView.layoutManager = GridLayoutManager(context, 2)
//        adapter = BaiDangAdapter(listBaiDang)
//        recyclerView.adapter = adapter
//
//        docDuLieuFirebase()
//
//        // Gắn sự kiện thêm bài đăng
//        val btnThemBaiDang = view.findViewById<ImageButton>(R.id.btnThemBaiDang)
//        btnThemBaiDang.setOnClickListener {
//            // Trong btnThemBaiDang.setOnClickListener
//            val currentUser = FirebaseAuth.getInstance().currentUser
//            Log.d("DangBaiFragment", "Current user: $currentUser")
//
//            if (currentUser == null) {
//                // Nếu chưa đăng nhập, yêu cầu đăng nhập
//                Toast.makeText(context, "Bạn cần đăng nhập để thêm bài đăng", Toast.LENGTH_SHORT).show()
//                // Mở màn hình đăng nhập và chờ kết quả
//                val intent = Intent(context, LoginActivity::class.java)
//                startActivityForResult(intent, REQUEST_CODE_LOGIN)
//            } else {
//                // Nếu đã đăng nhập, hiển thị hộp thoại thêm bài đăng
//                Log.d("DangBaiFragment", "Người dùng đã đăng nhập: ${currentUser.displayName}")
//                showDialogAddPost(currentUser)
//            }
//
//        }
//
//        return view
//    }
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_CODE_LOGIN && resultCode == Activity.RESULT_OK) {
//            val currentUser = FirebaseAuth.getInstance().currentUser
//            Log.d("DangBaiFragment", "Kết quả đăng nhập người dùng: ${currentUser?.displayName}")
//            if (currentUser != null) {
//                showDialogAddPost(currentUser)
//            }
//        }
//    }
//
//    private fun showDialogAddPost(currentUser: FirebaseUser) {
//        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_them_bai_dang, null)
//        val dialog = AlertDialog.Builder(requireContext())
//            .setView(dialogView)
//            .setPositiveButton("Thêm", null) // Để set sau
//            .setNegativeButton("Hủy", null)
//            .create()
//
//        dialog.setOnShowListener {
//            val btnThem = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
//            val btnHuy = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
//
//            btnThem.setTextColor(resources.getColor(R.color.colorNegative, null)) // đổi thành màu bạn muốn
//            btnHuy.setTextColor(resources.getColor(android.R.color.holo_red_dark, null)) // hoặc custom màu
//
//            btnThem.setOnClickListener {
//                val tenMonAn = dialogView.findViewById<EditText>(R.id.edtTenMonAn).text.toString()
//                val tinhThanh = dialogView.findViewById<EditText>(R.id.edtTinhThanh).text.toString()
//                val noiDung = dialogView.findViewById<EditText>(R.id.edtNoiDung).text.toString()
//                val hinhAnh = dialogView.findViewById<EditText>(R.id.edtHinhAnh).text.toString()
//
//                val idbd = FirebaseDatabase.getInstance().reference.push().key ?: ""
//                val idnd = currentUser.uid
//
//                val baiDang = DangBai(
//                    idbd = idbd,
//                    idnd = idnd,
//                    tenmonan = tenMonAn,
//                    tinhthanh = tinhThanh,
//                    noidung = noiDung,
//                    hinhanh_ma = hinhAnh,
//                    so_like = "0",
//                    created_at = layGioHienTai(),
//                    tenNguoiDung = currentUser.displayName ?: "Người dùng"
//                )
//
//                FirebaseDatabase
//                    .getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
//                    .getReference("2/data")
//                    .child(idbd)
//                    .setValue(baiDang)
//                    .addOnSuccessListener {
//                        Toast.makeText(context, "Đã thêm bài đăng", Toast.LENGTH_SHORT).show()
//                        dialog.dismiss()
//                    }
//                    .addOnFailureListener {
//                        Toast.makeText(context, "Lỗi khi thêm bài đăng", Toast.LENGTH_SHORT).show()
//                    }
//            }
//        }
//
//        dialog.show()
//
//    }
//    private fun layGioHienTai(): String {
//        val currentTimeMillis = System.currentTimeMillis()
//        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
//        return sdf.format(java.util.Date(currentTimeMillis))
//    }
//
//    private fun docDuLieuFirebase() {
//        val ref = FirebaseDatabase
//            .getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
//            .getReference("2/data")
//
//        ref.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                listBaiDang.clear()
//                for (item in snapshot.children) {
//                    val baiDang = item.getValue(DangBai::class.java)
//                    baiDang?.let { listBaiDang.add(it) }
//                }
//                adapter.notifyDataSetChanged()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(context, "Lỗi đọc dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//}
