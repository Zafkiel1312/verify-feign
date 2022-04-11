package io.github.zafkiel1312.exampleProject.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import feign.Feign
import feign.Request
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import io.github.zafkiel1312.exampleProject.client.HelloWorldClient
import java.util.concurrent.TimeUnit

fun createHelloWorldClient(
    url: String,
    readTimeout: Long,
    connectTimeout: Long,
): HelloWorldClient =
    Feign.builder().run {
        val objectMapper = objectMapper()
        encoder(JacksonEncoder(objectMapper))
        decoder(JacksonDecoder(objectMapper))
        decode404()
        options(Request.Options(connectTimeout, TimeUnit.MILLISECONDS, readTimeout, TimeUnit.MILLISECONDS, true))
        target(HelloWorldClient::class.java, url)
    }

internal fun objectMapper() =
    ObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
    }