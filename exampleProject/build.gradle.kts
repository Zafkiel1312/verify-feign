import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.6"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.spring") version "1.8.21"
}

repositories {
    mavenCentral()
}

group = "io.github.zafkiel1312.exampleProject"
version = "0.0.1-SNAPSHOT"

tasks.create("verifyApi") {
    this.dependsOn("server:verifyApi", "client:verifyApi")
    this.description =
        "Calls verifyApi in the server- and client-submodule"
    this.group = "check"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

springBoot {
    mainClass.set("server.io.github.zafkiel1312.exampleProject.ExampleProjectServer")
}