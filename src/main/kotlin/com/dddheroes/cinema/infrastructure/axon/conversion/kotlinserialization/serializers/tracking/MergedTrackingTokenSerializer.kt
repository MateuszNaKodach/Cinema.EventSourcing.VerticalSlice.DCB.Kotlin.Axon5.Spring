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
import org.axonframework.messaging.eventhandling.processing.streaming.token.MergedTrackingToken
import org.axonframework.messaging.eventhandling.processing.streaming.token.TrackingToken

/**
 * Serializer for [MergedTrackingToken].
 *
 * @see MergedTrackingToken
 */
object MergedTrackingTokenSerializer : KSerializer<MergedTrackingToken> {

    override val descriptor = buildClassSerialDescriptor(MergedTrackingToken::class.java.name) {
        element<TrackingToken>("lowerSegmentToken")
        element<TrackingToken>("upperSegmentToken")
    }

    override fun deserialize(decoder: Decoder) = decoder.decodeStructure(descriptor) {
        var lowerSegmentToken: TrackingToken? = null
        var upperSegmentToken: TrackingToken? = null
        while (true) {
            val index = decodeElementIndex(descriptor)
            if (index == CompositeDecoder.DECODE_DONE) break
            when (index) {
                0 -> lowerSegmentToken = decodeSerializableElement(descriptor, index, trackingTokenSerializer)
                1 -> upperSegmentToken = decodeSerializableElement(descriptor, index, trackingTokenSerializer)
            }
        }
        MergedTrackingToken(
            lowerSegmentToken ?: throw SerializationException("Element 'lowerSegmentToken' is missing"),
            upperSegmentToken ?: throw SerializationException("Element 'upperSegmentToken' is missing"),
        )
    }

    override fun serialize(encoder: Encoder, value: MergedTrackingToken) = encoder.encodeStructure(descriptor) {
        encodeSerializableElement(descriptor, 0, trackingTokenSerializer, value.lowerSegmentToken())
        encodeSerializableElement(descriptor, 1, trackingTokenSerializer, value.upperSegmentToken())
    }
}
