package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.contenttypeconverters

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.axonframework.conversion.ContentTypeConverter

/**
 * Converts [ByteArray] to [JsonElement] by parsing the byte array as a UTF-8 encoded JSON string.
 *
 * @param json The [Json] instance used for parsing.
 */
class ByteArrayToJsonElementConverter(
    private val json: Json = Json
) : ContentTypeConverter<ByteArray, JsonElement> {

    override fun expectedSourceType(): Class<ByteArray> = ByteArray::class.java

    override fun targetType(): Class<JsonElement> = JsonElement::class.java

    override fun convert(input: ByteArray?): JsonElement? {
        if (input == null) return null
        val jsonString = String(input, Charsets.UTF_8)
        return json.parseToJsonElement(jsonString)
    }
}
