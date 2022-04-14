plugins {
    id("org.springframework.boot") version "2.6.6"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"

    id("io.github.zafkiel1312.verifyfeign") version "0.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.6")

    implementation("io.github.openfeign:feign-core:10.9")
    implementation("io.github.openfeign:feign-jackson:10.9")

    implementation("io.github.zafkiel1312.verifyfeign:verifyfeign")
}

group = "io.github.zafkiel1312.exampleProject"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11