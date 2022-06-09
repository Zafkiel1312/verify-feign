package io.github.zafkiel1312.exampleProject.client

import feign.Param
import feign.RequestLine
import io.github.zafkiel1312.verifyfeign.annotations.VerifyFeign

@VerifyFeign(":server")
interface HelloWorldClient {

    @RequestLine("GET /")
    fun helloWorld(): StringView

    @RequestLine("GET /{num}")
    fun helloWorldFromUrl(@Param("num") num: Int): String

    @RequestLine("GET /{num}")
    fun helloWorldFromUrl2(@Param("num") num: Int): String

    @RequestLine("POST /")
    fun helloWorldNumber(num: Int): String

    @RequestLine("DELETE /delete")
    fun deleteTheWorld()
}
