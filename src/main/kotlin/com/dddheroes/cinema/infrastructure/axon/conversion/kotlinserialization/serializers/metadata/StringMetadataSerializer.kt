package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.metadata

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.axonframework.messaging.core.Metadata

/**
 * A Kotlinx [KSerializer] for Axon Framework's [Metadata] type, suitable for serialization across any format.
 *
 * This serializer converts a [Metadata] instance to a JSON-encoded [String] using a Map serializer.
 * This JSON string is then serialized using [String.serializer()], ensuring compatibility with any serialization format.
 *
 * Note: In Axon Framework 5, Metadata is now `Map<String, String>` (not `Map<String, Object>`), making serialization simpler.
 */
object StringMetadataSerializer : KSerializer<Metadata> {

    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }
    private val mapSerializer = MapSerializer(String.serializer(), String.serializer())

    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Metadata) {
        val jsonString = json.encodeToString(mapSerializer, value.toMap())
        encoder.encodeString(jsonString)
    }

    override fun deserialize(decoder: Decoder): Metadata {
        val jsonString = decoder.decodeString()
        val map = json.decodeFromString(mapSerializer, jsonString)
        return Metadata(map)
    }

    private fun Metadata.toMap(): Map<String, String> = this.entries.associate { it.key to it.value }
}
