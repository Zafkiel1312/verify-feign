package io.github.zafkiel1312.exampleProject.api

import io.github.zafkiel1312.exampleProject.client.StringView
import io.github.zafkiel1312.verifyfeign.FrontendEndpoint
import io.github.zafkiel1312.verifyfeign.PublicEndpoint
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test")
class HelloWorldController() {
    @GetMapping("/")
    fun helloWorld(): StringView {
        return StringView("Hello world!")
    }

    @PublicEndpoint("Returns Hello World!")
    @GetMapping("/public")
    fun helloWorldPublic(): String {
        return "Hello World!"
    }

    @FrontendEndpoint(":placeholder", "Returns Hello World!")
    @GetMapping("/frontend")
    fun helloWorldFrontend(): String {
        return  "Hello World!"
    }

    @GetMapping("/{num}")
    fun helloWorldFromUrl(@PathVariable num: Int): String {
        return "Hello world number ${num}!"
    }

    @PostMapping("/")
    fun helloWorldNumber(@RequestBody num: Int): String {
        return "Hello world number ${num}!"
    }

    @DeleteMapping("/delete")
    fun deleteTheWorld() {
        return
    }



}