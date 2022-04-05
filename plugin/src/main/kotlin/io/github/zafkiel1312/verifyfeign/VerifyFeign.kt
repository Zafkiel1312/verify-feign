package io.github.zafkiel1312.verifyfeign

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class VerifyFeign(val targetModule: String)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class PublicEndpoint(val description: String)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class FrontendEndpoint(val frontendModule: String, val description: String)