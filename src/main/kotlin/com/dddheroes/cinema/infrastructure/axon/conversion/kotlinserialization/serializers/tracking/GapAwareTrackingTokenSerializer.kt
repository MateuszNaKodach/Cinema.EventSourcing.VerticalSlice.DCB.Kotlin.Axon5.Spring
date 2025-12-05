package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.tracking

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import org.axonframework.messaging.eventhandling.processing.streaming.token.GapAwareTrackingToken

/**
 * Serializer for [GapAwareTrackingToken].
 *
 * @see GapAwareTrackingToken
 */
object GapAwareTrackingTokenSerializer : KSerializer<GapAwareTrackingToken> {

    private val setSerializer = SetSerializer(Long.serializer())

    override val descriptor = buildClassSerialDescriptor(GapAwareTrackingToken::class.java.name) {
        element<Long>("index")
        element("gaps", setSerializer.descriptor)
    }

    override fun deserialize(decoder: Decoder) = decoder.decodeStructure(descriptor) {
        var index: Long? = null
        var gaps: Set<Long>? = null
        while (true) {
            val elementIndex = decodeElementIndex(descriptor)
            if (elementIndex == CompositeDecoder.DECODE_DONE) break
            when (elementIndex) {
                0 -> index = decodeLongElement(descriptor, elementIndex)
                1 -> gaps = decodeSerializableElement(descriptor, elementIndex, setSerializer)
            }
        }
        GapAwareTrackingToken(
            index ?: throw SerializationException("Element 'index' is missing"),
            gaps ?: throw SerializationException("Element 'gaps' is missing"),
        )
    }

    override fun serialize(encoder: Encoder, value: GapAwareTrackingToken) = encoder.encodeStructure(descriptor) {
        encodeLongElement(descriptor, 0, value.index)
        encodeSerializableElement(descriptor, 1, setSerializer, value.gaps)
    }
}
