package io.github.zafkiel1312.verifyfeign.annotationProcessor

import com.beust.klaxon.Klaxon
import feign.Param
import feign.RequestLine
import io.github.zafkiel1312.verifyfeign.annotations.VerifyFeign
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

internal data class FeignClient(
    val name: String,
    val targetModule: String,
    val methods: MutableMap<String, RestControllerView.RestMethodView> = mutableMapOf()
)

@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes
@SupportedOptions(ControllerAnnotationProcessor.OUTPUT_DIR)
class FeignAnnotationProcessor : AbstractProcessor() {

    companion object {
        const val OUTPUT_DIR = "feignannotationprocessor.outputdir"
    }

    private var outputDir: File? = null
    private val clients = mutableMapOf<String, FeignClient>()

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
            RequestLine::class.java.canonicalName, VerifyFeign::class.java.canonicalName
        )

    override fun getSupportedAnnotationTypes(): Set<String> = supportedTypes

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        processClients(roundEnv)
        processMethods(roundEnv)

        if (outputDir == null) {
            outputDir = File(outputDir())
            outputDir?.mkdirs()
        }
        if (roundEnv.processingOver()) {
            Klaxon().toJsonString(clients)
                .let {
                    File(outputDir!!.absolutePath + "/clients.json").writeText(it)
                }
        }
        return false
    }

    private fun processClients(roundEnv: RoundEnvironment) {
        val elements = roundEnv.getElementsAnnotatedWithAny(setOf(VerifyFeign::class.java)) ?: emptyList()
        // ToDo Line 75 throws NullpointerException, when no @VerifyFeign Annotations are used... But Why?
        elements.filter {
            it.kind == ElementKind.INTERFACE
        }.forEach {
            clients[it.simpleName.toString()] =
                FeignClient(it.simpleName.toString(), it.getAnnotation(VerifyFeign::class.java).targetModule)
        }
    }

    private fun processMethods(roundEnv: RoundEnvironment) {
        val elements = roundEnv.getElementsAnnotatedWithAny(setOf(RequestLine::class.java))
        elements.filter {
            it.kind == ElementKind.METHOD
        }.forEach {
            registerMethod(it)
        }
    }

    private fun registerMethod(element: Element) {
        val executableElement = element as ExecutableElement
        val requestLine = element.getAnnotation(RequestLine::class.java).value
        val verb = HttpMethod.valueOf(requestLine.split(" ").first())
        val path = requestLine.split(" ").last()
        val parameters = executableElement.parameters.map {
            val isBodyParam = it.getAnnotation(Param::class.java) == null
            Parameter(
                it.simpleName.toString(), it.asType().toString(),
                if (isBodyParam) {
                    ParameterType.BODY
                } else {
                    ParameterType.PATH
                }
            )
        }
        RestControllerView.RestMethodView(
            element.simpleName.toString(),
            element.enclosingElement.simpleName.toString() + "." + element.simpleName.toString(), verb,
            path,
            executableElement.returnType.toString(),
            parameters.toMutableSet()
        ).let {
            val client = clients[element.enclosingElement.simpleName.toString()] ?: return
            client.methods[it.name] = it
        }
    }
}
