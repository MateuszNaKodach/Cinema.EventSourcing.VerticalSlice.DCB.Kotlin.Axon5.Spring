package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.contenttypeconverters

import kotlinx.serialization.json.JsonElement
import org.axonframework.conversion.ContentTypeConverter

/**
 * Converts [JsonElement] to [String] by converting the JSON element to its string representation.
 */
class JsonElementToStringConverter : ContentTypeConverter<JsonElement, String> {

    override fun expectedSourceType(): Class<JsonElement> = JsonElement::class.java

    override fun targetType(): Class<String> = String::class.java

    override fun convert(input: JsonElement?): String? {
        return input?.toString()
    }
}
