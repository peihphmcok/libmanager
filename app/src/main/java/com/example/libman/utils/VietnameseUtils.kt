package com.example.libman.utils

object VietnameseUtils {
    
    /**
     * Normalize Vietnamese text by removing diacritics for better search matching
     * @param text The Vietnamese text to normalize
     * @return Normalized text without diacritics
     */
    fun normalizeVietnamese(text: String): String {
        return text
            .replace("à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ".toRegex(), "a")
            .replace("è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ".toRegex(), "e")
            .replace("ì|í|ị|ỉ|ĩ".toRegex(), "i")
            .replace("ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ".toRegex(), "o")
            .replace("ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ".toRegex(), "u")
            .replace("ỳ|ý|ỵ|ỷ|ỹ".toRegex(), "y")
            .replace("đ".toRegex(), "d")
            .replace("À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ".toRegex(), "A")
            .replace("È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ".toRegex(), "E")
            .replace("Ì|Í|Ị|Ỉ|Ĩ".toRegex(), "I")
            .replace("Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ".toRegex(), "O")
            .replace("Ù|Ú|Ụ|Ủ|Ũ|Ư|Ừ|Ứ|Ự|Ử|Ữ".toRegex(), "U")
            .replace("Ỳ|Ý|Ỵ|Ỷ|Ỹ".toRegex(), "Y")
            .replace("Đ".toRegex(), "D")
    }
    
    /**
     * Check if search query matches text (case-insensitive, diacritic-insensitive)
     * @param text The text to search in
     * @param query The search query
     * @return true if query matches text
     */
    fun matchesVietnamese(text: String?, query: String?): Boolean {
        if (text == null || query == null) return false
        val normalizedText = normalizeVietnamese(text.lowercase())
        val normalizedQuery = normalizeVietnamese(query.lowercase())
        return normalizedText.contains(normalizedQuery)
    }
}
