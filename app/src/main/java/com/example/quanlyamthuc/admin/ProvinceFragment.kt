package com.example.quanlyamthuc.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.adapter.ProvinceAdapter
import com.example.quanlyamthuc.databinding.FragmentProvinceBinding
import com.example.quanlyamthuc.fragment.DishFragment
import com.example.quanlyamthuc.model.ProvinceModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProvinceFragment : Fragment() {

    private var _binding: FragmentProvinceBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: FirebaseDatabase
    private lateinit var provinceAdapter: ProvinceAdapter
    private val provinceList = mutableListOf<ProvinceModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProvinceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)

        // Khởi tạo RecyclerView và Adapter
        provinceAdapter = ProvinceAdapter(provinceList, provinceList) { province ->
            // Khi nhấn vào một tỉnh, truyền idtt sang DishFragment
            val bundle = Bundle().apply {
                putString("provinceIdtt", province.idtt) // Truyền idtt
            }
            val dishFragment = DishFragment().apply {
                arguments = bundle
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_content, dishFragment) // Đảm bảo R.id.fragment_container tồn tại
                .addToBackStack(null)
                .commit()
        }
        binding.provinceRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = provinceAdapter
        }

        // Thêm nút xem tất cả vào toolbar
        binding.toolbar.inflateMenu(R.menu.menu_all_dish)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_view_all -> {
                    // Mở DishFragment không truyền provinceIdtt
                    val dishFragment = DishFragment()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_content, dishFragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }
                else -> false
            }}

//        binding.provinceRecyclerView.layoutManager = GridLayoutManager(context, 2)
//        binding.provinceRecyclerView.adapter = provinceAdapter

        binding.fabAddProvince.setOnClickListener {
            // TODO: Mở dialog hoặc chuyển sang màn hình thêm mới
            showAddDialog() //
        }
        //binding.provinceRecyclerView.layoutManager = GridLayoutManager(context, 2)

        // Tải danh sách tỉnh thành
        loadProvinces()
    }
    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_or_edit, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Thêm Tỉnh")

        val nameEditText: EditText = dialogView.findViewById(R.id.nameEditText)
        val imageUrlEditText: EditText = dialogView.findViewById(R.id.imageUrlEditText)

        builder.setPositiveButton("Lưu") { dialog, _ ->
            val tentinh = nameEditText.text.toString().trim()
            val hinhanh = imageUrlEditText.text.toString().trim()
            // Nếu imageUrl rỗng, gán giá trị mặc định
            if (hinhanh.isEmpty()) {
                Toast.makeText(requireContext(), "Ảnh không thể trống", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            // Kiểm tra nếu name rỗng, yêu cầu người dùng nhập
            if (tentinh.isEmpty()) {
                Toast.makeText(requireContext(), "Tên tỉnh không thể trống", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            val newProvince = ProvinceModel(tentinh, hinhanh)
            database = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app/")
            val provinceRef = database.getReference("14/data")
            val newProvinceRef = provinceRef.push()
            newProvinceRef.setValue(newProvince)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Thêm tỉnh thành công", Toast.LENGTH_SHORT).show()
                    loadProvinces()  // Tải lại danh sách tỉnh
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Thêm tỉnh thất bại: ${it.message}", Toast.LENGTH_LONG).show()
                }
            dialog.dismiss()
        }

        builder.setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }

        builder.create().show()

    }


    private fun loadProvinces() {
        database = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val provincesRef = database.getReference("14/data")
        provincesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                provinceList.clear()
                for (provinceSnapshot in snapshot.children) {
                    try {
                        val province = provinceSnapshot.getValue(ProvinceModel::class.java)
                        province?.let {
                            // Gán key của node (ví dụ: "18") làm id nếu chưa có
                            if (it.id == null) it.id = provinceSnapshot.key
                            provinceList.add(it)
                            Log.d("ProvinceFragment", "Added province: ${it.name}, imageUrl: ${it.imageUrl}, id: ${it.id}")
                        } ?: run {
                            Log.e("ProvinceFragment", "Failed to parse province: ${provinceSnapshot.key}")
                        }
                    } catch (e: Exception) {
                        Log.e("ProvinceFragment", "Error parsing province ${provinceSnapshot.key}: ${e.message}")
                    }
                }
                Log.d("ProvinceFragment", "Total provinces fetched: ${provinceList.size}")
                provinceAdapter.updateList(provinceList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProvinceFragment", "Error loading provinces: ${error.message}", error.toException())
                Toast.makeText(context, "Lỗi khi tải danh sách tỉnh thành: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}