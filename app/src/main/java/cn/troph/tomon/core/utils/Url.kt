package cn.troph.tomon.core.utils

object Url {
    const val inviteUrl = "https://beta.tomon.co/invite/"
    const val inviteFormat = "https://beta.tomon.co/invite/%s?t=%s"
    fun parseInviteCode(url: String): String {
        val codeTicketPattern = """[0-9a-zA-Z]{6}\?t=[0-9a-zA-Z]{6}""".toRegex()
        val urlPattern = """(https://beta\.tomon\.co/invite/)""".toRegex()
        val codePattern = """[0-9a-zA-Z]{6}""".toRegex()
        val ticketPattern = """\?t=""".toRegex()
        val code = urlPattern.split(url)
        if (code[0] == "" && codeTicketPattern.matches(code[1])) {
            val codeDoubleParsed = code[1].split(ticketPattern)
            return if (codePattern.matches(codeDoubleParsed[0])) codeDoubleParsed[0] else ""
        }
        return if (code[0] == "" && codePattern.matches(code[1])) code[1] else ""
    }
}