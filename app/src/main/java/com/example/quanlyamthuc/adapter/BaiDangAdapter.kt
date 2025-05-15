package com.example.quanlyamthuc.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.model.DangBai
import com.google.firebase.firestore.FirebaseFirestore

class BaiDangAdapter(private val list: List<DangBai>) :
    RecyclerView.Adapter<BaiDangAdapter.BaiDangViewHolder>() {

    inner class BaiDangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTenNguoiDang: TextView = itemView.findViewById(R.id.tvTenNguoiDang)
        val tvThoiGian: TextView = itemView.findViewById(R.id.tvThoiGian)
        val imgMonAn: ImageView = itemView.findViewById(R.id.imgMonAn)
        val tvTenMonAn: TextView = itemView.findViewById(R.id.tvTenMonAn)
        val tvTinhThanh: TextView = itemView.findViewById(R.id.tvTinhThanh)
        val tvNoiDung: TextView = itemView.findViewById(R.id.tvNoiDung)
        val tvXemThem: TextView = itemView.findViewById(R.id.tvXemThem) // Xem thêm
        val tvAn: TextView = itemView.findViewById(R.id.tvAn) // Thêm dòng này để tham chiếu tvAn
       // val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaiDangViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bai_dang, parent, false)
        return BaiDangViewHolder(view)
    }

    override fun onBindViewHolder(holder: BaiDangViewHolder, position: Int) {
        val item = list[position]

        holder.tvTenMonAn.text = item.tenmonan
        holder.tvTinhThanh.text = "  ${item.tinhthanh}"
        holder.tvNoiDung.text = item.noidung
        holder.tvThoiGian.text = item.created_at

        // Load ảnh
        Glide.with(holder.itemView.context)
            .load(item.hinhanh_ma)
            .into(holder.imgMonAn)

        // Load tên người đăng từ Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("nguoidung")
            .whereEqualTo("idnd", item.idnd)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val ten = document.getString("name") ?: "Ẩn danh"
                    holder.tvTenNguoiDang.text = "$ten"

                }
            }
            .addOnFailureListener {
                holder.tvTenNguoiDang.text = "Đăng bởi: [Không tải được]"
            }

        // Hiển thị hoặc ẩn "Xem thêm"
        if (item.noidung.length > 100) {
            holder.tvXemThem.visibility = View.VISIBLE
        } else {
            holder.tvXemThem.visibility = View.GONE
        }

        holder.tvXemThem.setOnClickListener {
            holder.tvNoiDung.maxLines = Int.MAX_VALUE
            holder.tvNoiDung.requestLayout() // Yêu cầu cập nhật layout ngay lập tức
            holder.tvXemThem.visibility = View.GONE
            holder.tvAn.visibility = View.VISIBLE
        }

        holder.tvAn.setOnClickListener {
            holder.tvNoiDung.maxLines = 2
            holder.tvNoiDung.requestLayout() // Yêu cầu cập nhật layout ngay lập tức
            holder.tvAn.visibility = View.GONE
            holder.tvXemThem.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = list.size
}
