package io.github.zafkiel1312.exampleProject.api

import io.github.zafkiel1312.exampleProject.client.HelloWorldClient
import io.github.zafkiel1312.exampleProject.client.StringView
import io.github.zafkiel1312.verifyfeign.PublicEndpoint
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test")
class HelloWorldController(
    val helloWorldClient: HelloWorldClient
) {
    @GetMapping("/server")
    fun helloWorld(): StringView {
        return StringView("Hello world!")
    }

    @PublicEndpoint("calls helloWorld over the Client")
    @GetMapping("/client")
    fun helloWorldOverClient(): String {
        val hello: StringView = helloWorldClient.helloWorld()
        return  "${hello.string} Sent from Client!"
    }
}