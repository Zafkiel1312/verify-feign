package io.github.zafkiel1312.exampleProject.config

import io.github.zafkiel1312.exampleProject.client.HelloWorldClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ClientConfiguration {
    @Bean
    internal fun helloWorldClient(
        @Value("http://localhost:8080/test") url: String,
        @Value("300000") readTimeout: Long,
        @Value("6000") connectTimeout: Long,
    ): HelloWorldClient = createHelloWorldClient(url, readTimeout, connectTimeout)
}