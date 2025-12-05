package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.axonframework.messaging.core.Metadata
import org.axonframework.messaging.eventhandling.processing.streaming.token.GapAwareTrackingToken
import org.axonframework.messaging.eventhandling.processing.streaming.token.GlobalSequenceTrackingToken
import org.axonframework.messaging.eventhandling.processing.streaming.token.MergedTrackingToken
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.ConfigToken
import org.junit.jupiter.api.Test

class KotlinSerializationConverterTest {

    private val converter = KotlinSerializationConverter(
        Json {
            serializersModule = AxonSerializersModule
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    )

    @Test
    fun `should convert null to null`() {
        val result = converter.convert<String>(null, String::class.java)
        assertThat(result).isNull()
    }

    @Test
    fun `should return same instance when source and target types are identical`() {
        val input = "test"
        val result = converter.convert<String>(input, String::class.java)
        assertThat(result).isEqualTo(input)
    }

    @Test
    fun `should serialize object to String`() {
        val token = GlobalSequenceTrackingToken(42L)
        val result = converter.convert<String>(token, String::class.java)
        assertThat(result).isNotNull()
        assertThat(result!!.contains("42")).isTrue()
    }

    @Test
    fun `should deserialize String to object`() {
        val json = """{"globalIndex":42}"""
        val result = converter.convert<GlobalSequenceTrackingToken>(json, GlobalSequenceTrackingToken::class.java)
        assertThat(result).isNotNull()
        assertThat(result!!.globalIndex).isEqualTo(42L)
    }

    @Test
    fun `should serialize object to ByteArray`() {
        val token = GlobalSequenceTrackingToken(42L)
        val result = converter.convert<ByteArray>(token, ByteArray::class.java)
        assertThat(result).isNotNull()
        assertThat(String(result!!)).isEqualTo("""{"globalIndex":42}""")
    }

    @Test
    fun `should deserialize ByteArray to object`() {
        val json = """{"globalIndex":42}""".toByteArray()
        val result = converter.convert<GlobalSequenceTrackingToken>(json, GlobalSequenceTrackingToken::class.java)
        assertThat(result).isNotNull()
        assertThat(result!!.globalIndex).isEqualTo(42L)
    }

    @Test
    fun `should convert GlobalSequenceTrackingToken round-trip`() {
        val original = GlobalSequenceTrackingToken(100L)
        val json = converter.convert<String>(original, String::class.java)!!
        val restored = converter.convert<GlobalSequenceTrackingToken>(json, GlobalSequenceTrackingToken::class.java)
        assertThat(restored).isEqualTo(original)
    }

    @Test
    fun `should convert GapAwareTrackingToken round-trip`() {
        val original = GapAwareTrackingToken.newInstance(100L, setOf(95L, 97L, 99L))
        val json = converter.convert<String>(original, String::class.java)!!
        val restored = converter.convert<GapAwareTrackingToken>(json, GapAwareTrackingToken::class.java)
        assertThat(restored!!.index).isEqualTo(original.index)
        assertThat(restored.gaps).isEqualTo(original.gaps)
    }

    @Test
    fun `should convert ConfigToken round-trip`() {
        val original = ConfigToken(mapOf("key1" to "value1", "key2" to "value2"))
        val json = converter.convert<String>(original, String::class.java)!!
        val restored = converter.convert<ConfigToken>(json, ConfigToken::class.java)
        assertThat(restored!!.config).isEqualTo(original.config)
    }

    @Test
    fun `should convert MergedTrackingToken round-trip`() {
        val lower = GlobalSequenceTrackingToken(50L)
        val upper = GlobalSequenceTrackingToken(100L)
        val original = MergedTrackingToken(lower, upper)
        val json = converter.convert<String>(original, String::class.java)!!
        val restored = converter.convert<MergedTrackingToken>(json, MergedTrackingToken::class.java)
        assertThat(restored).isNotNull()
        assertThat((restored!!.lowerSegmentToken() as GlobalSequenceTrackingToken).globalIndex).isEqualTo(50L)
        assertThat((restored.upperSegmentToken() as GlobalSequenceTrackingToken).globalIndex).isEqualTo(100L)
    }

    @Test
    fun `should convert Metadata round-trip`() {
        val original = Metadata(mapOf("correlationId" to "123", "userId" to "user-456"))
        val json = converter.convert<String>(original, String::class.java)!!
        val restored = converter.convert<Metadata>(json, Metadata::class.java)
        assertThat(restored!!["correlationId"]).isEqualTo("123")
        assertThat(restored["userId"]).isEqualTo("user-456")
    }

    @Test
    fun `should convert empty Metadata`() {
        val original = Metadata.emptyInstance()
        val json = converter.convert<String>(original, String::class.java)!!
        val restored = converter.convert<Metadata>(json, Metadata::class.java)
        assertThat(restored!!.isEmpty()).isTrue()
    }

    @Test
    fun `should report canConvert for supported types`() {
        assertThat(converter.canConvert(GlobalSequenceTrackingToken::class.java, String::class.java)).isTrue()
        assertThat(converter.canConvert(String::class.java, GlobalSequenceTrackingToken::class.java)).isTrue()
        assertThat(converter.canConvert(GlobalSequenceTrackingToken::class.java, ByteArray::class.java)).isTrue()
        assertThat(converter.canConvert(ByteArray::class.java, GlobalSequenceTrackingToken::class.java)).isTrue()
    }

    @Test
    fun `should convert to JsonElement`() {
        val token = GlobalSequenceTrackingToken(42L)
        val result = converter.convert<JsonElement>(token, JsonElement::class.java)
        assertThat(result).isNotNull()
        assertThat(result!!.toString()).isEqualTo("""{"globalIndex":42}""")
    }

    @Test
    fun `should convert from JsonElement`() {
        val json = Json.parseToJsonElement("""{"globalIndex":42}""")
        val result = converter.convert<GlobalSequenceTrackingToken>(json, GlobalSequenceTrackingToken::class.java)
        assertThat(result).isNotNull()
        assertThat(result!!.globalIndex).isEqualTo(42L)
    }

    @Serializable
    data class TestEvent(val id: String, val value: Int)

    @Test
    fun `should convert custom serializable objects`() {
        val original = TestEvent("test-id", 42)
        val json = converter.convert<String>(original, String::class.java)!!
        val restored = converter.convert<TestEvent>(json, TestEvent::class.java)
        assertThat(restored).isEqualTo(original)
    }
}
