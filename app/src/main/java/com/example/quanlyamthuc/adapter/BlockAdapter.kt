package com.example.quanlyamthuc.adapter

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.quanlyamthuc.databinding.ItemBlockBinding
import com.example.quanlyamthuc.model.BlockModel
import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlyamthuc.R
import com.google.firebase.firestore.FirebaseFirestore

class BlockAdapter (
    private val context: Context,
    private var blockList: List<BlockModel>,
    private val onDeleteClick: (BlockModel) -> Unit,

) : RecyclerView.Adapter<BlockAdapter.BlockViewHolder>() {

   // private var filteredList: List<BlockModel> = blockList

    // Thêm hàm update danh sách
    fun updateList(newList: List<BlockModel>) {
        blockList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        val binding = ItemBlockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BlockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {

        val block = blockList[position]
        Log.d("ADAPTER_ITEM", "Hiển thị bài: ${block.tenmonan}")
        holder.bind(block)

        holder.btnDelete.setOnClickListener {
            onDeleteClick(block)
        }
    }

    override fun getItemCount(): Int = blockList.size

    inner class BlockViewHolder(val binding: ItemBlockBinding)
        : RecyclerView.ViewHolder(binding.root) {

        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(block: BlockModel) {
            binding.txtTenMa.text = block.tenmonan
            binding.txtNoiDung.text = block.noidung
            binding.txtNgayDang.text = "${block.created_at}"
            binding.txtTinh.text = block.tinhthanh
            binding.txtLike.text = block.so_like

            getUserName(block.idnd ?: "") { name ->
                binding.txtNguoiDang.text = name // bạn cần thêm TextView này trong layout
                Log.d("DEBUG_IDND", "block.idnd = ${block.idnd}")

            }

            if (!block.hinhanh_ma.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(block.hinhanh_ma)
                    .into(binding.imageBlock)
            } else {
                binding.imageBlock.setImageResource(R.drawable.banner1)
            }

           }
        }
    fun getUserName(userId: String, callback: (String) -> Unit) {
        if (userId.isBlank()) {
            Log.e("GET_USER", "ID người dùng rỗng!")
            callback("Ẩn danh")
            return
        }
        val db = FirebaseFirestore.getInstance()
        db.collection("nguoidung") .whereEqualTo("idnd", userId)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.first()
                    val name = doc.getString("name") ?: "Ẩn danh"
                    Log.d("GET_USER", "Tên người dùng: $name")
                    callback(name)
                } else {
                    Log.e("GET_USER", "Không tìm thấy người dùng với ind = $userId")
                    callback("Ẩn danh")
                }
            }
            .addOnFailureListener {
                Log.e("GET_USER", "Lỗi Firestore: ${it.message}")
                callback("Lỗi")
            }
    }
}
