package de.otto.salesproduct.build.verifyfeign

import com.beust.klaxon.Klaxon
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.zafkiel1312.verifyfeign.Parameter
import com.github.zafkiel1312.verifyfeign.RestControllerView
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

        clients.forEach {
            val controller = allController[it.value.targetModule] ?: loadController(it.value.targetModule) ?: error(
                "Project: ${it.value.targetModule} has no RestController for Client: ${it.key}"
            )
            it.value.methods.forEach { (_, method) ->
                findMethod(method, controller.values.toSet())
            }
            allController[it.value.targetModule] = controller
            safeController(it.value.targetModule, controller)
        }
    }

    private fun findMethod(
        feignMethod: RestControllerView.RestMethodView,
        allMethods: Set<RestControllerView.RestMethodView>
    ) {
        val matchingMethod = allMethods.firstOrNull {
            it.path.endsWith(feignMethod.path) && it.type == feignMethod.type
        }
        when {
            matchingMethod == null ->
                error(
                    "No Suitable controller method could be found for ${feignMethod.name}: ${feignMethod.type} ${feignMethod.path}"
                )
            matchingMethod.methodName != feignMethod.methodName ->
                error(
                    "Controller method: ${matchingMethod.name} and feignMethod: ${feignMethod.name} match but don't have the same name: Controller: ${matchingMethod.methodName} vs Feign: ${feignMethod.methodName}"
                )
            matchingMethod.returnType != feignMethod.returnType ->
                error(
                    "Controller method: ${matchingMethod.name} and feignMethod: ${feignMethod.name} match but don't have the same returnType: Controller: ${matchingMethod.returnType} vs Feign: ${feignMethod.returnType}"
                )
            matchingMethod.parameters != feignMethod.parameters ->
                error(
                    "Controller method: ${matchingMethod.name} and feignMethod: ${feignMethod.name} match but don't have the same parameters:\n${
                    parameterDiff(
                        matchingMethod.parameters, feignMethod.parameters
                    )
                    }"
                )
            else -> {
                matchingMethod.feignClients.add(feignMethod.name)
            }
        }
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
            val json=Klaxon().toJsonString(controller)
            File(it.buildDir.absolutePath + "/verifyfeign/controller_result/controllers.json").writeText(json)
        }
}
