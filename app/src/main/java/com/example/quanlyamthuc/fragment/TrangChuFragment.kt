package com.example.quanlyamthuc.fragment

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.adapter.MonAnAdapter
import com.example.quanlyamthuc.databinding.FragmentTrangChuBinding
import com.example.quanlyamthuc.model.MonAn
import com.example.quanlyamthuc.utils.StringUtils.removeVietnameseAccents
import com.google.firebase.database.*

class TrangChuFragment : Fragment() {

    private val mapTenTinh = mutableMapOf<String, String>()
    val idTinhList = arrayListOf<String>() // Thêm dòng này ở đầu onViewCreated hoặc là biến class

    private var _binding: FragmentTrangChuBinding? = null
    private val binding get() = _binding!!

    private lateinit var backgroundImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrangChuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val danhSachAnhTongHop = arrayListOf(
            SlideModel(R.drawable.bn222, ScaleTypes.FIT),
            SlideModel(R.drawable.bn6, ScaleTypes.FIT) ,
            SlideModel(R.drawable.bn11, ScaleTypes.FIT),
            SlideModel(R.drawable.bn7, ScaleTypes.FIT)
        )
        binding.imageSlider.setImageList(danhSachAnhTongHop, ScaleTypes.FIT)

        // Slider tỉnh thành - lấy từ Firebase
        val imageSliderTinh = binding.imageSliderTinh
        val database =
            FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
        val dbRef = database.getReference("14/data")  // Lấy tất cả dữ liệu từ node "14/data"

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val slideList = ArrayList<SlideModel>()

                // Duyệt qua tất cả các mục trong node "14/data"
                for (dataSnap in snapshot.children) {
                    val imageUrl = dataSnap.child("hinhanh").getValue(String::class.java)
                    val description = dataSnap.child("tentinh").getValue(String::class.java)
                    val idtt = dataSnap.child("idtt").getValue(String::class.java)

                    if (!imageUrl.isNullOrEmpty() && !idtt.isNullOrEmpty()) {
                        slideList.add(SlideModel(imageUrl, description ?: "", ScaleTypes.FIT))
                        idTinhList.add(idtt)  // Lưu idtt tương ứng
                    }
                }

                // Set tất cả ảnh vào slider
                imageSliderTinh.setImageList(slideList, ScaleTypes.FIT)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi tải ảnh từ Firebase", Toast.LENGTH_SHORT)
                    .show()
            }

        })

// Bắt sự kiện click slider tỉnh thành
        imageSliderTinh.setItemClickListener(object : ItemClickListener {
            override fun doubleClick(position: Int) {}

            override fun onItemSelected(position: Int) {
                val idTinhDuocChon = idTinhList[position]
                val tenTinh = mapTenTinh[idTinhDuocChon] ?: "Không rõ"

                val fragment = DanhSachMonAnTheoTinhFragment.newInstance(idTinhDuocChon, tenTinh)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()

            }
        })

        taiDanhSachTinh {
            setupSliderTinh()
            MonAnNoiTieng()
        }

        binding.editTextSearch.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearch()
                true
            } else {
                false
            }
        }
    }
    private fun performSearch() {
        val searchText = binding.editTextSearch.text.toString().trim()
        if (searchText.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show()
            return
        }

        val normalizedSearchText = searchText.removeVietnameseAccents().lowercase()

        // Kiểm tra xem từ khóa có trùng với tên tỉnh không
        val matchedEntry = mapTenTinh.entries.firstOrNull {
            it.value.removeVietnameseAccents().lowercase().contains(normalizedSearchText)
        }

        if (matchedEntry != null) {
            // Tìm idtt tương ứng với tên tỉnh
            val idTinh = matchedEntry.key
            val tenTinh = matchedEntry.value
            // Chuyển đến Fragment hiển thị danh sách món ăn theo tỉnh
            val fragment = DanhSachMonAnTheoTinhFragment.newInstance(idTinh, tenTinh)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        } else {
            // Nếu không phải tên tỉnh, tìm kiếm theo tên món ăn
            val fragment = DanhSachMonAnTimKiemFragment.newInstance(searchText)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.editTextSearch.setText("")

    }

    private fun setupSliderTinh() {
        binding.imageSliderTinh.setItemClickListener(object : ItemClickListener {
            override fun doubleClick(position: Int) {}

            override fun onItemSelected(position: Int) {
                val idTinhDuocChon = idTinhList[position]
                val tenTinh = mapTenTinh[idTinhDuocChon] ?: "Không rõ"

                val fragment = DanhSachMonAnTheoTinhFragment.newInstance(idTinhDuocChon, tenTinh)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        })
    }

//
//    private fun hienThiMonAnTheoTinh(idtt: String) {
//        val db = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
//        val dbRef = db.getReference("10/data")
//
//        val danhSach = mutableListOf<MonAn>()
//
//        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for (monAnSnap in snapshot.children) {
//                    val monAn = monAnSnap.getValue(MonAn::class.java)
//                    if (monAn != null && monAn.idtt == idtt) {
//                        monAn.tenTinh = mapTenTinh[monAn.idtt]
//                        danhSach.add(monAn)
//                    }
//                }
//
//                if (danhSach.isEmpty()) {
//                    Toast.makeText(requireContext(), "Không có món ăn nào!", Toast.LENGTH_SHORT).show()
//                    return
//                }
//
//                val adapter = MonAnAdapter(
//                    danhSach,
//                    onItemClick = { monAn ->
//                        val fragment = ChiTietMonAnFragment.newInstance(monAn)
//                        parentFragmentManager.beginTransaction()
//                            .replace(R.id.fragment_container, fragment)
//                            .addToBackStack(null)
//                            .commit()
//                    },
//                    layoutId = R.layout.item_mon_an_noi_tieng,
//                    mapTenTinh = mapTenTinh
//                )
//
//                binding.recyclerMonAn.layoutManager = GridLayoutManager(requireContext(), 2) // hoặc Linear
//                binding.recyclerMonAn.adapter = adapter
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(requireContext(), "Lỗi tải món ăn", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//

    private fun taiDanhSachTinh(onFinish: () -> Unit) {
        val dbTinh = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("14/data")

        dbTinh.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val tinh = child.getValue(com.example.quanlyamthuc.model.TinhThanh::class.java)
                    if (tinh != null && tinh.idtt != null && tinh.tentinh != null) {
                        mapTenTinh[tinh.idtt!!] = tinh.tentinh!!
                    }
                }
                onFinish()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Không tải được danh sách tỉnh", Toast.LENGTH_SHORT).show()
                onFinish()
            }
        })
    }

    private fun MonAnNoiTieng() {
        val database = FirebaseDatabase
            .getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")

        val danhSach = mutableListOf<MonAn>()
        val nodesToFetch = listOf("10/data/0", "10/data/23", "10/data/10", "10/data/16", "10/data/15", "10/data/70", "10/data/50", "10/data/4", "10/data/17")

        for (node in nodesToFetch) {
            val dbRef = database.getReference(node)

            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val monAn = snapshot.getValue(MonAn::class.java)
                    if (monAn != null) {
                        monAn.tenTinh = mapTenTinh[monAn.idtt]
                        danhSach.add(monAn)
                        Log.d("CheckMonAn", "IDTT: ${monAn.idtt}, Tên tỉnh: ${mapTenTinh[monAn.idtt]}")

                        if (danhSach.size == nodesToFetch.size) {
                            val adapter = MonAnAdapter(
                                danhSach,
                                onItemClick = { monAn ->
                                    val fragment = ChiTietMonAnFragment.newInstance(monAn)
                                    parentFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_container, fragment)
                                        .addToBackStack(null)
                                        .commit()
                                },
                                layoutId = R.layout.item_mon_an_noi_tieng,
                                mapTenTinh = mapTenTinh
                            )

                            binding.recyclerMonAn.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                            binding.recyclerMonAn.adapter = adapter
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Lỗi Firebase", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
    }




