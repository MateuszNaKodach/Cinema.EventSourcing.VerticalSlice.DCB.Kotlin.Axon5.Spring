package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.metadata

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import org.axonframework.messaging.core.Metadata

/**
 * A composite Kotlinx [KSerializer] for Axon Framework's [Metadata] type that selects the
 * appropriate serializer based on the encoder/decoder type.
 *
 * This serializer delegates to:
 * - [JsonMetadataSerializer] when used with [JsonEncoder]/[JsonDecoder]
 * - [StringMetadataSerializer] for all other encoder/decoder types
 *
 * This allows efficient JSON serialization without unnecessary string encoding, while
 * maintaining compatibility with all other serialization formats through string-based
 * serialization.
 */
object ComposedMetadataSerializer : KSerializer<Metadata> {

    override val descriptor: SerialDescriptor = StringMetadataSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Metadata) {
        when (encoder) {
            is JsonEncoder -> JsonMetadataSerializer.serialize(encoder, value)
            else -> StringMetadataSerializer.serialize(encoder, value)
        }
    }

    override fun deserialize(decoder: Decoder): Metadata {
        return when (decoder) {
            is JsonDecoder -> JsonMetadataSerializer.deserialize(decoder)
            else -> StringMetadataSerializer.deserialize(decoder)
        }
    }
}
