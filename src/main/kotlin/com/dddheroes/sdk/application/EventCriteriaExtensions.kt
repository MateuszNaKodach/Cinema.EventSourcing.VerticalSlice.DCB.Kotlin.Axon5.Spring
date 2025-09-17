package com.dddheroes.sdk.application

import org.axonframework.eventstreaming.EventCriteria
import org.axonframework.eventstreaming.Tag

fun anyOf(criteria: Collection<EventCriteria>): EventCriteria {
    val initial: EventCriteria = EventCriteria.havingTags(Tag.of("none", "none"))
    return criteria.fold(initial) { acc, eventCriteria -> acc.or(eventCriteria) }
}