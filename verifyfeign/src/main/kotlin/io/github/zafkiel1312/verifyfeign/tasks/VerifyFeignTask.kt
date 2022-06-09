package io.github.zafkiel1312.verifyfeign.tasks

import com.beust.klaxon.Klaxon
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.zafkiel1312.verifyfeign.annotationProcessor.FeignClient
import io.github.zafkiel1312.verifyfeign.annotationProcessor.Parameter
import io.github.zafkiel1312.verifyfeign.annotationProcessor.RestControllerView
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class VerifyFeignTask : DefaultTask() {
    init {
        project.rootProject.allprojects.forEach {
            File(it.buildDir.absolutePath + "/verifyfeign/controller").mkdirs()
            inputs.dir(it.buildDir.absolutePath + "/verifyfeign/controller")
            File(it.buildDir.absolutePath + "/verifyfeign/controller_result").mkdirs()
            outputs.dir(it.buildDir.absolutePath + "/verifyfeign/controller_result")
        }
        inputs.dir(project.buildDir.absolutePath + "/verifyfeign/client")
    }

    private val objectMapper = ObjectMapper().apply {
        registerKotlinModule()
    }

    @TaskAction
    fun verify() {
        val clientFile = File(project.buildDir.absolutePath + "/verifyfeign/client/clients.json")
        if (!clientFile.exists()) {
            return
        }

        val typeRef: TypeReference<Map<String, FeignClient>> =
            object : TypeReference<Map<String, FeignClient>>() {}
        val clients = objectMapper.readValue(clientFile, typeRef)
        val allController = mutableMapOf<String, Map<String, RestControllerView.RestMethodView>>()

        clients.forEach { (key, value) ->
            val controller = allController[value.targetModule] ?: loadController(value.targetModule) ?: error(
                "Project: ${value.targetModule} has no RestController for Client: $key"
            )
            value.methods.forEach { (_, method) ->
                findMethod(method, controller.values.toSet())
            }
            allController[value.targetModule] = controller
            safeController(value.targetModule, controller)
        }
    }

    private fun findMethod(
        feignMethod: RestControllerView.RestMethodView,
        allMethods: Set<RestControllerView.RestMethodView>
    ) {
        val possiblyMatchingMethods = allMethods.filter {
            it.path.endsWith(feignMethod.path) && it.type == feignMethod.type
        }
        if (possiblyMatchingMethods.isEmpty()) {
            error(
                "No Suitable controller method could be found for ${feignMethod.name}: ${feignMethod.type} ${feignMethod.path}"
            )
        } else {
            checkForMatchingMethods(feignMethod, possiblyMatchingMethods)
        }
    }

    private fun checkForMatchingMethods(
        feignMethod: RestControllerView.RestMethodView,
        possiblyMatchingMethods: List<RestControllerView.RestMethodView>
    ) {
        val errorMessage: MutableList<String> = mutableListOf()
        possiblyMatchingMethods.forEach {
            when {
                it.methodName != feignMethod.methodName ->
                    errorMessage.add("Controller method: ${it.name} and feignMethod: ${feignMethod.name} match but don't have the same name: Controller: ${it.methodName} vs Feign: ${feignMethod.methodName}")

                it.returnType != feignMethod.returnType ->
                    errorMessage.add("Controller method: ${it.name} and feignMethod: ${feignMethod.name} match but don't have the same returnType: Controller: ${it.returnType} vs Feign: ${feignMethod.returnType}")

                it.parameters != feignMethod.parameters ->
                    errorMessage.add(
                        "Controller method: ${it.name} and feignMethod: ${feignMethod.name} match but don't have the same parameters:\n${
                            parameterDiff(
                                it.parameters, feignMethod.parameters
                            )
                        }")

                else -> {
                    it.feignClients.add(feignMethod.name)
                    return
                }
            }
        }
        error(errorMessage.joinToString("\n"))
    }

    private fun parameterDiff(controllerParam: Set<Parameter>, feignParam: Set<Parameter>): String {
        val diff = mutableListOf<String>()
        controllerParam.minus(feignParam).let {
            if (it.isNotEmpty()) {

                diff.add(
                    "Parameters: " + it.joinToString(
                        ","
                    ) { it.name + ":" + it.type } + " are missing in the controller"
                )
            }
        }
        feignParam.minus(controllerParam).let {
            if (it.isNotEmpty()) {
                diff.add(
                    "Parameters: " + it.joinToString(
                        ","
                    ) { it.name + ":" + it.type } + " are missing in the feignmethod"
                )
            }
        }
        return diff.joinToString("\n")
    }

    private fun loadController(projectPath: String) =
        project.rootProject.findProject(projectPath)?.let {
            val typeRef: TypeReference<Map<String, RestControllerView.RestMethodView>> =
                object : TypeReference<Map<String, RestControllerView.RestMethodView>>() {}
            objectMapper.readValue(
                File(it.buildDir.absolutePath + "/verifyfeign/controller/controllers.json").readText(),
                typeRef
            )
        }

    private fun safeController(projectPath: String, controller: Map<String, RestControllerView.RestMethodView>) =
        project.rootProject.findProject(projectPath)?.let {
            val json = Klaxon().toJsonString(controller)
            File(it.buildDir.absolutePath + "/verifyfeign/controller_result/controllers.json").writeText(json)
        }
}
