package io.github.zafkiel1312.verifyfeign.annotationProcessor

import com.beust.klaxon.Klaxon
import io.github.zafkiel1312.verifyfeign.annotations.FrontendEndpoint
import io.github.zafkiel1312.verifyfeign.annotations.PublicEndpoint
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

internal data class Parameter(val name: String, val type: String, val parameterType: ParameterType)
internal data class RestControllerView(
    val basePath: String,
    val name: String,
    val members: MutableMap<String, RestMethodView> = mutableMapOf()
) {
    data class RestMethodView(
        val methodName: String,
        val name: String,
        val type: HttpMethod,
        val path: String,
        val returnType: String,
        val parameters: MutableSet<Parameter> = mutableSetOf(),
        val feignClients: MutableList<String> = mutableListOf(),
        val publicEndpoint: String? = null,
        val frontendEndpoint: String? = null
    )
}

internal enum class ParameterType {
    PATH, QUERY, BODY //ToDo add optional
}


@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes
@SupportedOptions(ControllerAnnotationProcessor.OUTPUT_DIR)
class ControllerAnnotationProcessor : AbstractProcessor() {

    companion object {
        const val OUTPUT_DIR = "controllerannotationprocessor.outputdir"
    }

    private var outputDir: File? = null
    private val controllers = mutableMapOf<String, RestControllerView>()

    override fun getSupportedOptions() = setOf(OUTPUT_DIR)

    private fun outputDir(): String {
        processingEnv.options[OUTPUT_DIR]?.let {
            return it
        }
        processingEnv.messager.printMessage(
            Diagnostic.Kind.ERROR, "Output directory: $OUTPUT_DIR not set"
        )
        error("Output directory: $OUTPUT_DIR not set")
    }

    private val supportedTypes =
        setOf<String>(
            RestController::class.java.canonicalName, RequestMapping::class.java.canonicalName,
            GetMapping::class.java.canonicalName, PostMapping::class.java.canonicalName,
            DeleteMapping::class.java.canonicalName,
            PutMapping::class.java.canonicalName, PathVariable::class.java.canonicalName,
            RequestBody::class.java.canonicalName, PublicEndpoint::class.java.canonicalName,
            FrontendEndpoint::class.java.canonicalName
        )

    override fun getSupportedAnnotationTypes(): Set<String> = supportedTypes

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        parseControllers(roundEnv)
        enumValues<HttpMethod>().forEach {
            parseMapping(roundEnv, it.annotationClass, it) { element ->
                getPathOrSlash(
                    element, it.annotationClass
                )
            }
        }
        parseParameters(roundEnv, PathVariable::class.java, ParameterType.PATH)
        parseParameters(roundEnv, RequestParam::class.java, ParameterType.QUERY)

        parseParameters(roundEnv, RequestBody::class.java, ParameterType.BODY)

        if (outputDir == null) {
            outputDir = File(outputDir())
            outputDir?.mkdirs()
        }
        if (roundEnv.processingOver()) {
            controllers.values.flatMap { it.members.values }.associateBy { it.name }.let {
                Klaxon().toJsonString(it)
            }.let {
                File(outputDir!!.absolutePath + "/controllers.json").writeText(it)
            }
        }

        return false
    }

    private fun parseControllers(roundEnv: RoundEnvironment) {
        val elements = roundEnv.getElementsAnnotatedWithAny(
            setOf(RestController::class.java)
        )
        elements.forEach {
            if (it.kind == ElementKind.CLASS) {
                controllers[it.simpleName.toString()] =
                    RestControllerView(
                        it.getAnnotation(RequestMapping::class.java)?.value?.joinToString("") ?: "",
                        it.simpleName.toString()
                    )
            }
        }
    }

    private fun <T : Annotation> parseMapping(
        roundEnv: RoundEnvironment,
        annotation: Class<T>,
        method: HttpMethod,
        valueProvider: (Element) -> String
    ) {
        val elements = roundEnv.getElementsAnnotatedWithAny(
            setOf(annotation)
        )
        val publicElements = roundEnv.getElementsAnnotatedWithAny(
            setOf(PublicEndpoint::class.java)
        ).associate { it.simpleName.toString() to it.getAnnotation(PublicEndpoint::class.java).description }
        val frontendElements = roundEnv.getElementsAnnotatedWithAny(
            setOf(FrontendEndpoint::class.java)
        ).associate {
            it.simpleName.toString() to it.getAnnotation(
                FrontendEndpoint::class.java
            ).frontendModule + ": " + it.getAnnotation(FrontendEndpoint::class.java).description
        }
        elements.forEach {
            if (it.kind == ElementKind.METHOD) {
                val symbol = it as ExecutableElement
                val controller = controllers[it.enclosingElement.simpleName.toString()] ?: return
                controller.members[it.simpleName.toString()] = RestControllerView.RestMethodView(
                    it.simpleName.toString(),
                    controller.name + "." + it.simpleName.toString(), method,
                    controller.basePath + valueProvider(it), symbol.returnType.toString(),
                    publicEndpoint = publicElements[it.simpleName.toString()],
                    frontendEndpoint = frontendElements[it.simpleName.toString()],
                )
            }
        }
    }

    private fun getPathOrSlash(element: Element, annotationType: Class<out Annotation>): String {
        when (annotationType) {
            GetMapping::class.java -> element.getAnnotation(annotationType).value.joinToString("")
            PostMapping::class.java -> element.getAnnotation(annotationType).value.joinToString("")
            PutMapping::class.java -> element.getAnnotation(annotationType).value.joinToString("")
            DeleteMapping::class.java -> element.getAnnotation(annotationType).value.joinToString("")
            else -> error("Unknown type of annotation. This should not happen!")
        }.let {
            if (it == "")
                return "/"
            return it
        }
    }

    private fun parseParameters(
        roundEnv: RoundEnvironment,
        annotation: Class<out Annotation>,
        parameterType: ParameterType
    ) {
        val elements = roundEnv.getElementsAnnotatedWithAny(
            setOf(annotation)
        )
        elements.filter {
            it.kind == ElementKind.PARAMETER
        }.map {
            val controller = controllers[it.enclosingElement.enclosingElement.simpleName.toString()] ?: return
            it to controller.members[it.enclosingElement.simpleName.toString()]
        }.forEach { (element, method) ->
            method?.parameters?.add(
                Parameter(
                    element.simpleName.toString(), element.asType().toString(), parameterType
                )
            )
        }
    }
}
