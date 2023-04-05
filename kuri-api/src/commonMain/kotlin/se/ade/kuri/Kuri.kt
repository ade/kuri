package se.ade.kuri

private val ALPHA_AND_DIGIT_CHARS = (('a'..'z') + ('A'..'Z') + ('0'..'9'))

/**
 * RFC 3986 section 2.3 Unreserved Characters (January 2005)
 * https://tools.ietf.org/html/rfc3986#section-2.3
 */
private val UNRESERVED_CHARS = listOf('-', '.', '_', '~') + ALPHA_AND_DIGIT_CHARS

/**
 * RFC 3986 section 2.2 Reserved Characters (January 2005)
 */
private val RESERVED_CHARS = setOf(
    ':', '/', '?', '#', '[', ']', '@', // "gen-delims"
    '!', '$', '&', '\'', '(', ')', '*', ',', ';', '=', // "sub-delims"
)

/**
 * RFC3986, section 3.4 advises against percent-encoding '/' and '?' characters in a query component for readability.
 */
private val QUERY_PART_NEVER_ENCODE = listOf('?', '/') + UNRESERVED_CHARS

object Kuri {
    /**
     * Encode a PATH parameter - a raw value that has been added as part of the path, but must be escaped
     * in order to not interfere with the URI structure.
     *
     * Example where <parameter> denotes the value of the String sent as input here:
     * http://example.com/message/<parameter>/text
     */
    fun encodeUrlPathParam(input: Any): String {
        return when(input) {
            is Boolean -> if(input) "1" else "0"
            is Int, is Long -> input.toString()
            else -> encodeURLPathParameter(input.toString())
        }
    }

    /**
     * Encodes the "Query component" of a URI according to RFC 3986 part 3.4 ("percent encoding")
     * The query component is the part of the URI after the first question mark ("?")
     * e.g. XYZ in "http://example.com/url?XYZ"
     *
     * @param spaceToPlus Boolean when true: Encode space as the plus (+) sign,
     * a special encoding used only for application/x-www-form-urlencoded content.
     */
    fun encodeUrlQuery(s: String) = encodeUrlQueryPart(s)

    /**
     * Encode a URL query parameter map, e.g. "http://example.com/url?a=1&b=2"
     * Note: Custom assigner and separator chars will not be encoded.
     */
    fun encodeUrlKeyValues(map: Map<String,Any?>, assigner: Char = '=', separator: Char = '&')
        = buildUrlKeyValuesEncoded(map, assigner, separator)
}


private fun encodeURLPathParameter(input: String): String {
    return buildString {
        input.forEach {
            when (it) {
                in UNRESERVED_CHARS -> append(it)
                else -> it.percentEncode(this)
            }
        }
    }
}


private fun encodeUrlQueryPart(input: String, spaceToPlus: Boolean = false): String {
    return buildString(input.length * 2) {
        input.forEach {
            when (it) {
                ' ' -> if (spaceToPlus) append('+') else append("%20")
                in QUERY_PART_NEVER_ENCODE -> append(it)
                else -> it.percentEncode(this)
            }
        }
    }
}

private fun buildUrlKeyValuesEncoded(map: Map<String, Any?>, assigner: Char = '=', separator: Char = '&'): Any {
    return buildString {
        map.forEach {
            if(it.value != null) {
                if (this.isNotEmpty()) append(separator)

                val value: String = when (val input = it.value) {
                    is Boolean -> if(input) "1" else "0"
                    else -> it.value.toString()
                }

                append(encodeUrlQueryPart(it.key))
                append(assigner)
                append(encodeUrlQueryPart(value))
            }
        }
    }
}

private fun Char.percentEncode(sink: StringBuilder) {
    toString().encodeToByteArray().forEach {
        it.percentEncode(sink)
    }
}

private fun Byte.percentEncode(sink: StringBuilder) = sink.let {
    val code = toInt() and 0xff
    it.append('%')
    it.append(intToHexChar(code shr 4))
    it.append(intToHexChar(code and 0x0f))
}

private fun intToHexChar(digit: Int): Char = when (digit) {
    in 0..9 -> '0' + digit
    else -> 'A' + digit - 10
}