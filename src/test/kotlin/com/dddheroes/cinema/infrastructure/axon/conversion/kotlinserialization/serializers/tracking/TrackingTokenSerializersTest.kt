package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.tracking

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.AxonSerializersModule
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
    fun `should serialize and deserialize ReplayToken with JsonElement context`() {
        val tokenAtReset = GlobalSequenceTrackingToken(100L)
        val currentToken = GlobalSequenceTrackingToken(50L)
        val context = JsonPrimitive("replay-reason")
        val original = ReplayToken.createReplayToken(tokenAtReset, currentToken, context) as ReplayToken

        val serialized = json.encodeToString(ReplayTokenSerializer, original)
        val deserialized = json.decodeFromString(ReplayTokenSerializer, serialized)

        assertThat((deserialized.tokenAtReset as GlobalSequenceTrackingToken).globalIndex).isEqualTo(100L)
        assertThat((deserialized.currentToken as GlobalSequenceTrackingToken).globalIndex).isEqualTo(50L)
        assertThat(deserialized.context()).isEqualTo(context)
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
}
