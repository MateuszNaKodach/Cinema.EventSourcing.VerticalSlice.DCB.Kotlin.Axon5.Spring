package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.contenttypeconverters

import kotlinx.serialization.json.JsonElement
import org.axonframework.conversion.ContentTypeConverter

/**
 * Converts [JsonElement] to [ByteArray] using UTF-8 encoding.
 */
class JsonElementToByteArrayConverter : ContentTypeConverter<JsonElement, ByteArray> {

    override fun expectedSourceType(): Class<JsonElement> = JsonElement::class.java

    override fun targetType(): Class<ByteArray> = ByteArray::class.java

    override fun convert(input: JsonElement?): ByteArray? {
        return input?.toString()?.toByteArray(Charsets.UTF_8)
    }
}
