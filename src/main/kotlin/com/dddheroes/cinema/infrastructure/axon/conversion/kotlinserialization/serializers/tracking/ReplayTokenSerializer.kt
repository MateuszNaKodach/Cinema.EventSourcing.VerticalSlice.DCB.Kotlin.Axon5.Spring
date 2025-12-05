package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.tracking

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import org.axonframework.messaging.eventhandling.processing.streaming.token.ReplayToken
import org.axonframework.messaging.eventhandling.processing.streaming.token.TrackingToken

/**
 * Serializer for [ReplayToken].
 *
 * The [ReplayToken.context] field is kept as raw [JsonElement] and not deserialized to a specific type.
 * This allows the user to pass the context to the `convert()` method with the expected type when needed.
 *
 * @see ReplayToken
 */
object ReplayTokenSerializer : KSerializer<ReplayToken> {

    override val descriptor = buildClassSerialDescriptor(ReplayToken::class.java.name) {
        element<TrackingToken>("tokenAtReset")
        element<TrackingToken>("currentToken")
        element<JsonElement>("resetContext")
    }

    override fun deserialize(decoder: Decoder) = decoder.decodeStructure(descriptor) {
        var tokenAtReset: TrackingToken? = null
        var currentToken: TrackingToken? = null
        var resetContext: JsonElement? = null
        while (true) {
            val index = decodeElementIndex(descriptor)
            if (index == CompositeDecoder.DECODE_DONE) break
            when (index) {
                0 -> tokenAtReset = decodeSerializableElement(descriptor, index, trackingTokenSerializer)
                1 -> currentToken = decodeSerializableElement(descriptor, index, trackingTokenSerializer)
                2 -> resetContext = decodeSerializableElement(descriptor, index, JsonElement.serializer())
            }
        }
        // Store the raw JsonElement as the context - it will be converted later if needed
        val context = if (resetContext == null || resetContext is JsonNull) null else resetContext
        ReplayToken.createReplayToken(
            tokenAtReset ?: throw SerializationException("Element 'tokenAtReset' is missing"),
            currentToken,
            context
        ) as ReplayToken
    }

    override fun serialize(encoder: Encoder, value: ReplayToken) = encoder.encodeStructure(descriptor) {
        encodeSerializableElement(descriptor, 0, trackingTokenSerializer, value.tokenAtReset)
        encodeSerializableElement(descriptor, 1, trackingTokenSerializer, value.currentToken)
        // Serialize context as JsonElement - if it's already a JsonElement, use it directly,
        // otherwise try to convert it
        val contextElement = when (val ctx = value.context()) {
            null -> JsonNull
            is JsonElement -> ctx
            else -> throw SerializationException(
                "ReplayToken context must be JsonElement for serialization. " +
                "Found: ${ctx::class.simpleName}. Convert your context to JsonElement before storing."
            )
        }
        encodeSerializableElement(descriptor, 2, JsonElement.serializer(), contextElement)
    }
}
