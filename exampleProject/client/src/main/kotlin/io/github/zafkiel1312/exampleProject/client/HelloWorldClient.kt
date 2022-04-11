package io.github.zafkiel1312.exampleProject.client

import feign.Headers
import feign.RequestLine
import io.github.zafkiel1312.verifyfeign.VerifyFeign

@VerifyFeign(":server")
interface HelloWorldClient {

    @Headers("Content-Type: application/json; charset=utf-8")
    @RequestLine("GET /server")
    fun helloWorld(): StringView

    // @Headers("Content-Type: application/json; charset=utf-8")
    // @RequestLine("GET /fail")
    // fun failingRequest(): StringView
}
