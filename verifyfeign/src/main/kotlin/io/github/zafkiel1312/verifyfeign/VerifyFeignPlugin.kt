package io.github.zafkiel1312.verifyfeign

import io.github.zafkiel1312.verifyfeign.annotationProcessor.ControllerAnnotationProcessor
import io.github.zafkiel1312.verifyfeign.annotationProcessor.FeignAnnotationProcessor
import io.github.zafkiel1312.verifyfeign.tasks.VerifyControllerTask
import io.github.zafkiel1312.verifyfeign.tasks.VerifyFeignTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import java.io.File

/**
 *  Plugin to check, if RestControllers are used by clients and clients have suitable rest-interfaces
 */
class VerifyFeignPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        addKapt(project)
        project.subprojects {
            addKapt(it)
        }
        project.tasks.register("verifyFeign", VerifyFeignTask::class.java) {
            it.dependsOn("compileKotlin")
            project.rootProject.getTasksByName("compileKotlin", true).forEach { task ->
                it.dependsOn(task)
            }
            it.description =
                "Checks if there are suitable spring restcontrollers for feignclient interfaces annotated with @VerifyFeign"
            it.group = "check"
        }
        project.afterEvaluate {
            project.tasks.register("verifyController", VerifyControllerTask::class.java) {
                val verifyFeignTask = project.rootProject.getTasksByName("verifyFeign", true)
                it.dependsOn(verifyFeignTask)
                it.description =
                    "Checks if RestControllers are used by clients"
                it.group = "check"
            }
            project.tasks.register("verifyApi") {
                it.dependsOn("verifyFeign", "verifyController")
                it.description =
                    "Checks if RestControllers are used by clients and clients have suitable rest-interfaces"
                it.group = "check"
            }
        }
    }

    private fun addKapt(project: Project) {
        File(project.buildDir.absolutePath + "/verifyfeign/controller").mkdirs()
        File(project.buildDir.absolutePath + "/verifyfeign/client").mkdirs()

        project.pluginManager.apply("org.jetbrains.kotlin.jvm")
        project.pluginManager.apply("org.jetbrains.kotlin.kapt")
        project.configurations.getByName("kapt").dependencies.add(
            project.dependencies.create("io.github.zafkiel1312.verifyfeign:verifyfeign")
        )

        project.pluginManager.withPlugin("org.jetbrains.kotlin.kapt") {
            project.afterEvaluate {
                (it.tasks.findByName("kaptKotlin") ?: error("kapt task should be defined here")).let {
                    it.inputs.dir(project.projectDir.absolutePath + "/src/main")
                    it.outputs.dir(project.buildDir.absolutePath + "/verifyfeign/controller")
                    it.outputs.dir(project.buildDir.absolutePath + "/verifyfeign/client")
                }
            }
        }

        project.extensions.configure<KaptExtension>("kapt") {
            it.useBuildCache = true
            it.arguments {
                arg(ControllerAnnotationProcessor.OUTPUT_DIR, project.buildDir.absolutePath + "/verifyfeign/controller")
                arg(FeignAnnotationProcessor.OUTPUT_DIR, project.buildDir.absolutePath + "/verifyfeign/client")
            }
        }
    }
}