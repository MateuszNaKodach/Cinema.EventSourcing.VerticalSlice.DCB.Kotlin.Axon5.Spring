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
import org.axonframework.messaging.eventhandling.processing.streaming.token.GlobalSequenceTrackingToken

/**
 * Serializer for [GlobalSequenceTrackingToken].
 *
 * @see GlobalSequenceTrackingToken
 */
object GlobalSequenceTrackingTokenSerializer : KSerializer<GlobalSequenceTrackingToken> {

    override val descriptor = buildClassSerialDescriptor(GlobalSequenceTrackingToken::class.java.name) {
        element<Long>("globalIndex")
    }

    override fun deserialize(decoder: Decoder) = decoder.decodeStructure(descriptor) {
        var globalIndex: Long? = null
        while (true) {
            val index = decodeElementIndex(descriptor)
            if (index == CompositeDecoder.DECODE_DONE) break
            when (index) {
                0 -> globalIndex = decodeLongElement(descriptor, index)
            }
        }
        GlobalSequenceTrackingToken(
            globalIndex ?: throw SerializationException("Element 'globalIndex' is missing"),
        )
    }

    override fun serialize(encoder: Encoder, value: GlobalSequenceTrackingToken) = encoder.encodeStructure(descriptor) {
        encodeLongElement(descriptor, 0, value.globalIndex)
    }
}
