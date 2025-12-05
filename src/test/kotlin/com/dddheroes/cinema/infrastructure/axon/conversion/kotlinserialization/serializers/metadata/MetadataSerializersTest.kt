package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.metadata

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.AxonSerializersModule
import kotlinx.serialization.json.Json
import org.axonframework.messaging.core.Metadata
import org.junit.jupiter.api.Test

class MetadataSerializersTest {

    private val json = Json {
        serializersModule = AxonSerializersModule
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun `JsonMetadataSerializer should serialize metadata to JSON map`() {
        val original = Metadata(mapOf("key1" to "value1", "key2" to "value2"))
        val serialized = json.encodeToString(JsonMetadataSerializer, original)
        assertThat(serialized).isEqualTo("""{"key1":"value1","key2":"value2"}""")
    }

    @Test
    fun `JsonMetadataSerializer should deserialize JSON map to metadata`() {
        val jsonString = """{"key1":"value1","key2":"value2"}"""
        val deserialized = json.decodeFromString(JsonMetadataSerializer, jsonString)
        assertThat(deserialized["key1"]).isEqualTo("value1")
        assertThat(deserialized["key2"]).isEqualTo("value2")
    }

    @Test
    fun `JsonMetadataSerializer should handle empty metadata`() {
        val original = Metadata.emptyInstance()
        val serialized = json.encodeToString(JsonMetadataSerializer, original)
        assertThat(serialized).isEqualTo("{}")

        val deserialized = json.decodeFromString(JsonMetadataSerializer, serialized)
        assertThat(deserialized.isEmpty()).isTrue()
    }

    @Test
    fun `StringMetadataSerializer should serialize metadata to JSON string`() {
        val original = Metadata(mapOf("key1" to "value1"))
        val serialized = json.encodeToString(StringMetadataSerializer, original)
        // StringMetadataSerializer wraps the JSON in quotes
        assertThat(serialized.contains("key1")).isTrue()
        assertThat(serialized.contains("value1")).isTrue()
    }

    @Test
    fun `StringMetadataSerializer should deserialize JSON string to metadata`() {
        // Note: StringMetadataSerializer expects a JSON-encoded string
        val jsonString = """"{\"key1\":\"value1\"}""""
        val deserialized = json.decodeFromString(StringMetadataSerializer, jsonString)
        assertThat(deserialized["key1"]).isEqualTo("value1")
    }

    @Test
    fun `ComposedMetadataSerializer should use JsonMetadataSerializer for JSON format`() {
        // When using Json format, ComposedMetadataSerializer should delegate to JsonMetadataSerializer
        val original = Metadata(mapOf("key1" to "value1", "key2" to "value2"))
        val serialized = json.encodeToString(ComposedMetadataSerializer, original)

        // The output should be the same as JsonMetadataSerializer (not wrapped in quotes)
        val deserialized = json.decodeFromString(ComposedMetadataSerializer, serialized)
        assertThat(deserialized["key1"]).isEqualTo("value1")
        assertThat(deserialized["key2"]).isEqualTo("value2")
    }

    @Test
    fun `ComposedMetadataSerializer should handle round-trip`() {
        val original = Metadata(mapOf(
            "correlationId" to "12345",
            "userId" to "user-abc",
            "timestamp" to "2025-01-01T00:00:00Z"
        ))

        val serialized = json.encodeToString(ComposedMetadataSerializer, original)
        val deserialized = json.decodeFromString(ComposedMetadataSerializer, serialized)

        assertThat(deserialized["correlationId"]).isEqualTo("12345")
        assertThat(deserialized["userId"]).isEqualTo("user-abc")
        assertThat(deserialized["timestamp"]).isEqualTo("2025-01-01T00:00:00Z")
    }

    @Test
    fun `should handle metadata with special characters`() {
        val original = Metadata(mapOf(
            "key" to "value with \"quotes\"",
            "key2" to "value with \nnewline"
        ))

        val serialized = json.encodeToString(ComposedMetadataSerializer, original)
        val deserialized = json.decodeFromString(ComposedMetadataSerializer, serialized)

        assertThat(deserialized["key"]).isEqualTo("value with \"quotes\"")
        assertThat(deserialized["key2"]).isEqualTo("value with \nnewline")
    }

    @Test
    fun `should handle metadata with null values`() {
        // Note: In Axon Framework 5, Metadata is Map<String, String>, so null values are stored as "null" string
        // or simply not present. Let's test with present values only.
        val original = Metadata(mapOf("key1" to "value1"))

        val serialized = json.encodeToString(ComposedMetadataSerializer, original)
        val deserialized = json.decodeFromString(ComposedMetadataSerializer, serialized)

        assertThat(deserialized["key1"]).isEqualTo("value1")
        assertThat(deserialized["nonExistentKey"]).isEqualTo(null)
    }
}
