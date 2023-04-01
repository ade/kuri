package se.ade

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import se.ade.kuri.Kuri
import se.ade.kuri.KuriInternals
import se.ade.kuri.Query

private val KuriMemberName = Kuri::class.asTypeName()

sealed class PathFragment {
    data class Literal(val value: String): PathFragment()
    data class Placeholder(val name: String): PathFragment()
}

@OptIn(KspExperimental::class)
class UriTemplateFunctionFactory(val logger: KSPLogger) {
    fun create(funName: String, template: String, parameters: List<KSValueParameter>): FunSpec {
        val queryParams = parameters.filter { it.isAnnotationPresent(Query::class) }

        val retFunc = FunSpec.builder(funName)
            .addModifiers(KModifier.OVERRIDE)
            .returns(String::class)

        parameters.forEach {
            val paramName = it.name!!.getShortName()
            val paramType = it.type.resolve()

            retFunc.addParameter(paramName, paramType.toTypeName())
        }

        val fragments = buildList<PathFragment> {
            var inPlaceholder = false
            var position = 0
            val buffer = arrayOfNulls<Char>(template.length)
            template.forEachIndexed { index, it ->
                if(it.toString() == KuriInternals.BEGIN_TOKEN) {
                    if(inPlaceholder) throw IllegalArgumentException("Syntax error: double opening of placeholder in template: $template")
                    add(PathFragment.Literal(buffer.take(position).joinToString("")))
                    inPlaceholder = true
                    position = 0
                } else if(it.toString() == KuriInternals.END_TOKEN) {
                    if (!inPlaceholder) throw IllegalArgumentException("Syntax error: premature end of placeholder in template: $template")
                    add(PathFragment.Placeholder(buffer.take(position).joinToString("")))
                    inPlaceholder = false
                    position = 0
                } else {
                    buffer[position++] = it

                    if(index == template.lastIndex) {
                        if(inPlaceholder) throw IllegalArgumentException("Unfinished template in $template")
                        else add(PathFragment.Literal(buffer.take(position).joinToString("")))
                    }
                }
            }
        }

        val queryParamSpace = if(queryParams.isNotEmpty()) 64 + queryParams.size * 32 else 0
        val args = mutableListOf<Any>()

        val statement = buildString(fragments.size * 16 + queryParamSpace) {
            appendLine("return buildString(${template.length * 2}) {")
            fragments.forEach {
                when(it) {
                    is PathFragment.Literal -> appendLine("\tappend(\"${it.value}\")")
                    is PathFragment.Placeholder -> {
                        args.add(KuriMemberName)
                        appendLine("\tappend(%T.encodeUrlPathParam(${it.name}))")
                    }
                }
            }
            if (queryParams.isNotEmpty()) {
                //If any query param != null, append "?"
                append("\tif(")
                append(queryParams.joinToString(separator = " || ") { "${it.name?.getShortName()} != null" })
                appendLine(") {")
                appendLine("\tappend('?')")

                appendLine("\tappend(%T.encodeUrlKeyValues(buildMap<String,Any?> {")
                args.add(KuriMemberName)

                queryParams.forEach {
                    val a = it.getAnnotationsByType(Query::class).singleOrNull()
                        ?: throw IllegalArgumentException("${Query::class.simpleName} annotation specified incorrectly")

                    val signatureName = it.name?.getShortName()
                    val name = if (a.name != "") a.name else signatureName

                    append("\t\t")
                    appendLine("""put("$name", $signatureName)""")
                }
                appendLine("\t}))")
                appendLine("\t}")
            }
            appendLine("}")
        }

        retFunc.addStatement(statement, *args.toTypedArray())

        retFunc.addKdoc("URI Template: $template")

        return retFunc.build()
    }
}