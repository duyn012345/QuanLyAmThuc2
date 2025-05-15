package com.example.quanlyamthuc.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.model.ProvinceModel
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
class ProvinceAdapter(
    private var provinces: List<ProvinceModel>,
    private val provinceList: MutableList<ProvinceModel>,
    private val onItemClick: (ProvinceModel) -> Unit
) : RecyclerView.Adapter<ProvinceAdapter.ProvinceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProvinceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_province, parent, false)
        return ProvinceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProvinceViewHolder, position: Int) {
        val province = provinces[position]
        holder.nameTextView.text = province.name

        holder.btnEdit.setOnClickListener {
            showEditDialog(holder.itemView.context, province)
        }

        holder.btnDelete.setOnClickListener {
            showDeleteDialog(holder.itemView.context, province)
        }

        // Xử lý click vào item
        holder.itemView.setOnClickListener {
            onItemClick(province) // Gọi callback khi click vào tỉnh
        }
        // Load ảnh từ URL
        Picasso.get()
            .load(province.imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = provinces.size

    fun updateList(newList: List<ProvinceModel>) {
        provinces = newList
        notifyDataSetChanged()
    }

    inner class ProvinceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.provinceNameTextView)
        val imageView: ImageView = itemView.findViewById(R.id.provinceImageView)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    private fun showEditDialog(context: Context, province: ProvinceModel) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_or_edit, null)
        val builder = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Sửa Tỉnh")

        val nameEditText: EditText = dialogView.findViewById(R.id.nameEditText)
        val imageUrlEditText: EditText = dialogView.findViewById(R.id.imageUrlEditText)

        // Điền các giá trị hiện tại vào form
        nameEditText.setText(province.name)
        imageUrlEditText.setText(province.imageUrl)

        builder.setPositiveButton("Lưu") { dialog, _ ->
            val name = nameEditText.text.toString()
            val imageUrl = imageUrlEditText.text.toString()

            // Cập nhật thông tin tỉnh
            province.tentinh = name
            province.hinhanh = imageUrl
            val database = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app/")
            // val provinceRef = database.getReference("14/data")

            val provinceRef = database.getReference("14/data/${province.id}")
    provinceRef.setValue(province)
                .addOnSuccessListener {
                    Toast.makeText(context, "Cập nhật tỉnh thành công", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Cập nhật tỉnh thất bại: ${it.message}", Toast.LENGTH_LONG).show()
                }
            dialog.dismiss()
        }

        builder.setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }

    private fun showDeleteDialog(context: Context, province: ProvinceModel) {
        AlertDialog.Builder(context)
            .setTitle("Xóa tỉnh")
            .setMessage("Bạn có chắc chắn muốn xóa tỉnh ${province.name}?")
            .setPositiveButton("Xóa") { dialog, _ ->
                // Khởi tạo FirebaseDatabase với URL tùy chỉnh
                val database = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app/")
               // val provinceRef = database.getReference("14/data")

                val provinceRef = database.getReference("14/data/${province.id}")
                provinceRef.removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Xóa tỉnh thành công", Toast.LENGTH_SHORT).show()
                        provinceList.remove(province)
                        notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Xóa tỉnh thất bại: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}