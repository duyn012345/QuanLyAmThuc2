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

class DanhGiaAdapter(private val danhSach: List<DanhGia>) :
    RecyclerView.Adapter<DanhGiaAdapter.DanhGiaViewHolder>() {

    class DanhGiaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNoiDung: TextView = view.findViewById(R.id.txtNoiDung)
        val txtSoSao: TextView = view.findViewById(R.id.txtSoSao)
        val imgDanhGia: ImageView = view.findViewById(R.id.imgDanhGia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DanhGiaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_danhgia, parent, false)
        return DanhGiaViewHolder(view)
    }

    override fun onBindViewHolder(holder: DanhGiaViewHolder, position: Int) {
        val danhGia = danhSach[position]
        holder.txtNoiDung.text = danhGia.noi_dung
        holder.txtSoSao.text = "★ ${danhGia.so_sao}/5"
        Glide.with(holder.itemView.context)
            .load(danhGia.hinhanh_danhgia)
            .into(holder.imgDanhGia)
    }

    override fun getItemCount() = danhSach.size
}
