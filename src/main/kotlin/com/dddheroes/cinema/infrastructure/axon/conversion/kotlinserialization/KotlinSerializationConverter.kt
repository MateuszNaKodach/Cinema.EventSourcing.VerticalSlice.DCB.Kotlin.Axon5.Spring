package com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization

import com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.contenttypeconverters.ByteArrayToJsonElementConverter
import com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.contenttypeconverters.JsonElementToByteArrayConverter
import com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.contenttypeconverters.JsonElementToStringConverter
import com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.contenttypeconverters.StringToJsonElementConverter
import jakarta.annotation.Nonnull
import jakarta.annotation.Nullable
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import org.axonframework.common.infra.ComponentDescriptor
import org.axonframework.conversion.ChainingContentTypeConverter
import org.axonframework.conversion.ConversionException
import org.axonframework.conversion.Converter
import org.slf4j.LoggerFactory
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * A [Converter] implementation that uses kotlinx.serialization to convert objects into and from
 * JSON or binary formats.
 *
 * This converter supports both [StringFormat] (like Json) and [BinaryFormat] (like Cbor, ProtoBuf)
 * through the [SerialFormat] abstraction.
 *
 * @param serialFormat The kotlinx.serialization format to use for serialization/deserialization.
 * @param chainingConverter The converter used for simpler type conversions.
 */
class KotlinSerializationConverter(
    private val serialFormat: SerialFormat,
    private val chainingConverter: ChainingContentTypeConverter = ChainingContentTypeConverter()
) : Converter {

    private val logger = LoggerFactory.getLogger(KotlinSerializationConverter::class.java)

    init {
        // Register format-specific ContentTypeConverters
        when (serialFormat) {
            is StringFormat -> {
                val json = if (serialFormat is Json) serialFormat else Json
                chainingConverter.registerConverter(JsonElementToByteArrayConverter())
                chainingConverter.registerConverter(ByteArrayToJsonElementConverter(json))
                chainingConverter.registerConverter(StringToJsonElementConverter(json))
                chainingConverter.registerConverter(JsonElementToStringConverter())
            }
        }
    }

    /**
     * Creates a [KotlinSerializationConverter] with a default [Json] configuration that includes
     * the [AxonSerializersModule].
     */
    constructor() : this(
        Json {
            serializersModule = AxonSerializersModule
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    )

    override fun canConvert(@Nonnull sourceType: Type, @Nonnull targetType: Type): Boolean {
        if (logger.isTraceEnabled) {
            logger.trace(
                "Validating if we can convert from source type [{}] to target type [{}].",
                sourceType, targetType
            )
        }
        return sourceType == targetType
                || chainingConverter.canConvert(sourceType, targetType)
                || chainingConverter.canConvert(sourceType, ByteArray::class.java)
                || chainingConverter.canConvert(ByteArray::class.java, targetType)
                || isSerializableType(sourceType)
                || isSerializableType(targetType)
    }

    @Nullable
    @Suppress("UNCHECKED_CAST")
    override fun <T> convert(@Nullable input: Any?, @Nonnull targetType: Type): T? {
        if (input == null) {
            if (logger.isTraceEnabled) {
                logger.trace("Input to convert is null, so returning null immediately.")
            }
            return null
        }

        val sourceType = input::class.java
        if (sourceType == targetType) {
            if (logger.isTraceEnabled) {
                logger.trace("Casting given input since source and target type are identical.")
            }
            return input as T
        }

        val targetClass = when (targetType) {
            is Class<*> -> targetType
            else -> throw ConversionException(
                "The targetType [$targetType] is not of type Class<?>, " +
                "which is required for KotlinSerializationConverter."
            )
        }

        try {
            // Try direct conversion via chaining converter
            if (chainingConverter.canConvert(sourceType, targetClass)) {
                if (logger.isTraceEnabled) {
                    logger.trace(
                        "Converter [{}] will do the entire conversion from source [{}] to target [{}] for [{}].",
                        chainingConverter, sourceType, targetType, input
                    )
                }
                return chainingConverter.convert(input, targetClass) as T?
            }

            // Try conversion through serialization
            return when (serialFormat) {
                is StringFormat -> convertWithStringFormat(input, sourceType, targetClass, serialFormat)
                is BinaryFormat -> convertWithBinaryFormat(input, sourceType, targetClass, serialFormat)
                else -> throw ConversionException(
                    "Unsupported SerialFormat type: ${serialFormat::class.simpleName}"
                )
            }
        } catch (e: Exception) {
            throw ConversionException(
                "Exception when trying to convert object of type '${sourceType.typeName}' to '${targetType.typeName}'",
                e
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    @OptIn(InternalSerializationApi::class)
    private fun <T> convertWithStringFormat(
        input: Any,
        sourceType: Class<*>,
        targetClass: Class<*>,
        stringFormat: StringFormat
    ): T? {
        // Input is String - deserialize to target
        if (input is String) {
            if (logger.isTraceEnabled) {
                logger.trace("Deserializing String input to [{}].", targetClass)
            }
            val serializer = findSerializer(targetClass)
            return stringFormat.decodeFromString(serializer, input) as T?
        }

        // Target is String - serialize input
        if (targetClass == String::class.java) {
            if (logger.isTraceEnabled) {
                logger.trace("Serializing input [{}] to String.", input)
            }
            val serializer = findSerializer(sourceType) as KSerializer<Any>
            return stringFormat.encodeToString(serializer, input) as T?
        }

        // Input is byte[] - convert to String first, then deserialize
        if (input is ByteArray) {
            if (logger.isTraceEnabled) {
                logger.trace("Converting byte[] input to String, then deserializing to [{}].", targetClass)
            }
            val jsonString = String(input, Charsets.UTF_8)
            val serializer = findSerializer(targetClass)
            return stringFormat.decodeFromString(serializer, jsonString) as T?
        }

        // Target is byte[] - serialize to String first, then convert
        if (targetClass == ByteArray::class.java) {
            if (logger.isTraceEnabled) {
                logger.trace("Serializing input [{}] to String, then converting to byte[].", input)
            }
            val serializer = findSerializer(sourceType) as KSerializer<Any>
            val jsonString = stringFormat.encodeToString(serializer, input)
            return jsonString.toByteArray(Charsets.UTF_8) as T?
        }

        // Input is JsonElement - handle specially
        if (input is JsonElement && stringFormat is Json) {
            if (logger.isTraceEnabled) {
                logger.trace("Decoding JsonElement input to [{}].", targetClass)
            }
            val serializer = findSerializer(targetClass)
            return stringFormat.decodeFromJsonElement(serializer, input) as T?
        }

        // Target is JsonElement - handle specially
        if (targetClass == JsonElement::class.java && stringFormat is Json) {
            if (logger.isTraceEnabled) {
                logger.trace("Encoding input [{}] to JsonElement.", input)
            }
            val serializer = findSerializer(sourceType) as KSerializer<Any>
            return stringFormat.encodeToJsonElement(serializer, input) as T?
        }

        // Object-to-object conversion through JSON
        if (logger.isTraceEnabled) {
            logger.trace(
                "Converting [{}] to [{}] through JSON serialization/deserialization.",
                input, targetClass
            )
        }
        val sourceSerializer = findSerializer(sourceType) as KSerializer<Any>
        val jsonString = stringFormat.encodeToString(sourceSerializer, input)
        val targetSerializer = findSerializer(targetClass)
        return stringFormat.decodeFromString(targetSerializer, jsonString) as T?
    }

    @Suppress("UNCHECKED_CAST")
    @OptIn(InternalSerializationApi::class)
    private fun <T> convertWithBinaryFormat(
        input: Any,
        sourceType: Class<*>,
        targetClass: Class<*>,
        binaryFormat: BinaryFormat
    ): T? {
        // Input is byte[] - deserialize to target
        if (input is ByteArray) {
            if (logger.isTraceEnabled) {
                logger.trace("Deserializing byte[] input to [{}].", targetClass)
            }
            val serializer = findSerializer(targetClass)
            return binaryFormat.decodeFromByteArray(serializer, input) as T?
        }

        // Target is byte[] - serialize input
        if (targetClass == ByteArray::class.java) {
            if (logger.isTraceEnabled) {
                logger.trace("Serializing input [{}] to byte[].", input)
            }
            val serializer = findSerializer(sourceType) as KSerializer<Any>
            return binaryFormat.encodeToByteArray(serializer, input) as T?
        }

        // Object-to-object conversion through binary
        if (logger.isTraceEnabled) {
            logger.trace(
                "Converting [{}] to [{}] through binary serialization/deserialization.",
                input, targetClass
            )
        }
        val sourceSerializer = findSerializer(sourceType) as KSerializer<Any>
        val bytes = binaryFormat.encodeToByteArray(sourceSerializer, input)
        val targetSerializer = findSerializer(targetClass)
        return binaryFormat.decodeFromByteArray(targetSerializer, bytes) as T?
    }

    @OptIn(InternalSerializationApi::class, kotlinx.serialization.ExperimentalSerializationApi::class)
    private fun findSerializer(clazz: Class<*>): KSerializer<Any> {
        @Suppress("UNCHECKED_CAST")
        return serialFormat.serializersModule.getContextual(clazz.kotlin as KClass<Any>)
            ?: (clazz.kotlin as KClass<Any>).serializer()
    }

    private fun isSerializableType(type: Type): Boolean {
        if (type !is Class<*>) return false
        return try {
            findSerializer(type)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun describeTo(@Nonnull descriptor: ComponentDescriptor) {
        descriptor.describeProperty("serialFormat", serialFormat::class.simpleName)
        descriptor.describeProperty("chaining-content-type-converter", chainingConverter)
    }
}
