package com.example.quanlyamthuc.adapter

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.databinding.ItemImageBinding
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.DataSource

class ImageSliderAdapter(private var imageList: List<String>) :
    RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {

    // Sử dụng companion object để định nghĩa các hằng số
    companion object {
        private const val MAX_RETRY = 3 // Số lần thử lại khi load ảnh
        private const val CROSS_FADE_DURATION = 300 // Thời gian hiệu ứng chuyển ảnh
    }

    inner class ImageViewHolder(val binding: ItemImageBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

//    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
//        Glide.with(holder.itemView.context)
//            .load(imageList[position])
//            .into(holder.binding.imageView)
//    }
//override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
//    val imageUrl = imageList.getOrNull(position) ?: run {
//        Log.d("ImageURL", "URL is NULL - Using default image")
//        holder.binding.imageView.setImageResource(R.drawable.default_image)
//        return
//    }
//    Log.d("ImageURL", "Position $position - URL: $imageUrl")
//
//    if (imageUrl.isEmpty() || !imageUrl.startsWith("http")) {
//        Log.d("ImageURL", "Invalid URL - Using error image")
//        holder.binding.imageView.setImageResource(R.drawable.error_image)
//        return
//    }
//
////    Glide.with(holder.itemView.context)
////        .load(imageUrl)
////        .placeholder(R.drawable.loading_placeholder)
////        .error(R.drawable.error_image)
////        .transition(DrawableTransitionOptions.withCrossFade(300))
////        .into(holder.binding.imageView)
////}
//    Glide.with(holder.itemView.context)
//        .load(imageUrl)
//        .placeholder(R.drawable.loading_placeholder)
//        .error(R.drawable.error_image)
//        .listener(object : RequestListener<Drawable> {
//            override fun onLoadFailed(
//                e: GlideException?,
//                model: Any?,
//                target: Target<Drawable>?,
//                isFirstResource: Boolean): Boolean {
//                Log.e("GlideError", "Load failed for URL: $imageUrl", e) // Log lỗi chi tiết
//                return false
//            }
//            override fun onResourceReady(
//                resource: Drawable?,
//                model: Any?,
//                target:Target<Drawable>?,
//                dataSource: DataSource?, isFirstResource: Boolean): Boolean {
//                Log.d("GlideSuccess", "Image loaded successfully: $imageUrl") // Log khi thành công
//                return false
//            }
//        })
//        .transition(DrawableTransitionOptions.withCrossFade(300))
//        .into(holder.binding.imageView)
//}
override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
    val imageUrl = imageList.getOrNull(position)

    if (imageUrl.isNullOrEmpty() || !imageUrl.startsWith("http")) {
        Log.w("ImageSlider", "URL không hợp lệ tại vị trí $position: $imageUrl")
        holder.binding.imageView.setImageResource(R.drawable.loading)
        return
    }

    Glide.with(holder.itemView.context)
        .load(imageUrl)
        .placeholder(R.drawable.loading_placeholder)
        .error(R.drawable.loading)
        .listener(object : RequestListener<Drawable?> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable?>,
                isFirstResource: Boolean
            ): Boolean {
                Log.e("GlideError", "Load failed for URL: $imageUrl", e)
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable?>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                Log.d("GlideSuccess", "Image loaded successfully: $imageUrl")
                return false
            }
        })
        .transition(DrawableTransitionOptions.withCrossFade(300))
        .into(holder.binding.imageView)

}

    override fun getItemCount(): Int = imageList.size

    fun updateImages(newImages: List<String>) {
        imageList = newImages.filter { it.isNotBlank() }
        notifyDataSetChanged()
    }
}