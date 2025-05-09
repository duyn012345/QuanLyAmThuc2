package com.example.quanlyamthuc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.model.TinhThanh

class TinhThanhAdapter(private val tinhList: List<TinhThanh>) :
    RecyclerView.Adapter<TinhThanhAdapter.TinhThanhViewHolder>() {

    inner class TinhThanhViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTenTinh = itemView.findViewById<TextView>(R.id.tvTenTinh)
        val imgTinh = itemView.findViewById<ImageView>(R.id.imgTinh)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TinhThanhViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tinh_thanh, parent, false)
        return TinhThanhViewHolder(view)
    }

    override fun onBindViewHolder(holder: TinhThanhViewHolder, position: Int) {
        val tinh = tinhList[position]
        holder.tvTenTinh.text = tinh.tentinh
        Glide.with(holder.itemView.context)
            .load(tinh.hinhanh)
            .placeholder(R.drawable.restorent) // đảm bảo bạn có ảnh này trong drawable
            .into(holder.imgTinh)
    }

    override fun getItemCount(): Int = tinhList.size
}
