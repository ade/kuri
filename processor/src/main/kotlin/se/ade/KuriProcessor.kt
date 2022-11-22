package se.ade

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import se.ade.kuri.UriTemplate
import se.ade.kuri.Kuri
import se.ade.kuri.KuriInternals
import se.ade.kuri.UriProvider
import java.io.OutputStream

private const val kuriPackageName = "se.ade.kuri"
private val kuriProviderName = UriProvider::class.simpleName
private val KuriClassName = Kuri::class.simpleName!!

private const val DEFAULT_PLACEHOLDER_START_TOKEN = "{"
private const val DEFAULT_PLACEHOLDER_END_TOKEN = "}"

class KuriProcessor(
    private val options: Map<String, String>,
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    val placeholderStartToken = option("placeholder.start")
        ?: DEFAULT_PLACEHOLDER_START_TOKEN
    val placeholderEndToken = option("placeholder.end")
        ?: DEFAULT_PLACEHOLDER_END_TOKEN

    private fun option(key: String): String? {
        return options["$kuriPackageName.$key"]
    }

    operator fun OutputStream.plusAssign(str: String) {
        this.write(str.toByteArray())
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation("$kuriPackageName.$kuriProviderName")
            .filterIsInstance<KSClassDeclaration>()

        if (!symbols.iterator().hasNext())
            return emptyList()

        symbols.forEach { it.accept(Visitor(), Unit) }

        return symbols.filterNot { it.validate() }.toList()
    }

    @OptIn(KspExperimental::class)
    inner class Visitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            assert (classDeclaration.classKind == ClassKind.INTERFACE) {
                "Only interface can be annotated with @$kuriProviderName"
            }

            val classPackageName = classDeclaration.packageName.asString()
            val className = classDeclaration.simpleName.getShortName()
            val src = classDeclaration.containingFile?.let { arrayOf(it) } ?: arrayOf()

            val file = codeGenerator.createNewFile(
                dependencies = Dependencies(aggregating = false, *src),
                packageName = classPackageName,
                fileName = "Kuri$className"
            )

            file += "package $classPackageName\n"

            val classFunctions = classDeclaration.getAllFunctions().filter { it.isAbstract }
            assert(classFunctions.all {
                it.isAnnotationPresent(UriTemplate::class)
            }) {
                "All abstract functions must implement ${UriTemplate::class.simpleName} in $classPackageName.$className "
            }

            assert(classDeclaration.getAllFunctions().filter { it.isAnnotationPresent(UriTemplate::class) }.all {
                it.isAbstract
            }) {
                "Only abstract functions may implement ${UriTemplate::class.simpleName} in $classPackageName.$className"
            }

            val internalModifier = if(classDeclaration.isInternal())
                "internal "
            else
                ""

            //file += "import $classPackageName.$className\n"
            file += "import $kuriPackageName.*\n"
            file += "\n"
            file += internalModifier
            file += "class Kuri${className}: $className {\n"

            classFunctions.forEach { prop ->
                visitFunction(file, prop)
            }

            file += "}\n"
            file.close()
        }

        private fun visitFunction(file: OutputStream, func: KSFunctionDeclaration) {
            if(func.returnType?.resolve().toString() != "String") {
                throw RuntimeException("${functionErrorReference(func)} must return a String.")
            }

            val funName = func.simpleName.getShortName()

            val template = (func.annotations.firstOrNull()?.arguments?.firstOrNull()?.value as? String)
                ?: throw java.lang.RuntimeException("Can't read template string: ${functionErrorReference(func)}")

            val paramNames = func.parameters.map {
                it.name?.getShortName()
                    ?: throw IllegalArgumentException("Parameters must be named. ${functionErrorReference(func)}")
            }

            val placeholders = TokenUtil.getTokensFromTemplate(template, placeholderStartToken, placeholderEndToken)

            val paramSet = paramNames.toSet()
            val placeholderSet = placeholders.toSet()

            assert(paramSet == placeholderSet) {
                "Parameters should match placeholders.\n" +
                        "Parameters: $paramNames, placeholders: $placeholders\n" +
                        "(${functionErrorReference(func)})"
            }

            // rebuild template string to internal format
            val internalToken = KuriInternals.TOKEN
            var templateInternal = template
            paramNames.forEach {
                templateInternal = templateInternal.replace(
                    "${placeholderStartToken}$it${placeholderEndToken}",
                    "$internalToken$it$internalToken"
                )
            }

            // arguments list compiled, e.g. "x: String, y: Int"
            val paramsDeclaration = func.parameters.joinToString(", ") {
                val paramName = it.name!!.getShortName()
                val paramType = it.type.resolve().declaration.qualifiedName!!.asString()

                "$paramName: $paramType"
            }

            val expression = buildString {
                append(KuriClassName)
                append(".build(template = \"")
                append(templateInternal.replace("$", "\$"))
                append("\", ")
                append(paramNames.joinToString(", ") { s -> "\"$s\" to $s" })
                append(")")
            }

            file += "\toverride fun $funName($paramsDeclaration) = $expression\n"
        }

        private fun functionErrorReference(func: KSFunctionDeclaration): String {
            val location = func.location as? FileLocation
            return "function ${func}, declared at ${location?.filePath}:${location?.lineNumber}"
        }
    }
}