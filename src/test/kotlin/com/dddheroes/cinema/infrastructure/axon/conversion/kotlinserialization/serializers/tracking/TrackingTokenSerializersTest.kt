package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.tracking

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.AxonSerializersModule
import com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.KotlinSerializationConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.axonframework.messaging.eventhandling.processing.streaming.token.GapAwareTrackingToken
import org.axonframework.messaging.eventhandling.processing.streaming.token.GlobalSequenceTrackingToken
import org.axonframework.messaging.eventhandling.processing.streaming.token.MergedTrackingToken
import org.axonframework.messaging.eventhandling.processing.streaming.token.ReplayToken
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.ConfigToken
import org.junit.jupiter.api.Test

class TrackingTokenSerializersTest {

    private val json = Json {
        serializersModule = AxonSerializersModule
        ignoreUnknownKeys = true
    }

    @Test
    fun `should serialize and deserialize GlobalSequenceTrackingToken`() {
        val original = GlobalSequenceTrackingToken(42L)
        val serialized = json.encodeToString(GlobalSequenceTrackingTokenSerializer, original)
        val deserialized = json.decodeFromString(GlobalSequenceTrackingTokenSerializer, serialized)

        assertThat(deserialized.globalIndex).isEqualTo(original.globalIndex)
    }

    @Test
    fun `should serialize and deserialize GapAwareTrackingToken`() {
        val original = GapAwareTrackingToken.newInstance(100L, setOf(95L, 97L, 99L))
        val serialized = json.encodeToString(GapAwareTrackingTokenSerializer, original)
        val deserialized = json.decodeFromString(GapAwareTrackingTokenSerializer, serialized)

        assertThat(deserialized.index).isEqualTo(original.index)
        assertThat(deserialized.gaps).isEqualTo(original.gaps)
    }

    @Test
    fun `should serialize and deserialize GapAwareTrackingToken with empty gaps`() {
        val original = GapAwareTrackingToken.newInstance(100L, emptySet())
        val serialized = json.encodeToString(GapAwareTrackingTokenSerializer, original)
        val deserialized = json.decodeFromString(GapAwareTrackingTokenSerializer, serialized)

        assertThat(deserialized.index).isEqualTo(original.index)
        assertThat(deserialized.gaps).isEqualTo(emptySet<Long>())
    }

    @Test
    fun `should serialize and deserialize ConfigToken`() {
        val original = ConfigToken(mapOf("key1" to "value1", "key2" to "value2"))
        val serialized = json.encodeToString(ConfigTokenSerializer, original)
        val deserialized = json.decodeFromString(ConfigTokenSerializer, serialized)

        assertThat(deserialized.config).isEqualTo(original.config)
    }

    @Test
    fun `should serialize and deserialize ConfigToken with empty config`() {
        val original = ConfigToken(emptyMap())
        val serialized = json.encodeToString(ConfigTokenSerializer, original)
        val deserialized = json.decodeFromString(ConfigTokenSerializer, serialized)

        assertThat(deserialized.config).isEqualTo(emptyMap<String, String>())
    }

    @Test
    fun `should serialize and deserialize MergedTrackingToken`() {
        val lower = GlobalSequenceTrackingToken(50L)
        val upper = GlobalSequenceTrackingToken(100L)
        val original = MergedTrackingToken(lower, upper)

        val serialized = json.encodeToString(MergedTrackingTokenSerializer, original)
        val deserialized = json.decodeFromString(MergedTrackingTokenSerializer, serialized)

        assertThat((deserialized.lowerSegmentToken() as GlobalSequenceTrackingToken).globalIndex).isEqualTo(50L)
        assertThat((deserialized.upperSegmentToken() as GlobalSequenceTrackingToken).globalIndex).isEqualTo(100L)
    }

    @Test
    fun `should serialize and deserialize nested MergedTrackingToken`() {
        val innerLower = GlobalSequenceTrackingToken(25L)
        val innerUpper = GlobalSequenceTrackingToken(50L)
        val innerMerged = MergedTrackingToken(innerLower, innerUpper)
        val outer = GlobalSequenceTrackingToken(100L)
        val original = MergedTrackingToken(innerMerged, outer)

        val serialized = json.encodeToString(MergedTrackingTokenSerializer, original)
        val deserialized = json.decodeFromString(MergedTrackingTokenSerializer, serialized)

        val deserializedInner = deserialized.lowerSegmentToken() as MergedTrackingToken
        assertThat((deserializedInner.lowerSegmentToken() as GlobalSequenceTrackingToken).globalIndex).isEqualTo(25L)
        assertThat((deserializedInner.upperSegmentToken() as GlobalSequenceTrackingToken).globalIndex).isEqualTo(50L)
        assertThat((deserialized.upperSegmentToken() as GlobalSequenceTrackingToken).globalIndex).isEqualTo(100L)
    }

    @Test
    fun `should serialize and deserialize ReplayToken with null context`() {
        val tokenAtReset = GlobalSequenceTrackingToken(100L)
        val currentToken = GlobalSequenceTrackingToken(50L)
        val original = ReplayToken.createReplayToken(tokenAtReset, currentToken, null) as ReplayToken

        val serialized = json.encodeToString(ReplayTokenSerializer, original)
        val deserialized = json.decodeFromString(ReplayTokenSerializer, serialized)

        assertThat((deserialized.tokenAtReset as GlobalSequenceTrackingToken).globalIndex).isEqualTo(100L)
        assertThat((deserialized.currentToken as GlobalSequenceTrackingToken).globalIndex).isEqualTo(50L)
        assertThat(deserialized.context()).isNull()
    }

    @Test
    fun `should serialize and deserialize ReplayToken with String context`() {
        val tokenAtReset = GlobalSequenceTrackingToken(100L)
        val currentToken = GlobalSequenceTrackingToken(50L)
        val context = "replay-reason"
        val original = ReplayToken.createReplayToken(tokenAtReset, currentToken, context) as ReplayToken

        val serialized = json.encodeToString(ReplayTokenSerializer, original)
        val deserialized = json.decodeFromString(ReplayTokenSerializer, serialized)

        assertThat((deserialized.tokenAtReset as GlobalSequenceTrackingToken).globalIndex).isEqualTo(100L)
        assertThat((deserialized.currentToken as GlobalSequenceTrackingToken).globalIndex).isEqualTo(50L)
        // Context is stored as ByteArray for cross-serializer compatibility
        val contextBytes = deserialized.context() as ByteArray
        assertThat(String(contextBytes, Charsets.UTF_8)).isEqualTo(context)
    }

    @Test
    fun `should serialize ReplayToken with ByteArray context and deserialize as ByteArray`() {
        val tokenAtReset = GlobalSequenceTrackingToken(100L)
        val currentToken = GlobalSequenceTrackingToken(50L)
        val context = "replay-reason".toByteArray(Charsets.UTF_8)
        val original = ReplayToken.createReplayToken(tokenAtReset, currentToken, context) as ReplayToken

        val serialized = json.encodeToString(ReplayTokenSerializer, original)
        val deserialized = json.decodeFromString(ReplayTokenSerializer, serialized)

        assertThat((deserialized.tokenAtReset as GlobalSequenceTrackingToken).globalIndex).isEqualTo(100L)
        assertThat((deserialized.currentToken as GlobalSequenceTrackingToken).globalIndex).isEqualTo(50L)
        // Context is stored as ByteArray for cross-serializer compatibility
        val contextBytes = deserialized.context() as ByteArray
        assertThat(String(contextBytes, Charsets.UTF_8)).isEqualTo("replay-reason")
    }

    @Test
    fun `should serialize ReplayToken with JsonElement context and deserialize as ByteArray`() {
        val tokenAtReset = GlobalSequenceTrackingToken(100L)
        val currentToken = GlobalSequenceTrackingToken(50L)
        val context = JsonPrimitive("replay-reason")
        val original = ReplayToken.createReplayToken(tokenAtReset, currentToken, context) as ReplayToken

        val serialized = json.encodeToString(ReplayTokenSerializer, original)
        val deserialized = json.decodeFromString(ReplayTokenSerializer, serialized)

        assertThat((deserialized.tokenAtReset as GlobalSequenceTrackingToken).globalIndex).isEqualTo(100L)
        assertThat((deserialized.currentToken as GlobalSequenceTrackingToken).globalIndex).isEqualTo(50L)
        // JsonElement context is converted to JSON bytes representation for cross-serializer compatibility
        val contextBytes = deserialized.context() as ByteArray
        assertThat(String(contextBytes, Charsets.UTF_8)).isEqualTo("\"replay-reason\"")
    }

    @Test
    fun `should serialize and deserialize ReplayToken with null currentToken`() {
        val tokenAtReset = GlobalSequenceTrackingToken(100L)
        val original = ReplayToken.createReplayToken(tokenAtReset, null, null) as ReplayToken

        val serialized = json.encodeToString(ReplayTokenSerializer, original)
        val deserialized = json.decodeFromString(ReplayTokenSerializer, serialized)

        assertThat((deserialized.tokenAtReset as GlobalSequenceTrackingToken).globalIndex).isEqualTo(100L)
        assertThat(deserialized.currentToken).isNull()
    }

    @Test
    fun `GlobalSequenceTrackingToken JSON format should be correct`() {
        val token = GlobalSequenceTrackingToken(42L)
        val serialized = json.encodeToString(GlobalSequenceTrackingTokenSerializer, token)
        assertThat(serialized).isEqualTo("""{"globalIndex":42}""")
    }

    @Test
    fun `GapAwareTrackingToken JSON format should be correct`() {
        val token = GapAwareTrackingToken.newInstance(10L, setOf(5L, 7L))
        val serialized = json.encodeToString(GapAwareTrackingTokenSerializer, token)
        // Gaps might be in any order due to Set, but the structure should be correct
        assertThat(serialized.contains("\"index\":10")).isEqualTo(true)
        assertThat(serialized.contains("\"gaps\"")).isEqualTo(true)
    }

    @Test
    fun `ConfigToken JSON format should be correct`() {
        val token = ConfigToken(mapOf("key" to "value"))
        val serialized = json.encodeToString(ConfigTokenSerializer, token)
        assertThat(serialized).isEqualTo("""{"config":{"key":"value"}}""")
    }

    // Custom context class for testing ReplayToken context conversion
    @Serializable
    data class ReplayContext(
        val reason: String,
        val triggeredBy: String,
        val timestamp: Long
    )

    @Test
    fun `should serialize ReplayToken with custom object context and convert back using Converter`() {
        // Given: A ReplayToken with a custom object as context
        val tokenAtReset = GlobalSequenceTrackingToken(100L)
        val currentToken = GlobalSequenceTrackingToken(50L)
        val originalContext = ReplayContext(
            reason = "Data migration",
            triggeredBy = "admin-user",
            timestamp = 1234567890L
        )

        // When: Create ReplayToken with custom context and serialize/deserialize it
        val original = ReplayToken.createReplayToken(tokenAtReset, currentToken, originalContext) as ReplayToken
        val serialized = json.encodeToString(ReplayTokenSerializer, original)
        val deserialized = json.decodeFromString(ReplayTokenSerializer, serialized)

        // Then: The context is stored as ByteArray
        assertThat(deserialized.context()).isNotNull()
        val contextBytes = deserialized.context() as ByteArray

        // And: We can convert the ByteArray context back to the original type using Converter
        val converter = KotlinSerializationConverter(json)
        val restoredContext = converter.convert<ReplayContext>(contextBytes, ReplayContext::class.java)

        assertThat(restoredContext).isNotNull()
        assertThat(restoredContext!!.reason).isEqualTo("Data migration")
        assertThat(restoredContext.triggeredBy).isEqualTo("admin-user")
        assertThat(restoredContext.timestamp).isEqualTo(1234567890L)
    }

    @Test
    fun `should serialize ReplayToken with nested object context and convert back using Converter`() {
        // Given: A more complex nested context object
        @Serializable
        data class ReplayMetadata(val version: Int, val tags: List<String>)

        @Serializable
        data class ComplexReplayContext(
            val reason: String,
            val metadata: ReplayMetadata
        )

        val tokenAtReset = GlobalSequenceTrackingToken(200L)
        val originalContext = ComplexReplayContext(
            reason = "Schema upgrade",
            metadata = ReplayMetadata(version = 2, tags = listOf("migration", "v2"))
        )

        // When: Create ReplayToken with complex context and serialize/deserialize
        val original = ReplayToken.createReplayToken(tokenAtReset, null, originalContext) as ReplayToken
        val serialized = json.encodeToString(ReplayTokenSerializer, original)
        val deserialized = json.decodeFromString(ReplayTokenSerializer, serialized)

        // Then: Convert the ByteArray context back to the original type
        val converter = KotlinSerializationConverter(json)
        val contextBytes = deserialized.context() as ByteArray
        val restoredContext = converter.convert<ComplexReplayContext>(contextBytes, ComplexReplayContext::class.java)

        assertThat(restoredContext).isNotNull()
        assertThat(restoredContext!!.reason).isEqualTo("Schema upgrade")
        assertThat(restoredContext.metadata.version).isEqualTo(2)
        assertThat(restoredContext.metadata.tags).isEqualTo(listOf("migration", "v2"))
    }
}
