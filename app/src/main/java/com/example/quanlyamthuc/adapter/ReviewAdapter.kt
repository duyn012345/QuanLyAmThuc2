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

    private var tenNguoiDungMap = mapOf<String, String>()
    private var tenMonAnMap = mapOf<String, String>()

    fun setTenNguoiDungMap(map: Map<String, String>) {
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
            holder.txtTenNguoiDung.text = tenNguoiDungMap[userId]
        } else {
            getUserName(userId) { name ->
                holder.txtTenNguoiDung.text = name
            }
        }

        // Lấy tên món ăn từ map
        holder.txtTenMonAn.text = tenMonAnMap[review.idma] ?: "Không rõ"

        holder.txtNoiDung.text = review.noi_dung
        holder.txtSoSao.text = "★".repeat(review.so_sao?.toIntOrNull() ?: 0)
        holder.txtNgayTao.text = review.created_at ?: "Không rõ"

        Glide.with(context)
            .load(review.avatar_url)
            .placeholder(R.drawable.usernew)
            .into(holder.imgUserIcon)

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

    private fun getUserName(userId: String, callback: (String) -> Unit) {
        if (userId.isBlank()) {
            callback("Ẩn danh")
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("nguoidung")
            .whereEqualTo("idnd", userId)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val name = documents.first().getString("name") ?: "Ẩn danh"
                    callback(name)
                } else {
                    callback("Ẩn danh")
                }
            }
            .addOnFailureListener {
                Log.e("GET_USER", "Lỗi Firestore: ${it.message}")
                callback("Lỗi")
            }
    }
}
