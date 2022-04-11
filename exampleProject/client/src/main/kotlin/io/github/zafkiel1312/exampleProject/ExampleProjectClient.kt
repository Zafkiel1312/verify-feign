package io.github.zafkiel1312.exampleProject

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ExampleProjectClient

fun main(args: Array<String>) {
	runApplication<ExampleProjectClient>(*args)
}
