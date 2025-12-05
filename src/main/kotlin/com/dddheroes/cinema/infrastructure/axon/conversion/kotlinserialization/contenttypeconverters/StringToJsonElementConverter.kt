package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.contenttypeconverters

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.axonframework.conversion.ContentTypeConverter

/**
 * Converts [String] to [JsonElement] by parsing the string as JSON.
 *
 * @param json The [Json] instance used for parsing.
 */
class StringToJsonElementConverter(
    private val json: Json = Json
) : ContentTypeConverter<String, JsonElement> {

    override fun expectedSourceType(): Class<String> = String::class.java

    override fun targetType(): Class<JsonElement> = JsonElement::class.java

    override fun convert(input: String?): JsonElement? {
        if (input == null) return null
        return json.parseToJsonElement(input)
    }
}
