package io.github.zafkiel1312.verifyfeign.annotationProcessor

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping

internal enum class HttpMethod(val annotationClass: Class<out Annotation>) {
    PUT(PutMapping::class.java),
    GET(GetMapping::class.java),
    POST(PostMapping::class.java),
    DELETE(DeleteMapping::class.java)
}