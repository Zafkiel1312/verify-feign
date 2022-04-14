plugins {
    id("org.springframework.boot") version "2.6.6"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
}

repositories {
    mavenCentral()
}

group = "io.github.zafkiel1312.exampleProject"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

tasks.create("verifyApi") {
    this.dependsOn("server:verifyApi", "client:verifyApi")
    this.description =
        "Calls verifyApi in the server- and client-submodule"
    this.group = "check"
}

springBoot {
    mainClass.set("server.io.github.zafkiel1312.exampleProject.ExampleProjectServer")
}