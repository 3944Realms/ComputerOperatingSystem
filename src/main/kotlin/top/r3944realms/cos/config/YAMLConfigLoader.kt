package top.r3944realms.cos.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

object YAMLConfigLoader {
    private val mapper = ObjectMapper(YAMLFactory()).apply {
        registerKotlinModule()
    }

    fun <T> loadConfig(filePath: String, clazz: Class<T>): T {
        return mapper.readValue(File(filePath), clazz)
    }

    fun <T> loadConfigFromResource(resourcePath: String, clazz: Class<T>): T {
        val resource = this::class.java.classLoader.getResource(resourcePath)
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")
        return mapper.readValue(resource, clazz)
    }
}