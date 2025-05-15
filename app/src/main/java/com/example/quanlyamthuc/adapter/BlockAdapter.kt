package com.example.quanlyamthuc.adapter

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.quanlyamthuc.databinding.ItemBlockBinding
import com.example.quanlyamthuc.model.BlockModel
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlyamthuc.R
import com.google.firebase.firestore.FirebaseFirestore

class BlockAdapter(
    private val context: Context,
    private var blockList: List<BlockModel>,
    private val onDeleteClick: (BlockModel) -> Unit
) : RecyclerView.Adapter<BlockAdapter.BlockViewHolder>() {

    private var userInfoMap = mapOf<String, Pair<String, String?>>() // Map chứa cả tên và avatarUrl

    fun setUserInfoMap(map: Map<String, Pair<String, String?>>) {
        userInfoMap = map
        notifyDataSetChanged()
    }
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

            // Load thông tin người dùng và avatar
            val userId = block.idnd ?: ""
            if (userInfoMap.containsKey(userId)) {
                val (name, avatarUrl) = userInfoMap[userId] ?: Pair("Ẩn danh", null)
                binding.txtNguoiDang.text = name
                loadAvatar(binding.imgUserIcon, avatarUrl)
            } else {
                getUserInfo(userId) { name, avatarUrl ->
                    binding.txtNguoiDang.text = name
                    loadAvatar(binding.imgUserIcon, avatarUrl)
                }
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
    private fun getUserInfo(userId: String, callback: (String, String?) -> Unit) {
        if (userId.isBlank()) {
            callback("Ẩn danh", null)
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
                    val avatarUrl = doc.getString("avatarUrl")
                    callback(name, avatarUrl)
                } else {
                    callback("Ẩn danh", null)
//                    val db = FirebaseFirestore.getInstance()
//                    db.collection("nguoidung") .whereEqualTo("idnd", userId)
//                        .limit(1)
//                        .get()
//                        .addOnSuccessListener { document ->
//                            if (document.exists()) {
//                                val name = document.getString("name") ?: "Ẩn danh"
//                                val avatarUrl = document.getString("avatarUrl")
//                                callback(name, avatarUrl)
//                            } else {
//                                callback("Ẩn danh", null)
                }
            }
            .addOnFailureListener {
                Log.e("GET_USER", "Lỗi Firestore: ${it.message}")
                callback("Lỗi", null)
            }
    }

    private fun loadAvatar(imageView: ImageView, avatarUrl: String?) {
        avatarUrl?.let { url ->
            Glide.with(context)
                .load(url)
                .placeholder(R.drawable.baseline_account_circle_24)
                .circleCrop()
                .into(imageView)
        } ?: imageView.setImageResource(R.drawable.baseline_account_circle_24)
    }
}