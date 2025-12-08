package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization

import com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.metadata.ComposedMetadataSerializer
import com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.time.JavaInstantSerializer
import com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.tracking.ConfigTokenSerializer
import com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.tracking.GapAwareTrackingTokenSerializer
import com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.tracking.GlobalSequenceTrackingTokenSerializer
import com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.tracking.MergedTrackingTokenSerializer
import com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.serializers.tracking.ReplayTokenSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.axonframework.messaging.core.Metadata
import org.axonframework.messaging.eventhandling.processing.streaming.token.GapAwareTrackingToken
import org.axonframework.messaging.eventhandling.processing.streaming.token.GlobalSequenceTrackingToken
import org.axonframework.messaging.eventhandling.processing.streaming.token.MergedTrackingToken
import org.axonframework.messaging.eventhandling.processing.streaming.token.ReplayToken
import org.axonframework.messaging.eventhandling.processing.streaming.token.TrackingToken
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.ConfigToken
import java.time.Instant

/**
 * Module defining serializers for Axon Framework 5's core event handling and messaging components.
 * This module includes serializers for TrackingTokens and Metadata enabling
 * seamless integration with Axon-based applications.
 *
 * Note: MultiSourceTrackingToken is not yet available in Axon Framework 5 (it's in stash/todo).
 * Note: ScheduleTokens and ResponseTypes are not included as they are not present in Axon Framework 5.
 */
val AxonSerializersModule = SerializersModule {
    // Java time types
    contextual(Instant::class) { JavaInstantSerializer }

    // TrackingTokens - contextual serializers
    contextual(ConfigToken::class) { ConfigTokenSerializer }
    contextual(GapAwareTrackingToken::class) { GapAwareTrackingTokenSerializer }
    contextual(GlobalSequenceTrackingToken::class) { GlobalSequenceTrackingTokenSerializer }
    contextual(MergedTrackingToken::class) { MergedTrackingTokenSerializer }
    contextual(ReplayToken::class) { ReplayTokenSerializer }

    // TrackingTokens - polymorphic serializers for handling TrackingToken interface
    polymorphic(TrackingToken::class) {
        subclass(ConfigTokenSerializer)
        subclass(GapAwareTrackingTokenSerializer)
        subclass(GlobalSequenceTrackingTokenSerializer)
        subclass(MergedTrackingTokenSerializer)
        subclass(ReplayTokenSerializer)
    }

    // Metadata serializer
    contextual(Metadata::class) { ComposedMetadataSerializer }
}
