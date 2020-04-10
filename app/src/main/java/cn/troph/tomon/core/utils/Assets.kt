package cn.troph.tomon.core.utils

object Assets {
    fun emojiURL(id: String, animated: Boolean = false): String {
        return "https://cdn.tomon.co/emojis/$id.${if (animated) "gif" else "png"}"
    }
}