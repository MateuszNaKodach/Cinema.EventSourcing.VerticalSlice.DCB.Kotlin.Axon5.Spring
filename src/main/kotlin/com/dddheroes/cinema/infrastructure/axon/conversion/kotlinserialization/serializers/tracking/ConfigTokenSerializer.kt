package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.tracking

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.ConfigToken

/**
 * Serializer for [ConfigToken].
 *
 * @see ConfigToken
 */
object ConfigTokenSerializer : KSerializer<ConfigToken> {

    private val mapSerializer = MapSerializer(String.serializer(), String.serializer())

    override val descriptor = buildClassSerialDescriptor(ConfigToken::class.java.name) {
        element<Map<String, String>>("config")
    }

    override fun deserialize(decoder: Decoder): ConfigToken = decoder.decodeStructure(descriptor) {
        var config: Map<String, String>? = null
        while (true) {
            val index = decodeElementIndex(descriptor)
            if (index == CompositeDecoder.DECODE_DONE) break
            when (index) {
                0 -> config = decodeSerializableElement(descriptor, index, mapSerializer)
            }
        }
        ConfigToken(
            config ?: throw SerializationException("Element 'config' is missing"),
        )
    }

    override fun serialize(encoder: Encoder, value: ConfigToken) = encoder.encodeStructure(descriptor) {
        encodeSerializableElement(descriptor, 0, mapSerializer, value.config)
    }
}
