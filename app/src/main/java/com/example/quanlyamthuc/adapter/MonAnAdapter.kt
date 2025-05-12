package com.example.quanlyamthuc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.model.MonAn

class MonAnAdapter(
    private val listMonAn: List<MonAn>,
    private val onItemClick: (MonAn) -> Unit,
    private val layoutId: Int = R.layout.item_mon_an,
    private val mapTenTinh: Map<String, String> = emptyMap()
) : RecyclerView.Adapter<MonAnAdapter.MonAnViewHolder>() {


    class MonAnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMonAn: ImageView = itemView.findViewById(R.id.imgMonAn)
        val tvTenMonAn: TextView = itemView.findViewById(R.id.tvTenMonAn)
        val tvTenTinh: TextView = itemView.findViewById(R.id.tvTenTinh)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonAnViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MonAnViewHolder(view)
    }


    override fun onBindViewHolder(holder: MonAnViewHolder, position: Int) {
        val monAn = listMonAn[position]
        holder.tvTenMonAn.text = monAn.tenma


        val tenTinh = mapTenTinh[monAn.idtt] ?: "Không rõ"
        holder.tvTenTinh.text = tenTinh

        Glide.with(holder.itemView.context)
            .load(monAn.hinhanh)
            .placeholder(R.drawable.bn7)
            .error(R.drawable.bn6)
            .into(holder.imgMonAn)

        holder.itemView.setOnClickListener {
            onItemClick(monAn)
        }
    }


    override fun getItemCount(): Int = listMonAn.size
}
