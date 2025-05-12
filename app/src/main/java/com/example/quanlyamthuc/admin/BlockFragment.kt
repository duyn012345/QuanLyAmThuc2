package com.example.quanlyamthuc.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.quanlyamthuc.model.BlockModel
import com.example.quanlyamthuc.adapter.BlockAdapter
import com.example.quanlyamthuc.databinding.FragmentBlockBinding
import com.google.firebase.database.*
import java.text.Normalizer

class BlockFragment : Fragment() {

    private var _binding: FragmentBlockBinding? = null
    private val binding get() = _binding!!

    private val databaseRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("2/data")
    }


    private val fullList = mutableListOf<BlockModel>()
    private lateinit var blockAdapter: BlockAdapter
    private var searchQuery = ""

    override fun onStart() {
        super.onStart()

        // Lấy dữ liệu từ Firebase khi ứng dụng được bật lại
        val database = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val myRef = database.getReference("2/data")

        myRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val data = task.result
                // Xử lý dữ liệu và hiển thị lên giao diện
            } else {
                // Xử lý lỗi nếu không lấy được dữ liệu
                Log.e("Firebase", "Error getting data", task.exception)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        loadData()
    }

    private fun setupRecyclerView() {
        blockAdapter = BlockAdapter(
            requireContext(),
            emptyList(),
            onDeleteClick = { confirmDelete(it) }
        )

        binding.recyclerBaiDang.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = blockAdapter
            setHasFixedSize(true)
        }
    }
    private fun removeAccent(s: String?): String {
        if (s.isNullOrEmpty()) return ""
        return Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
    }

    private fun setupSearch() {
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            private var lastText = ""
            private val handler = android.os.Handler()
            private val runnable = Runnable {
                val currentText = binding.edtSearch.text.toString()
                if (currentText != lastText) {
                    lastText = currentText
                    searchQuery = lastText
                    filterData()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 300) // Delay 300ms để giảm số lần filter
            }
        })
    }

    private fun loadData() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fullList.clear()
                for (blockSnap in snapshot.children) {
                    blockSnap.getValue(BlockModel::class.java)?.let { block ->
                        block.idbd = blockSnap.key
                        fullList.add(block)
                    }
                }
                filterData()
                Log.d("BlockFragment", "Loaded ${fullList.size} items")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Lỗi tải dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("BlockFragment", "DatabaseError: ${error.message}")
            }
        })
    }

    private fun filterData() {
        val searchText = removeAccent(searchQuery.trim())
        val filteredList = if (searchText.isEmpty()) {
            fullList
        } else {
            fullList.filter { block ->
                removeAccent(block.tenmonan).contains(searchText) ||
                        removeAccent(block.noidung).contains(searchText) ||
                        removeAccent(block.tinhthanh).contains(searchText)
            }
        }

        blockAdapter.updateList(filteredList)
        updateItemCount(filteredList.size)
    }

    private fun updateItemCount(count: Int) {
        binding.txtTongBaiDang.text = "Tổng bài đăng: $count"
    }

    private fun confirmDelete(block: BlockModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn chắc chắn muốn xóa bài đăng '${block.tenmonan}'?")
            .setPositiveButton("Xóa") { _, _ ->
                block.idbd?.let { id ->
                    databaseRef.child(id).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Lỗi khi xóa: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}