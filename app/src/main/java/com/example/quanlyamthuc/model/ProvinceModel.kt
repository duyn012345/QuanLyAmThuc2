package com.example.quanlyamthuc.model

data class ProvinceModel(
    // Mã tỉnh (nếu có)
    var tentinh: String? = null,         // Tên tỉnh
    var hinhanh: String? = null,
    var id: String? = null,              // Firebase key
    var idtt: String? = null,   // URL hình ảnh
    var created_at: String? = null,
    var updated_at: String? = null
) {
    val name: String
        get() = tentinh ?: ""

    val imageUrl: String
        get() = hinhanh ?: ""
}
