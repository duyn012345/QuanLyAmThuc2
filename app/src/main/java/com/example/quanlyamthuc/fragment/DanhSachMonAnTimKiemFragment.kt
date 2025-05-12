package com.example.quanlyamthuc.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.adapter.MonAnAdapter
import com.example.quanlyamthuc.databinding.FragmentDanhSachMonAnTimKiemBinding
import com.example.quanlyamthuc.model.MonAn
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.Normalizer

class DanhSachMonAnTimKiemFragment : Fragment() {

    private var searchText: String? = null
    private val mapTenTinh = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchText = it.getString(ARG_SEARCH_TEXT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentDanhSachMonAnTimKiemBinding.inflate(inflater, container, false)

        binding.textViewKetQua.text = "Kết quả tìm kiếm cho từ khóa: ${searchText ?: ""}"
        // Tải danh sách tỉnh để sử dụng mapTenTinh
        taiDanhSachTinh {
            timKiemMonAnTheoTen(searchText ?: "", binding)
        }

        return binding.root
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

    private fun timKiemMonAnTheoTen(searchText: String, binding: FragmentDanhSachMonAnTimKiemBinding) {
        val database = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
        val dbRef = database.getReference("10/data")

        val danhSach = mutableListOf<MonAn>()

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                danhSach.clear()
                val normalizedSearchText = searchText.removeVietnameseAccents().lowercase()
                for (monAnSnap in snapshot.children) {
                    val monAn = monAnSnap.getValue(MonAn::class.java)
                    monAn?.let {
                        if (it.tenma?.removeVietnameseAccents()?.lowercase()
                                ?.contains(normalizedSearchText) == true) {
                            it.tenTinh = mapTenTinh[it.idtt]
                            danhSach.add(it)
                        }
                    }
                }

                if (danhSach.isEmpty()) {
                    Toast.makeText(requireContext(), "Không tìm thấy món ăn phù hợp", Toast.LENGTH_SHORT).show()
                    return
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
                    layoutId = R.layout.item_ket_qua_tim_kiem_mon_an,
                    mapTenTinh = mapTenTinh
                )

                binding.recyclerMonAn.layoutManager = GridLayoutManager(requireContext(), 2)
                binding.recyclerMonAn.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi tải món ăn", Toast.LENGTH_SHORT).show()
            }
        })
    }
    fun String.removeVietnameseAccents(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        return Regex("\\p{InCombiningDiacriticalMarks}+").replace(temp, "")
    }


    companion object {
        private const val ARG_SEARCH_TEXT = "search_text"

        @JvmStatic
        fun newInstance(searchText: String) =
            DanhSachMonAnTimKiemFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SEARCH_TEXT, searchText)
                }
            }
    }
}
