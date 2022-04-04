package com.github.zafkiel1312.verifyfeign

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class VerifyControllerTask : DefaultTask() {
    init {
        inputs.dir(project.buildDir.absolutePath + "/verifyfeign/controller_result")
        inputs.dir(project.buildDir.absolutePath + "/verifyfeign/controller")
    }

    private val objectMapper = ObjectMapper().apply {
        registerKotlinModule()
    }

    @TaskAction
    fun verify() {
        val controllerFile = File(project.buildDir.absolutePath + "/verifyfeign/controller/controllers.json")
        val controllerResultFile =
            File(project.buildDir.absolutePath + "/verifyfeign/controller_result/controllers.json")
        val file = if (controllerResultFile.exists()) {
            controllerResultFile
        } else if (controllerFile.exists()) {
            controllerFile
        } else {
            return
        }

        val typeRef: TypeReference<Map<String, RestControllerView.RestMethodView>> =
            object : TypeReference<Map<String, RestControllerView.RestMethodView>>() {}

        val controller = objectMapper.readValue(file, typeRef)

        controller.values.forEach {
            if (!isUsed(it)) {
                error(
                    "RestMethod: ${project.path}: ${it.name} is not used by any feign client," +
                        " not is a public endpoint or used by a frontend"
                )
            }
        }
    }

    private fun isUsed(controllerMethod: RestControllerView.RestMethodView) =
        controllerMethod.feignClients.isNotEmpty() ||
            controllerMethod.publicEndpoint != null ||
            controllerMethod.frontendEndpoint != null
}
