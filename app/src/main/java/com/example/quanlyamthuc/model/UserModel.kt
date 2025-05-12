package com.example.quanlyamthuc.model

data class UserModel(
    var idnd: String? = "",
    var name: String = "",
    var email: String = "",
    var role: String = "user",
    var phone: String = "",
    var bio: String = "",
    var avatarUrl: String = ""
)
