pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven(url = "./local-plugin-repository")
    }
}

rootProject.name = "verify-feign"
include("plugin", "test")
