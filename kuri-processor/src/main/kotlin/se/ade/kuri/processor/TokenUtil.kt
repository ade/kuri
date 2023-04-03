package se.ade.kuri.processor

object TokenUtil {
    fun getTokensFromTemplate(template: String, startToken: String, endToken: String = ""): List<String> {
        return if(endToken.isEmpty()) {
            getTokensStartOnly(template, startToken)
        } else {
            getTokensStartAndEnd(template, startToken, endToken)
        }
    }

    private fun getTokensStartOnly(template: String, startToken: String): List<String> {
        val start = Regex.escape(startToken)
        val regex = "$start([A-Za-z0-9_]+)".toRegex()
        val matches = regex.findAll(template)

        return matches
            .toList()
            .map { it.groupValues.drop(1) }
            .flatten()
    }

    private fun getTokensStartAndEnd(template: String, startToken: String, endToken: String): List<String> {
        return buildList {
            var opening = true
            var pos = 0
            while(pos < template.length) {
                if(opening) {
                    pos = template.indexOf(startToken, pos)
                    opening = false
                    if(pos == -1) return@buildList
                } else {
                    val endPos = template.indexOf(endToken, pos+startToken.length)
                    if(endPos == -1) throw IllegalArgumentException("Bad template string \"$template\"")
                    val value = template.substring(pos+startToken.length, endPos)
                    add(value)
                    opening = true
                    pos = endPos+endToken.length
                }
            }
        }
    }
}