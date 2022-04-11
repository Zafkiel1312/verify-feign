package io.github.zafkiel1312.exampleProject.client

import feign.Headers
import feign.Param
import feign.RequestLine
import io.github.zafkiel1312.verifyfeign.VerifyFeign

@VerifyFeign(":server")
interface HelloWorldClient {

    @Headers("Content-Type: application/json; charset=utf-8")
    @RequestLine("GET /")
    fun helloWorld(): StringView

    @RequestLine("GET /{num}")
    fun helloWorldFromUrl(@Param("num") num: Int): String

    @RequestLine("POST /")
    fun helloWorldNumber(num: Int): String

    @RequestLine("DELETE /delete")
    fun deleteTheWorld()
}
