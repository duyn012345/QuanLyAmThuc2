package com.example.quanlyamthuc

import android.widget.Toast
import com.example.quanlyamthuc.model.UserModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import com.example.quanlyamthuc.services.FirebaseService
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.DocumentReference

class ProfileFragment : Fragment() {

    private lateinit var imgAvatar: ImageView
    private lateinit var txtName: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtPhone: TextView
    private lateinit var txtBio: TextView
    private lateinit var txtSoBaiDang: TextView
    private lateinit var txtSoDanhGia: TextView
    private lateinit var btnEdit: ImageButton
    private lateinit var layoutEdit: LinearLayout
    private lateinit var edtName: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtBio: EditText
    private lateinit var btnLuu: Button
    private lateinit var btnChonAnh: Button

    private var imageUri: Uri? = null
    private var isEditMode = false
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private val realtimeDb = FirebaseDatabase.getInstance().reference

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            imgAvatar.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ánh xạ view
        imgAvatar = view.findViewById(R.id.imgAvatar)
        txtName = view.findViewById(R.id.txtName)
        txtEmail = view.findViewById(R.id.txtEmail)
        txtPhone = view.findViewById(R.id.txtPhone)
        txtBio = view.findViewById(R.id.txtBio)
        txtSoBaiDang = view.findViewById(R.id.txtSoBaiDang)
        txtSoDanhGia = view.findViewById(R.id.txtSoDanhGia)
        btnEdit = view.findViewById(R.id.btnEdit)
        layoutEdit = view.findViewById(R.id.layoutEdit)
        edtName = view.findViewById(R.id.edtName)
        edtPhone = view.findViewById(R.id.edtPhone)
        edtBio = view.findViewById(R.id.edtBio)
        btnLuu = view.findViewById(R.id.btnLuu)
        btnChonAnh = view.findViewById(R.id.btnChonAnh)

        // Hiển thị email từ Firebase Auth
        txtEmail.text = currentUser?.email ?: ""

        btnEdit.setOnClickListener {
            toggleEditMode()
        }

        btnChonAnh.setOnClickListener {
            launcher.launch("image/*")
        }

        btnLuu.setOnClickListener {
            luuThongTinNguoiDung()
        }

        loadThongTinNguoiDung()
        demSoBaiDangVaDanhGia()
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode

        if (isEditMode) {
            // Chuyển sang chế độ chỉnh sửa
            layoutEdit.visibility = View.VISIBLE
            btnEdit.setImageResource(R.drawable.baseline_close_24)

            // Điền dữ liệu hiện tại vào form
            edtName.setText(txtName.text)
            edtPhone.setText(txtPhone.text)
            edtBio.setText(txtBio.text)
        } else {
            // Thoát chế độ chỉnh sửa
            layoutEdit.visibility = View.GONE
            btnEdit.setImageResource(R.drawable.baseline_edit_24)
        }
    }

    private fun loadThongTinNguoiDung() {
        currentUser?.uid?.let { uid ->
            db.collection("nguoidung").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val user = doc.toObject(UserModel::class.java)
                        txtName.text = user?.name ?: "Chưa cập nhật"
                        txtPhone.text = user?.phone ?: "Chưa cập nhật"
                        txtBio.text = user?.bio ?: "Chưa cập nhật"

                        // Load avatar
                        user?.avatarUrl?.let { url ->
                            if (url.isNotBlank()) {
                                Glide.with(requireContext())
                                    .load(url)
                                    .circleCrop()
                                    .placeholder(R.drawable.baseline_account_circle_24)
                                    .error(R.drawable.baseline_account_circle_24)
                                    .into(imgAvatar)
                            }
                        }
                    } else {
                        // Nếu chưa có dữ liệu, tạo mới với idnd
                        val newUser = UserModel(
                            idnd = uid,
                            email = currentUser?.email ?: "",
                            name = currentUser?.displayName ?: "Chưa cập nhật"
                        )
                        db.collection("nguoidung").document(uid).set(newUser)
                    }
                }
        }
    }
    private fun luuThongTinNguoiDung() {
        val name = edtName.text.toString().trim()
        val phone = edtPhone.text.toString().trim()
        val bio = edtBio.text.toString().trim()

        if (name.isEmpty()) {
            edtName.error = "Vui lòng nhập tên"
            return
        }

        btnLuu.isEnabled = false
        btnLuu.text = "Đang lưu..."

        val uid = currentUser?.uid ?: run {
            Toast.makeText(context, "Lỗi: Người dùng không xác định", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            // Upload ảnh lên Cloudinary
            FirebaseService.uploadImage(requireContext(), imageUri!!) { imageUrl ->
                activity?.runOnUiThread {
                    if (imageUrl != null) {
                        // Lưu thông tin vào Firestore
                        saveToFirestore(uid, name, phone, bio, imageUrl)
                        // Lưu URL ảnh vào Realtime Database
                        saveToRealtimeDB(uid, imageUrl)
                    } else {
                        btnLuu.isEnabled = true
                        btnLuu.text = "Lưu thông tin"
                        Toast.makeText(context, "Lỗi khi tải ảnh lên", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            // Không có ảnh mới, chỉ cập nhật thông tin
            saveToFirestore(uid, name, phone, bio, null)
        }
    }

    private fun saveToFirestore(uid: String, name: String, phone: String, bio: String, imageUrl: String?) {
        val userRef = db.collection("nguoidung").document(uid)

        val updates = hashMapOf<String, Any>(
            "name" to name,
            "phone" to phone,
            "bio" to bio
        )

        imageUrl?.let { updates["avatarUrl"] = it }

        userRef.update(updates)
            .addOnSuccessListener {
                btnLuu.isEnabled = true
                btnLuu.text = "Lưu thông tin"
                toggleEditMode()
                loadThongTinNguoiDung()
                Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                btnLuu.isEnabled = true
                btnLuu.text = "Lưu thông tin"
                Toast.makeText(context, "Lỗi khi lưu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToRealtimeDB(uid: String, imageUrl: String) {
        val updates = hashMapOf<String, Any>(
            "avatarUrl" to imageUrl,
            "lastUpdated" to ServerValue.TIMESTAMP
        )

        realtimeDb.child("users").child(uid).updateChildren(updates)
            .addOnFailureListener { e ->
                Log.e("Profile", "Lỗi lưu vào Realtime DB", e)
            }
    }
//    private fun luuThongTinNguoiDung() {
//        val uid = currentUser?.uid ?: return
//        val name = edtName.text.toString().trim()
//        val phone = edtPhone.text.toString().trim()
//        val bio = edtBio.text.toString().trim()
//
//        if (name.isEmpty()) {
//            edtName.error = "Vui lòng nhập tên"
//            return
//        }
//
//        val userRef = db.collection("nguoidung").document(uid)
//
//        if (imageUri != null) {
//            // Upload ảnh mới nếu có
//            uploadToCloudinary(imageUri!!) { url ->
//                if (url != null) {
//                    updateUserInfo(userRef, name, phone, bio, url)
//                } else {
//                    Toast.makeText(context, "Lỗi tải lên ảnh", Toast.LENGTH_SHORT).show()
//                }
//            }
//        } else {
//            // Giữ nguyên ảnh cũ nếu không có ảnh mới
//            userRef.get().addOnSuccessListener { doc ->
//                val currentAvatar = doc.getString("avatarUrl") ?: ""
//                updateUserInfo(userRef, name, phone, bio, currentAvatar)
//            }
//        }
//    }

    private fun updateUserInfo(
        userRef: DocumentReference,
        name: String,
        phone: String,
        bio: String,
        avatarUrl: String
    ) {
        val uid = currentUser?.uid ?: return

        val updatedUser = UserModel(
            idnd = uid,
            name = name,
            email = currentUser?.email ?: "",
            phone = phone,
            bio = bio,
            avatarUrl = avatarUrl
        )

        userRef.set(updatedUser)
            .addOnSuccessListener {
                // Cập nhật UI
                txtName.text = name
                txtPhone.text = if (phone.isEmpty()) "Chưa cập nhật" else phone
                txtBio.text = if (bio.isEmpty()) "Chưa cập nhật" else bio

                if (avatarUrl.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(avatarUrl)
                        .circleCrop()
                        .into(imgAvatar)
                }

                // Tắt chế độ chỉnh sửa
                toggleEditMode()
                Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Lỗi cập nhật: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadToCloudinary(uri: Uri, callback: (String?) -> Unit) {
        val uid = currentUser?.uid ?: run {
            callback(null)
            return
        }
        Log.d("Profile", "Bắt đầu upload ảnh: $uri")
        val storageRef = FirebaseStorage.getInstance().reference
        val avatarRef = storageRef.child("avatars/$uid.jpg")

        avatarRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { downloadUri ->
                    callback(downloadUri.toString())
                }?.addOnFailureListener { e ->
                    Log.e("Profile", "Error getting download URL", e)
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Profile", "Upload failed", e)
                callback(null)
            }
    }

    private fun demSoBaiDangVaDanhGia() {
        currentUser?.uid?.let { uid ->
            // ===== REALTIME DATABASE =====
            // Đếm BÀI ĐĂNG (Realtime DB - idnd là field)
            FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("2/data")
                .orderByChild("idnd")
                .equalTo(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        txtSoBaiDang.text = "${snapshot.childrenCount}"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Profile", "Lỗi đếm bài đăng: ${error.message}")
                        txtSoBaiDang.text = "0"
                    }
                })

            // Đếm ĐÁNH GIÁ (Realtime DB - idnd là field)
            FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("5/data")
                .orderByChild("idnd")
                .equalTo(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        txtSoDanhGia.text = "${snapshot.childrenCount}"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Profile", "Lỗi đếm đánh giá: ${error.message}")
                        txtSoDanhGia.text = "0"
                    }
                })

        }
    }
}


