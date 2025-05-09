package com.example.quanlyamthuc.model

import java.io.Serializable

data class MonAn(
    val idma: String = "",
    val idtt: String = "",
    val tenma: String = "",
    val mota: String = "",
    val hinhanh: String = "",
    val gioithieu: String = "",
    val diachi: String = "",
    val hinhanh2: String = "",
    val hinhanh3: String = "",
    val duonglink_diachi: String = "",
    val giaca: String = "",
    val created_at: String="",
    val updated_at: String="",

): Serializable
