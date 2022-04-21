# Verify-Feign Gradle Plugin
This plugin will help you to verify all rest clients and controllers 
in a multimodule spring project. It will check, if all feign clients have matching 
controllers with fitting parameters and if all rest interfaces are used by a 
client, are public endpoints or frontend endpoints.

This plugin currently only supports OpenFeign clients and Spring-Boot controllers

## Quickstart

Add the plugin to your gradle build file.

`id("io.github.zafkiel1312.verifyfeign")`

Add the dependency to your gradle build file.

`implementation("io.github.zafkiel1312.verifyfeign:verifyfeign")`

The latest Version of the plugin can be found on the [gradle plugin portal](https://plugins.gradle.org/plugin/io.github.zafkiel1312.verifyfeign)

## Usage

```
@RestController
@RequestMapping("/helloWorld")
class HelloWorldController {
    @GetMapping("/")
    fun helloWorld(): String {
    return "Hello World!"
    }

    @PublicEndpoint("Returns Hello World!")
    @GetMapping("/public")
    fun helloWorldPublic(): String {
        return "Hello World!"
    }

    @FrontendEndpoint(":frontendModule", "Returns Hello World!")
    @GetMapping("/frontend")
    fun helloWorldFrontend(): String {
        return "Hello World!"
    }
}

@VerifyFeign(":targetModule")
interface HelloWorldClient {
    @RequestLine("GET /")
    fun helloWorld(): String
}
```

``@VerifyFeign(targetModule: String)``
- marks the feign client to be verified
- needs to be in front of the interface
- targetModule: points to the module, which implements the corresponding rest controller

``@PublicEndpoint(description: String)``
- marks the rest endpoint (e.g. `GetMapping`) as a public endpoint
- needs to be in front of a method, inside a class annotated with `@RestController`
- description: describes what the rest endpoint does

``@FrontendEndpoint(frontendModule: String, description: String)``
- marks the rest endpoint (e.g. `GetMapping`) as a frontend endpoint
- needs to be in front of a method, inside a class annotated with `@RestController`
- frontendModule: points to the frontend module, where the rest endpoint is used
- description: describes what the rest endpoint does

Every method inside a rest controller needs to have at least one corresponding feign 
client or needs to be marked as public or frontend endpoint

## Gradle Tasks

``verifyFeign`` 
- checks if there are suitable spring boot controllers for feign clients annotated 
with `@VerifyFeign.`

``verifyController`` 
- checks if rest controllers are used by clients or are declared 
as public or frontend endpoints.

``verifyApi`` 
- checks if rest controllers are used by clients and clients have suitable 
rest-interfaces. It does this by calling ``verifyFeign`` and ``verifyController``.

All tasks can be found in the group `check`.

These gradle Tasks produce output to the following directories:

`./build/verifyfeign/client/`

`./build/verifyfeign/controller/`

`./build/verifyfeign/controller_result/`

# Demonstration

A demonstration for this plugin can be found inside exampleProject. This project 
contains a client and a server. It shows different combinations of client and 
server interfaces. If you call any of the 3 Gradle tasks, they should run without
any problem. 

You can experiment with the annotations and see how the verification succeeds or fails.
