package com.example.quanlyamthuc.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.model.ReviewModel
import com.google.firebase.firestore.FirebaseFirestore

class ReviewAdapter(
    private val context: Context,
    private var reviewList: List<ReviewModel>,
    private val onDeleteClick: (ReviewModel) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private var tenNguoiDungMap = mapOf<String, Pair<String, String?>>() // Map chứa cả tên và avatarUrl
    private var tenMonAnMap = mapOf<String, String>()

    fun setTenNguoiDungMap(map: Map<String, Pair<String, String?>>) {
        tenNguoiDungMap = map
        notifyDataSetChanged()
    }

    fun setTenMonAnMap(map: Map<String, String>) {
        tenMonAnMap = map
        notifyDataSetChanged()
    }

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTenNguoiDung: TextView = itemView.findViewById(R.id.txtTenNguoiDung)
        val txtTenMonAn: TextView = itemView.findViewById(R.id.txtTenMonAn)
        val imgUserIcon: ImageView = itemView.findViewById(R.id.imgUserIcon)
        val txtNgayTao: TextView = itemView.findViewById(R.id.txtNgayTao)
        val imgDanhGia: ImageView = itemView.findViewById(R.id.imgDanhGia)
        val txtNoiDung: TextView = itemView.findViewById(R.id.txtNoiDung)
        val txtSoSao: TextView = itemView.findViewById(R.id.txtSoSao)
        val delButton: ImageView = itemView.findViewById(R.id.delButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]

        // Lấy tên người dùng từ map hoặc Firestore nếu chưa có
        val userId = review.idnd ?: ""
        if (tenNguoiDungMap.containsKey(userId)) {
            val (name, avatarUrl) = tenNguoiDungMap[userId] ?: Pair("Ẩn danh", null)
            holder.txtTenNguoiDung.text = name
            loadAvatar(holder.imgUserIcon, avatarUrl)
        } else {
            getUserInfo(userId) { name, avatarUrl ->
                holder.txtTenNguoiDung.text = name
                loadAvatar(holder.imgUserIcon, avatarUrl)
            }
        }

        // Lấy tên món ăn từ map
        holder.txtTenMonAn.text = tenMonAnMap[review.idma] ?: "Không rõ"

        holder.txtNoiDung.text = review.noi_dung
        holder.txtSoSao.text = "★".repeat(review.so_sao?.toIntOrNull() ?: 0)
        holder.txtNgayTao.text = review.created_at ?: "Không rõ"

//        Glide.with(context)
//            .load(review.avatar_url)
//            .placeholder(R.drawable.usernew)
//            .into(holder.imgUserIcon)

        Glide.with(context)
            .load(review.hinhanh_danhgia)
            .placeholder(R.drawable.banner1)
            .into(holder.imgDanhGia)

        holder.delButton.setOnClickListener {
            onDeleteClick(review)
        }
    }

    override fun getItemCount(): Int = reviewList.size

    fun updateList(newList: List<ReviewModel>) {
        reviewList = newList
        notifyDataSetChanged()
    }

    private fun getUserInfo(userId: String, callback: (String, String?) -> Unit) {
        if (userId.isBlank()) {
            callback("Ẩn danh", null)
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("nguoidung")
            .document(userId) // Sử dụng userId làm document ID thay vì tìm kiếm theo field
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Ẩn danh"
                    val avatarUrl = document.getString("avatarUrl") // Lấy URL avatar
                    callback(name, avatarUrl)
                } else {
                    callback("Ẩn danh", null)
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
