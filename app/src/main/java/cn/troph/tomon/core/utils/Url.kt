package cn.troph.tomon.core.utils

object Url {
    const val inviteUrl = "https://beta.tomon.co/invite/"
    fun parseInviteCode(url: String): String {
        val codePattern = """[0-9a-zA-Z]+""".toRegex()
        val urlPattern = """(https://beta\.tomon\.co/invite/)""".toRegex()
        val code = urlPattern.split(url)
        return if (code[0] == "" && codePattern.matches(code[1])) code[1] else ""
    }
}