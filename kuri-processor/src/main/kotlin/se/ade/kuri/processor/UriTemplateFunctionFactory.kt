package se.ade.kuri.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toTypeName
import se.ade.kuri.Kuri
import se.ade.kuri.KuriInternals
import se.ade.kuri.Query
import se.ade.kuri.Unescaped

private val KuriMemberName = Kuri::class.asTypeName()

sealed class PathFragment {
    data class Literal(val value: String): PathFragment()
    data class Placeholder(val name: String): PathFragment()
}

@OptIn(KspExperimental::class)
class UriTemplateFunctionFactory(val logger: KSPLogger) {
    fun create(resolver: Resolver, funName: String, template: String, parameters: List<KSValueParameter>): FunSpec {

        val retFunc = FunSpec.builder(funName)
            .addModifiers(KModifier.OVERRIDE)
            .returns(String::class)

        parameters.forEach {
            val paramName = it.name!!.getShortName()
            val paramType = it.type.resolve()

            retFunc.addParameter(paramName, paramType.toTypeName())
        }

        val queryParams = parameters.filter { it.isAnnotationPresent(Query::class) }

        val fragments = divide(template)

        val statement = buildCodeBlock {
            beginControlFlow("return buildString(${template.length * 2 + queryParams.size * 16})")

            fragments.forEach {
                when(it) {
                    is PathFragment.Literal -> addStatement("append(\"${it.value}\")")
                    is PathFragment.Placeholder -> {
                        val unescaped = parameters.firstOrNull { p -> it.name == p.name?.getShortName() }
                            ?.isAnnotationPresent(Unescaped::class)
                            ?: false

                        if(unescaped) {
                            addStatement("append(${it.name})")
                        } else {
                            addStatement("append(%T.encodeUrlPathParam(${it.name}))", KuriMemberName)
                        }
                    }
                }
            }

            val listType = resolver.getClassDeclarationByName("kotlin.collections.Collection")!!.asStarProjectedType()

            // We check so that we don't append the "?" query string when there are no query parameters to add.
            // Some parameters are nullable - if all are nullable, we can skip the "?" query string.
            // Additionally - Collections can be empty, so we check for that as well.
            val paramChecks = queryParams.map {
                val resolvedType = it.type.resolve()

                if(listType.isAssignableFrom(it.type.resolve())) {
                    "!${it.name?.getShortName()}.isEmpty()"
                } else if(listType.makeNullable().isAssignableFrom(it.type.resolve())) {
                    "${it.name?.getShortName()} != null && !${it.name?.getShortName()}.isEmpty()"
                } else if(resolvedType.isMarkedNullable) {
                    "${it.name?.getShortName()} != null"
                } else  {
                    // This parameter is always present, which means we'll always append the "?" query string.
                    // We save a null value here in order to know that we should not add any check.
                    null
                }
            }

            val addChecks = paramChecks.isNotEmpty() && !paramChecks.contains(null)

            if (queryParams.isNotEmpty()) {
                if(addChecks) {
                   // if(listChecks.isNotEmpty()) throw RuntimeException("LÖl $listChecks")
                    val nullChecks = paramChecks.joinToString(separator = " || ")
                    beginControlFlow("if($nullChecks)")
                }

                addStatement("append('?')")

                addStatement("append(%T.encodeUrlKeyValues(", KuriMemberName)
                indent()
                beginControlFlow("buildMap<String,Any?> {")

                queryParams.forEach {
                    val a = it.getAnnotationsByType(Query::class).singleOrNull()
                        ?: throw IllegalArgumentException("${Query::class.simpleName} annotation specified incorrectly")

                    val signatureName = it.name?.getShortName()
                    val name = if (a.name != "") a.name else signatureName

                    addStatement("""put("$name", $signatureName)""")
                }

                endControlFlow()
                unindent()
                addStatement("))")

                if(addChecks) {
                    endControlFlow()
                }
            }
            endControlFlow()
        }

        retFunc.addCode(statement)

        val kdocQueryString = if(queryParams.isNotEmpty())
            "?" + queryParams.joinToString(prefix = "(", postfix = ")") {
                it.name?.getShortName() + if(it.type.resolve().isMarkedNullable) "?" else ""
            }
        else ""

        retFunc.addKdoc("URI Template: $template$kdocQueryString")

        return retFunc.build()
    }

    private fun divide(template: String): List<PathFragment> {
        return buildList {
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
    }
}