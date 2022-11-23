package se.ade

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import se.ade.kuri.UriTemplate
import se.ade.kuri.Kuri
import se.ade.kuri.KuriInternals
import se.ade.kuri.UriProvider
import java.io.OutputStream

private const val kuriPackageName = "se.ade.kuri"
private val kuriProviderName = UriProvider::class.simpleName
private val KuriClassName = Kuri::class.simpleName!!
private val KuriMemberName = Kuri::class.asTypeName()

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

            val src = classDeclaration.containingFile?.let { listOf(it) } ?: listOf()

            val poetFile = FileSpec.builder(classPackageName, "Kuri$className")
            val poetClass = TypeSpec.classBuilder("Kuri$className")
                .addSuperinterface(classDeclaration.toClassName())

            if(classDeclaration.isInternal())
                poetClass.addModifiers(KModifier.INTERNAL)

            val memberFunctions = classDeclaration.getAllFunctions().filter { it.isAbstract }
            assert(memberFunctions.all {
                it.isAnnotationPresent(UriTemplate::class)
            }) {
                "All abstract functions must implement ${UriTemplate::class.simpleName} in $classPackageName.$className "
            }

            assert(classDeclaration.getAllFunctions().filter { it.isAnnotationPresent(UriTemplate::class) }.all {
                it.isAbstract
            }) {
                "Only abstract functions may implement ${UriTemplate::class.simpleName} in $classPackageName.$className"
            }

            memberFunctions.forEach {
                poetClass.addFunction(implementFunction(it))
            }

            poetFile.addType(poetClass.build())

            poetFile.build().writeTo(codeGenerator, aggregating = false, originatingKSFiles = src)
        }

        private fun functionErrorReference(func: KSFunctionDeclaration): String {
            val location = func.location as? FileLocation
            return "function ${func}, declared at ${location?.filePath}:${location?.lineNumber}"
        }

        private fun implementFunction(def: KSFunctionDeclaration): FunSpec {
            if(def.returnType?.resolve().toString() != "String") {
                throw RuntimeException("${functionErrorReference(def)} must return a String.")
            }

            val funName = def.simpleName.getShortName()

            val template = (def.annotations.firstOrNull()?.arguments?.firstOrNull()?.value as? String)
                ?: throw java.lang.RuntimeException("Can't read template string: ${functionErrorReference(def)}")

            val paramNames = def.parameters.map {
                it.name?.getShortName()
                    ?: throw IllegalArgumentException("Parameters must be named. ${functionErrorReference(def)}")
            }

            val placeholders = TokenUtil.getTokensFromTemplate(template, placeholderStartToken, placeholderEndToken)

            val paramSet = paramNames.toSet()
            val placeholderSet = placeholders.toSet()

            assert(paramSet == placeholderSet) {
                "Parameters should match placeholders.\n" +
                        "Parameters: $paramNames, placeholders: $placeholders\n" +
                        "(${functionErrorReference(def)})"
            }

            // rebuild template string to internal format
            val internalToken = KuriInternals.TOKEN.replace("%", "%%") //Poet no like %
            var templateInternal = template
            paramNames.forEach {
                templateInternal = templateInternal.replace(
                    "${placeholderStartToken}$it${placeholderEndToken}",
                    "$internalToken$it$internalToken"
                )
            }

            val impl = FunSpec.builder(funName)
                .addModifiers(KModifier.OVERRIDE)
                .returns(String::class)

            def.parameters.forEach {
                val paramName = it.name!!.getShortName()
                val paramType = it.type.resolve()

                impl.addParameter(paramName, paramType.toTypeName())
            }

            val statement = buildString {
                append("return %L.build(template = \"")
                append(templateInternal.replace("$", "\$"))
                append("\", ")
                append(paramNames.joinToString(", ") { s -> "\"$s\" to $s" })
                append(")")
            }

            impl.addStatement(statement, KuriMemberName)

            return impl.build()
        }
    }
}