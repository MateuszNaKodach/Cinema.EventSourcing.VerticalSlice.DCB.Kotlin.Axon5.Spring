package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.tracking

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import org.axonframework.messaging.eventhandling.processing.streaming.token.ReplayToken
import org.axonframework.messaging.eventhandling.processing.streaming.token.TrackingToken

/**
 * Serializer for [ReplayToken].
 *
 * The [ReplayToken.context] field is stored as a raw ByteArray for cross-serializer compatibility.
 * This allows the context to be:
 * - Serialized/deserialized by any serializer (Jackson, Kotlin Serialization, CBOR, ProtoBuf, etc.)
 * - Converted to the expected type on demand using the `Converter`
 *
 * The context is stored as:
 * - `null` if no context was provided
 * - A ByteArray containing the JSON representation of the original context object
 *
 * To deserialize the context to a specific type, use `Converter.convert(replayToken.context(), YourType::class.java)`
 *
 * @see ReplayToken
 */
object ReplayTokenSerializer : KSerializer<ReplayToken> {

    // Internal JSON instance for context serialization
    private val contextJson = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    override val descriptor = buildClassSerialDescriptor(ReplayToken::class.java.name) {
        element<TrackingToken>("tokenAtReset")
        element<TrackingToken>("currentToken")
        element<ByteArray>("resetContext")  // Store as ByteArray for cross-serializer compatibility
    }

    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder) = decoder.decodeStructure(descriptor) {
        var tokenAtReset: TrackingToken? = null
        var currentToken: TrackingToken? = null
        var resetContextBytes: ByteArray? = null
        while (true) {
            val index = decodeElementIndex(descriptor)
            if (index == CompositeDecoder.DECODE_DONE) break
            when (index) {
                0 -> tokenAtReset = decodeSerializableElement(descriptor, index, trackingTokenSerializer)
                1 -> currentToken = decodeSerializableElement(descriptor, index, trackingTokenSerializer)
                2 -> resetContextBytes = decodeNullableSerializableElement(descriptor, index, ByteArraySerializer())
            }
        }
        // Store the raw ByteArray as the context - it can be converted later using Converter
        ReplayToken.createReplayToken(
            tokenAtReset ?: throw SerializationException("Element 'tokenAtReset' is missing"),
            currentToken,
            resetContextBytes  // Store as ByteArray for compatibility
        ) as ReplayToken
    }

    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: ReplayToken) = encoder.encodeStructure(descriptor) {
        encodeSerializableElement(descriptor, 0, trackingTokenSerializer, value.tokenAtReset)
        encodeSerializableElement(descriptor, 1, trackingTokenSerializer, value.currentToken)

        // Convert context to ByteArray for storage
        val contextBytes: ByteArray? = when (val ctx = value.context()) {
            null -> null
            is ByteArray -> ctx  // Already a ByteArray, keep as-is
            is String -> ctx.toByteArray(Charsets.UTF_8)  // Convert String to bytes
            is JsonElement -> if (ctx is JsonNull) null else ctx.toString().toByteArray(Charsets.UTF_8)
            else -> {
                // For any other type, try to serialize it to JSON bytes
                // This allows storing complex objects as JSON bytes
                try {
                    contextJson.encodeToString(JsonElement.serializer(), contextJson.encodeToJsonElement(
                        kotlinx.serialization.serializer(ctx::class.java),
                        ctx
                    )).toByteArray(Charsets.UTF_8)
                } catch (e: Exception) {
                    // Fallback to toString() if serialization fails
                    ctx.toString().toByteArray(Charsets.UTF_8)
                }
            }
        }
        encodeNullableSerializableElement(descriptor, 2, ByteArraySerializer(), contextBytes)
    }
}
