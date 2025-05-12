package com.example.quanlyamthuc.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.databinding.ItemUserBinding
import com.example.quanlyamthuc.model.UserModel

class UserAdapter(
    private var userList: MutableList<UserModel>,
   // private val onEditClick: (UserModel) -> Unit,
    private val onDeleteClick: (UserModel) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserModel) {
            binding.userNameTextView.text = user.name ?: "No Name"
            binding.userEmailTextView.text = user.email ?: "No Email"
            binding.userRoleTextView.text = user.role ?: "No Role"
            binding.userPhoneTextView.text = user.phone ?: "No Phone"
            binding.userBioTextView.text = user.bio ?: "No Bio"

            // Load avatar với Glide
            if (!user.avatarUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(user.avatarUrl)
                    .override(80, 80)
                    .circleCrop() // Hiển thị ảnh tròn
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .error(R.drawable.baseline_account_circle_24)
                    .into(binding.userAvatarImageView)
            } else {
                binding.userAvatarImageView.setImageResource(R.drawable.baseline_account_circle_24)
            }

           // binding.editButton.setOnClickListener { onEditClick(user) }
            binding.btnDelete.setOnClickListener { onDeleteClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size

    fun updateList(newList: List<UserModel>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = userList.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                userList[oldPos].idnd == newList[newPos].idnd
            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                userList[oldPos] == newList[newPos]
        })

        userList = newList.toMutableList()
        diffResult.dispatchUpdatesTo(this)

        Log.d("UserAdapter", "Updated with ${userList.size} items")
    }

//    fun updateList(newList: List<UserModel>) {
//        Log.d("UserAdapter", "Before update, userList size: ${userList.size}")
//        userList.clear()
//        userList.addAll(newList)
//        Log.d("UserAdapter", "After update, userList size: ${userList.size}")
//        notifyDataSetChanged()
//    }
}