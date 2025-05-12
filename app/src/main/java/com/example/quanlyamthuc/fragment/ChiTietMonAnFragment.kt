package com.example.quanlyamthuc.fragment

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.quanlyamthuc.LoginActivity
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.adapter.DanhGiaAdapter
import com.example.quanlyamthuc.databinding.FragmentChiTietMonAnBinding
import com.example.quanlyamthuc.model.DanhGia
import com.example.quanlyamthuc.model.MonAn
import com.example.quanlyamthuc.model.TinhThanh
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChiTietMonAnFragment : Fragment() {

    private var _binding: FragmentChiTietMonAnBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(monAn: MonAn): ChiTietMonAnFragment {
            val fragment = ChiTietMonAnFragment()
            val bundle = Bundle()
            bundle.putSerializable("monAn", monAn)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChiTietMonAnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val monAn = arguments?.getSerializable("monAn") as? MonAn ?: return

        // Set up danh sách ảnh
        val danhSachAnh = arrayListOf<SlideModel>()
        monAn.hinhanh?.let { danhSachAnh.add(SlideModel(it, ScaleTypes.FIT)) }
        monAn.hinhanh2?.let { danhSachAnh.add(SlideModel(it, ScaleTypes.FIT)) }
        monAn.hinhanh3?.let { danhSachAnh.add(SlideModel(it, ScaleTypes.FIT)) }

        binding.imageSliderChiTiet.setImageList(danhSachAnh, ScaleTypes.FIT)
        binding.imageSliderChiTiet.startSliding(1500)

        binding.tvTenMonChiTiet.text = monAn.tenma

        // Fetch and display the province (TinhThanh)
        val tinhRef = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("14/data")

        tinhRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var tenTinh = "Không rõ tỉnh"
                for (data in snapshot.children) {
                    val tinh = data.getValue(TinhThanh::class.java)
                    if (tinh != null && tinh.idtt == monAn.idtt) {
                        tenTinh = tinh.tentinh ?: "Không rõ tỉnh"
                        break
                    }
                }
                binding.tvTinhChiTiet.text = tenTinh
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Lỗi lấy tỉnh", error.toException())
                binding.tvTinhChiTiet.text = "Không rõ tỉnh"
            }
        })

        binding.tvGioiThieuChiTiet.text = monAn.gioithieu
        binding.tvGiaCaChiTiet.text = "  Giao động từ: ${monAn.giaca}"
        binding.tvDiaChiChiTiet.text = "  Địa chỉ quán gợi ý: ${monAn.diachi}"
        binding.tvMoTaChiTiet.text = "\n${monAn.mota}"

        binding.tvXemDiaChiChiTiet.text = "  Xem địa chỉ"
        binding.tvXemDiaChiChiTiet.setOnClickListener {
            val link = monAn.duonglink_diachi
            if (!link.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                startActivity(intent)
            }
        }

        // Set up RecyclerView for ratings
        binding.rvDanhGia.layoutManager = LinearLayoutManager(requireContext())

        val danhGiaList = mutableListOf<DanhGia>()
        val adapter = DanhGiaAdapter(danhGiaList)
        binding.rvDanhGia.adapter = adapter

        val database = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("5/data")

        // Load danh gia and fetch user name from Firestore
        database.orderByChild("idma").equalTo(monAn.idma).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<DanhGia>()
                val total = snapshot.childrenCount.toInt()
                var processed = 0

                for (data in snapshot.children) {
                    val dg = data.getValue(DanhGia::class.java)
                    dg?.let {
                        fetchUserName(it.idnd ?: "") { userName ->
                            it.idnd = userName // Ghi đè idnd thành name người dùng
                            tempList.add(it)
                            processed++

                            if (processed == total) {
                                danhGiaList.clear()
                                danhGiaList.addAll(tempList)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }


            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to load data.", error.toException())
            }
        })
        binding.btnThemDanhGia.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                // Nếu chưa đăng nhập, chuyển sang màn hình đăng nhập
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            } else {
                showDanhGiaDialog(monAn.idma ?: "", currentUser.uid)
            }
        }


    }

    private fun fetchUserName(userId: String, callback: (String) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("nguoidung").document(userId)

        // Thử lấy theo UID (tức là document ID trước)
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Tên người dùng không có"
                    callback(name)
                } else {
                    // Nếu không có document với UID, dùng cách cũ: where idnd = userId
                    firestore.collection("nguoidung")
                        .whereEqualTo("idnd", userId)
                        .get()
                        .addOnSuccessListener { result ->
                            if (!result.isEmpty) {
                                val name = result.documents[0].getString("name") ?: "Tên người dùng không có"
                                callback(name)
                            } else {
                                callback("Không tìm thấy người dùng")
                            }
                        }
                        .addOnFailureListener {
                            Log.e("Firebase", "Lỗi khi tìm user bằng idnd", it)
                            callback("Lỗi tìm người dùng")
                        }
                }
            }
            .addOnFailureListener {
                Log.e("Firebase", "Lỗi khi tìm user theo UID", it)
                callback("Lỗi tìm người dùng")
            }
    }



    private fun showDanhGiaDialog(idma: String, uid: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_danhgia, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Thêm đánh giá")
            .setView(dialogView)
            .setPositiveButton("Thêm", null)
            .setNegativeButton("Hủy", null)
            .create()

        dialog.setOnShowListener {
            val btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val btnNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            // Đổi màu cho nút "Thêm"
            btnPositive.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPositive))

            // Đổi màu cho nút "Hủy"
            btnNegative.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorNegative))

            btnPositive.setOnClickListener {
                val soSao = dialogView.findViewById<RatingBar>(R.id.ratingBar).rating.toInt()
                val noiDung = dialogView.findViewById<EditText>(R.id.edtNoiDung).text.toString()
                val hinhanh = dialogView.findViewById<EditText>(R.id.edtLinkAnh).text.toString()

                if (noiDung.isBlank()) {
                    Toast.makeText(requireContext(), "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val danhGiaRef = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("5/data")

                val iddg = danhGiaRef.push().key ?: return@setOnClickListener
                val createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                val danhGia = mapOf(
                    "iddg" to iddg,
                    "idma" to idma,
                    "idnd" to uid,
                    "so_sao" to soSao.toString(),
                    "noi_dung" to noiDung,
                    "hinhanh_danhgia" to hinhanh,
                    "created_at" to createdAt,
                    "updated_at" to createdAt
                )

                danhGiaRef.child(iddg).setValue(danhGia).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Đã thêm đánh giá", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Lỗi khi thêm đánh giá", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
