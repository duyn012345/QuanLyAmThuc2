package com.example.quanlyamthuc.utils

object StringUtils {
    fun String.removeVietnameseAccents(): String {
        val normalized = java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }

}