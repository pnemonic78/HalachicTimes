package com.github.times.location.google.json

import com.google.maps.model.AddressComponent
import com.google.maps.model.AddressComponentType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure

@OptIn(ExperimentalSerializationApi::class)
class AddressComponentSerializer : KSerializer<AddressComponent?> {

    private val typeSerializer = AddressComponentTypeSerializer()
    private val typesSerializer =
        ArraySerializer<AddressComponentType, AddressComponentType?>(typeSerializer)

    override val descriptor = buildClassSerialDescriptor("AddressComponent") {
        element("long_name", String.serializer().descriptor)
        element("short_name", String.serializer().descriptor)
        element("types", typesSerializer.descriptor)
    }

    override fun deserialize(decoder: Decoder): AddressComponent? {
        if (decoder.decodeNotNullMark()) {
            val result = AddressComponent()
            decoder.decodeStructure(descriptor) {
                while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        0 -> result.longName = decodeStringElement(descriptor, 0)
                        1 -> result.shortName = decodeStringElement(descriptor, 1)
                        2 -> result.types =
                            decodeSerializableElement(descriptor, 2, typesSerializer)

                        CompositeDecoder.DECODE_DONE -> break
                        else -> error("Unexpected index: $index")
                    }
                }
            }
            return result
        }
        return decoder.decodeNull()
    }

    override fun serialize(encoder: Encoder, value: AddressComponent?) = Unit
}
