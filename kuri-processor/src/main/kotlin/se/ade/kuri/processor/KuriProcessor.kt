package se.ade.kuri.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import se.ade.kuri.*
import se.ade.kuri.processor.exceptions.KuriReturnTypeException
import java.io.OutputStream

private const val kuriPackageName = "se.ade.kuri"
private val kuriProviderName = UriProvider::class.simpleName

private const val DEFAULT_PLACEHOLDER_START_TOKEN = "{"
private const val DEFAULT_PLACEHOLDER_END_TOKEN = "}"

class KuriProcessor(
    private val options: Map<String, String>,
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    val uriTemplateFunctionFactory = UriTemplateFunctionFactory(logger)

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
            val srcFiles = classDeclaration.containingFile?.let { listOf(it) } ?: listOf()

            val poetFile = FileSpec.builder(classPackageName, "Kuri$className")

            val impl = TypeSpec.classBuilder("Kuri$className")
                .addSuperinterface(classDeclaration.toClassName())

            if(classDeclaration.isInternal())
                impl.addModifiers(KModifier.INTERNAL)

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

            val metaClassSimpleName = "Kuri${className}Spec"
            val templatesObjectBuilder = TypeSpec.objectBuilder(metaClassSimpleName)

            memberFunctions.forEach {
                impl.addFunction(implementFunction(it))
                templatesObjectBuilder.addProperty(implementTemplateProperty(it))
            }

            poetFile.addType(impl.build())
            poetFile.addType(templatesObjectBuilder.build())

            //Create extension function to get metadata
            val metaClassName = ClassName(classPackageName, metaClassSimpleName)
            val extFunSpec = PropertySpec.builder("_spec", metaClassName)
                .receiver(classDeclaration.toClassName())
                .getter(FunSpec.builder("get()").addCode("return $metaClassSimpleName").build())

            if(classDeclaration.isInternal())
                extFunSpec.addModifiers(KModifier.INTERNAL)

            poetFile.addProperty(extFunSpec.build())

            poetFile.build().writeTo(codeGenerator, aggregating = false, originatingKSFiles = srcFiles)
        }

        private fun implementTemplateProperty(def: KSFunctionDeclaration): PropertySpec {
            val funName = def.simpleName.getShortName()

            val template = (def.annotations.firstOrNull()?.arguments?.firstOrNull()?.value as? String)
                ?: throw java.lang.RuntimeException("Can't read template string: ${functionErrorReference(def)}")

            val tokensMap = buildString {
                append("mapOf(")

                append(def.parameters.map { param ->
                    "\"${param.name?.getShortName()}\" to ${param.type.resolve().toClassName()}::class"
                }.joinToString(","))

                append(")")
            }

            return PropertySpec.builder(funName, KuriTemplateSpec::class)
                .initializer("KuriTemplateSpec(template = \"$template\", tokens = $tokensMap)")
                .build()
        }

        private fun functionErrorReference(func: KSFunctionDeclaration): String {
            val location = func.location as? FileLocation
            return "function ${func}, declared at ${location?.filePath}:${location?.lineNumber}"
        }

        private fun implementFunction(def: KSFunctionDeclaration): FunSpec {
            if(def.returnType?.resolve().toString() != "String") {
                throw KuriReturnTypeException("${functionErrorReference(def)} must return a String.")
            }

            val funName = def.simpleName.getShortName()

            val template = (def.annotations.firstOrNull()?.arguments?.firstOrNull()?.value as? String)
                ?: throw java.lang.RuntimeException("Can't read template string: ${functionErrorReference(def)}")

            val pathParamNames = def.parameters.filter {
                !it.isAnnotationPresent(Query::class)
            }.map {
                it.name?.getShortName()
                    ?: throw IllegalArgumentException("Parameters must be named. ${functionErrorReference(def)}")
            }

            val placeholders = TokenUtil.getTokensFromTemplate(template, placeholderStartToken, placeholderEndToken)

            val paramSet = pathParamNames.toSet()
            val placeholderSet = placeholders.toSet()

            assert(paramSet == placeholderSet) {
                "Parameters should match placeholders.\n" +
                        "Parameters: $pathParamNames, placeholders: $placeholders\n" +
                        "(${functionErrorReference(def)})"
            }

            // rebuild template string to internal format
            var templateInternal = template
            pathParamNames.forEach {
                templateInternal = templateInternal.replace(
                    "${placeholderStartToken}$it${placeholderEndToken}",
                    "${KuriInternals.BEGIN_TOKEN}$it${KuriInternals.END_TOKEN}"
                )
            }

            return uriTemplateFunctionFactory.create(funName, templateInternal, def.parameters)
        }
    }
}