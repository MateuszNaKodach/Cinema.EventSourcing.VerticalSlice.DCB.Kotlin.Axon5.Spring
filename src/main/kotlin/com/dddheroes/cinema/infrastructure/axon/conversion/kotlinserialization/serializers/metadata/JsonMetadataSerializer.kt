package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.metadata

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.axonframework.messaging.core.Metadata

/**
 * A Kotlinx [KSerializer] for Axon Framework's [Metadata] type, optimized for JSON serialization.
 *
 * This serializer converts a [Metadata] instance directly to a JSON object structure (Map<String, String>),
 * avoiding the string-encoding that [StringMetadataSerializer] uses. This ensures JSON values
 * are properly encoded without quote escaping.
 *
 * Note: In Axon Framework 5, Metadata is now `Map<String, String>` (not `Map<String, Object>`), making serialization simpler.
 */
object JsonMetadataSerializer : KSerializer<Metadata> {

    private val mapSerializer = MapSerializer(String.serializer(), String.serializer())

    override val descriptor: SerialDescriptor = mapSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Metadata) {
        encoder.encodeSerializableValue(mapSerializer, value.toMap())
    }

    override fun deserialize(decoder: Decoder): Metadata {
        val map = decoder.decodeSerializableValue(mapSerializer)
        return Metadata(map)
    }

    private fun Metadata.toMap(): Map<String, String> = this.entries.associate { it.key to it.value }
}
