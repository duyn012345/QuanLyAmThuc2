package com.example.quanlyamthuc.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.databinding.ItemDishBinding
import com.example.quanlyamthuc.model.DishModel
class DishAdapter(
    private var monAnList: List<DishModel>,
    private val context: Context,
    private val onEditClick: (DishModel) -> Unit,
    private val onDeleteClick: (DishModel) -> Unit
) : RecyclerView.Adapter<DishAdapter.MonAnViewHolder>() {

    fun updateList(newList: List<DishModel>) {
        monAnList = newList
        notifyDataSetChanged()
        Log.d("DishAdapter", "Danh sách đã cập nhật với ${newList.size} món")
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonAnViewHolder {
        val binding = ItemDishBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MonAnViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonAnViewHolder, position: Int) {

        val dish = monAnList[position]
        holder.bind(dish)

//        holder.btnEdit.setOnClickListener {
//            onEditClick(dish)
//        }
//
//        holder.btnDelete.setOnClickListener {
//            onDeleteClick(dish)
//        }

    }

    override fun getItemCount(): Int = monAnList.size

    inner class MonAnViewHolder(private val binding: ItemDishBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)


        fun bind(monAn: DishModel) {
            binding.txtTenMonAn.text = monAn.tenma
            binding.txtTenDiaChi.text = monAn.diachi
            binding.txtGioiThieu.text = monAn.gioithieu
            binding.txtGia.text = monAn.giaca
            binding.txtMoTa.text = monAn.mota
            // binding.txtLinkDc.text = monAn.duonglink_diachi
            //xử lí đc
            // Hiển thị địa chỉ ngắn gọn
            binding.txtDiaChi.text = "Xem địa chỉ trên bản đồ"

            // Xử lý click vào địa chỉ
            binding.layoutAddress.setOnClickListener {
                val gmmIntentUri = Uri.parse(monAn.duonglink_diachi)
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")

                // Kiểm tra xem có ứng dụng bản đồ nào có thể xử lý intent không
                if (mapIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(mapIntent)
                } else {
                    // Nếu không có ứng dụng bản đồ, mở trình duyệt
                    val browserIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    context.startActivity(browserIntent)
                }
            }
//            binding.txtLinkDc.apply {
//                text = monAn.duonglink_diachi
//                setTextColor(Color.BLUE) // Màu cho giống hyperlink
//                paint.isUnderlineText = true // Gạch chân
//
//                setOnClickListener {
//                    val gmmIntentUri = Uri.parse(monAn.duonglink_diachi)
//                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
//                    mapIntent.setPackage("com.google.android.apps.maps")
//                    it.context.startActivity(mapIntent)
//                }
//            }

            // Hiển thị ảnh
            val imageList = listOfNotNull(
                monAn.hinhanh?.takeIf { it.isNotBlank() },
                monAn.hinhanh2?.takeIf { it.isNotBlank() },
                monAn.hinhanh3?.takeIf { it.isNotBlank() }
            )
//            val imageAdapter = ImageSliderAdapter(imageList)
//            binding.viewPager.adapter = imageAdapter
            if (imageList.isNotEmpty()) {
                binding.viewPager.visibility = View.VISIBLE
                binding.viewPager.adapter = ImageSliderAdapter(imageList)
            } else {
                binding.viewPager.visibility = View.GONE
            }

            // Xem thêm/thu gọn mô tả
            binding.txtXemThem.setOnClickListener {
                onSeeMoreClicked(binding.txtMoTa, binding.txtXemThem)
            }
            btnEdit.setOnClickListener { onEditClick(monAn) }
            btnDelete.setOnClickListener { onDeleteClick(monAn) }
        }
        private fun onSeeMoreClicked(txtMoTa: TextView, txtXemThem: TextView) {
            // Toggle visibility of description
            if (txtMoTa.visibility == View.GONE) {
                txtMoTa.visibility = View.VISIBLE
                txtXemThem.text = "Thu gọn"
            } else {
                txtMoTa.visibility = View.GONE
                txtXemThem.text = "Xem thêm"
            }
        }
    }
}