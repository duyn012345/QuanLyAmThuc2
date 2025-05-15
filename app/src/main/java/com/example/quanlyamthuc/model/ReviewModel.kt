package com.example.quanlyamthuc.model

data class ReviewModel(
   var key: String? = null,
   var avatar_url: String? = null,
   var iddg: String? = null,
   var idma: String? = "",
   var idnd: String? = "",
   var so_sao: String? = null,
   var noi_dung: String? = null,
   var hinhanh_danhgia: String? = null,
   var created_at: String? = null,
   var updated_at: String? = null,
   val avatarNguoiDung: String = ""
)

// data class ReviewModel (
//    val iddg: String? = null,
//    val idma: String? = null,
//    val idnd: String? = null,
//    val noi_dung: String? = null,
//    val so_sao: String? = null,
//    val hinhanh_danhgia: String? = null,
//    val created_at: String? = null,
//    val updated_at: String? = null
//)