plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
}

repositories {
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    implementation("io.github.openfeign:feign-core:10.9")

    implementation("org.springframework:spring-context:5.3.2")
    implementation("org.springframework:spring-web:5.3.2")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.5")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.5")
    implementation("com.beust:klaxon:5.5")
}

gradlePlugin {
    plugins {
        create("verify-feign") {
            id = "com.github.zafkiel1312.verify-feign"
            implementationClass = "com.github.zafkiel1312.verifyfeign.VerifyFeignPlugin"
        }
    }
}

// Add a source set for the functional test suite
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
}
