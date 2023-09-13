import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.18.0"

    id("org.jetbrains.kotlin.jvm") version "1.8.21"
}

val VERIFY_FEIGN_VERSION = "0.5"

group = "io.github.zafkiel1312.verifyfeign"
version = VERIFY_FEIGN_VERSION

gradlePlugin {
    plugins {
        create("verifyfeign") {
            id = "io.github.zafkiel1312.verifyfeign"
            implementationClass = "io.github.zafkiel1312.verifyfeign.VerifyFeignPlugin"
        }
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21")

    implementation("org.springframework:spring-context:5.3.2")
    implementation("org.springframework:spring-web:5.3.2")

    implementation("io.github.openfeign:feign-core:10.9")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.5")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.5")
    implementation("com.beust:klaxon:5.5")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

pluginBundle {
    website = "https://github.com/Zafkiel1312/verify-feign"
    vcsUrl = "https://github.com/Zafkiel1312/verify-feign"
    description =
        "Plugin to check, if RestControllers are used by clients and clients have suitable rest-interfaces"

    (plugins) {
        "verifyfeign" {
            displayName = "Verify-Feign"
            tags = listOf("feign", "spring", "spring-boot")
            version = VERIFY_FEIGN_VERSION
        }
    }

    mavenCoordinates {
        groupId = project.group.toString()
        artifactId = project.name
        version = project.version.toString()
    }
}

publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("../local-plugin-repository")
        }
    }
}

/*// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

gradlePlugin.testSourceSets(functionalTestSourceSet)

tasks.named<Task>("check") {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}*/
