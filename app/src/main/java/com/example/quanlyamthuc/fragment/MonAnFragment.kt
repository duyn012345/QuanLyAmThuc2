package com.example.quanlyamthuc.fragment

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.adapter.MonAnAdapter
import com.example.quanlyamthuc.databinding.FragmentMonAnBinding
import com.example.quanlyamthuc.model.MonAn
import com.example.quanlyamthuc.utils.StringUtils.removeVietnameseAccents
import com.google.firebase.database.*

class MonAnFragment : Fragment() {

    private val mapTenTinh = mutableMapOf<String, String>()

    private var _binding: FragmentMonAnBinding? = null
    private val binding get() = _binding!!
    private lateinit var monAnAdapter: MonAnAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonAnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Slider ảnh tổng hợp
        val danhSachAnhTongHop = arrayListOf(
            SlideModel(R.drawable.restorent, ScaleTypes.FIT),
            SlideModel(R.drawable.bn8, ScaleTypes.FIT),
            SlideModel(R.drawable.bn7, ScaleTypes.FIT)
        )
        binding.imageSlider.setImageList(danhSachAnhTongHop, ScaleTypes.FIT)
        binding.imageSlider.startSliding(1500)

        // Tải tên tỉnh trước khi hiển thị món ăn
        taiDanhSachTinh {
            TatCaMonAn()
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

    private fun MonAnNoiTieng() {
        val database = FirebaseDatabase
            .getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")

        val danhSach = mutableListOf<MonAn>()
        val nodesToFetch = listOf("10/data/0", "10/data/23", "10/data/10", "10/data/8", "10/data/15", "10/data/70", "10/data/50", "10/data/16", "10/data/17","10/data/59", "10/data/60", "10/data/65")

        for (node in nodesToFetch) {
            val dbRef = database.getReference(node)

            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val monAn = snapshot.getValue(MonAn::class.java)
                    if (monAn != null) {
                        monAn.tenTinh = mapTenTinh[monAn.idtt]
                        danhSach.add(monAn)
                        Log.d("CheckMonAn", "IDTT: ${monAn.idtt}, Tên tỉnh: ${mapTenTinh[monAn.idtt]}")

                        // Khi đã load xong tất cả node
                        if (danhSach.size == nodesToFetch.size) {
                            if (danhSach.isEmpty()) {
                                binding.txtThongBao.visibility = View.VISIBLE
                                binding.txtThongBao.text = "Không có món ăn nào!"
                            } else {
                                binding.txtThongBao.visibility = View.GONE
                            }

                            // Gán adapter
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

    private fun TatCaMonAn() {
        val database = FirebaseDatabase
            .getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")

        val danhSach = mutableListOf<MonAn>()
        val dbRef = database.getReference("10/data")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                danhSach.clear()
                for (monSnapshot in snapshot.children) {
                    val monAn = monSnapshot.getValue(MonAn::class.java)
                    if (monAn != null) {
                        monAn.tenTinh = mapTenTinh[monAn.idtt]
                        Log.d("CheckMonAn", "IDTT: ${monAn.idtt}, Tên tỉnh: ${mapTenTinh[monAn.idtt]}")

                        danhSach.add(monAn)
                    }
                }

                val adapter = MonAnAdapter(
                    danhSach,
                    onItemClick = { monAn ->
                        val fragment = ChiTietMonAnFragment.newInstance(monAn)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                    },
                    layoutId = R.layout.item_mon_an,
                    mapTenTinh = mapTenTinh
                )


                binding.recyclerTatCaMonAn.layoutManager = GridLayoutManager(requireContext(), 3)
                binding.recyclerTatCaMonAn.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi Firebase", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
