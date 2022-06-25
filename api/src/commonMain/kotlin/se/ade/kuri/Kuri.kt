package se.ade.kuri

private val ALLOWED_PLAIN_CHARS = (('a'..'z') + ('A'..'Z') + ('0'..'9')) + listOf('-', '.', '_', '~')
private const val TOKEN = KuriInternals.TOKEN

object Kuri {
    fun build(template: String, vararg tokens: Pair<String, Any>): String {
        var output = template
        tokens.forEach {
            output = output.replace("$TOKEN${it.first}$TOKEN", format(it.second))
        }
        return output
    }
    fun format(it: Any?): String {
        return when(it) {
            is Char -> it.toString()
            is Int -> it.toString()
            is Long -> it.toString()
            else -> encodeURLParameter(it.toString())
        }
    }
}

private fun encodeURLParameter(input: String, spaceAsPlus: Boolean = false): String {
    return buildString {
        input.forEach {
            when {
                it in ALLOWED_PLAIN_CHARS -> append(it)
                spaceAsPlus && it == ' ' -> append('+')
                else -> it.percentEncode(this)
            }
        }
    }
}

private fun Char.percentEncode(to: StringBuilder) = to.apply {
    val code = code and 0xff
    append('%')
    append(intToHexChar(code shr 4))
    append(intToHexChar(code and 0x0f))
}

private fun intToHexChar(digit: Int): Char = when (digit) {
    in 0..9 -> '0' + digit
    else -> 'A' + digit - 10
}