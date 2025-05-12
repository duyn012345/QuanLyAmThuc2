package com.example.quanlyamthuc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.model.DanhGia

class DanhGiaAdapter(private val list: List<DanhGia>) : RecyclerView.Adapter<DanhGiaAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val soSao: TextView = view.findViewById(R.id.tvSoSao)
        val ngayNX: TextView = view.findViewById(R.id.tvNgayNX)
        val noiDung: TextView = view.findViewById(R.id.tvNoiDung)
        val idNguoiDung: TextView = view.findViewById(R.id.tvIdNguoiDung)
        val imgDanhGia: ImageView = view.findViewById(R.id.imgDanhGia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_danhgia, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dg = list[position]
        holder.idNguoiDung.text = dg.idnd
        holder.soSao.text = "⭐".repeat(dg.so_sao.toIntOrNull() ?: 0)
        holder.ngayNX.text = dg.created_at
        holder.noiDung.text = dg.noi_dung
        if (!dg.hinhanh_danhgia.isNullOrEmpty()) {
            holder.imgDanhGia.visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(dg.hinhanh_danhgia).into(holder.imgDanhGia)
        } else {
            holder.imgDanhGia.visibility = View.GONE
        }
    }

    override fun getItemCount() = list.size
}
