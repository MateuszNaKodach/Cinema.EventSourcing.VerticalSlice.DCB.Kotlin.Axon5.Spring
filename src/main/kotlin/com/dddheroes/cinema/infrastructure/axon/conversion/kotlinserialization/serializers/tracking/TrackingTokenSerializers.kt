package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.tracking

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.nullable
import org.axonframework.messaging.eventhandling.processing.streaming.token.TrackingToken

/**
 * Serializer for Axon's [TrackingToken] class.
 * Provides serialization and deserialization support for nullable instances of TrackingToken.
 *
 * @see TrackingToken
 */
val trackingTokenSerializer = PolymorphicSerializer(TrackingToken::class).nullable
