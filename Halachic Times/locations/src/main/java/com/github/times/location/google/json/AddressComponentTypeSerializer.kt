package com.github.times.location.google.json

import com.google.maps.model.AddressComponentType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
class AddressComponentTypeSerializer : KSerializer<AddressComponentType?> {

    override val descriptor =
        PrimitiveSerialDescriptor("AddressComponentType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): AddressComponentType? {
        val value = decoder.decodeString()
        return AddressComponentType.entries.firstOrNull { it.toCanonicalLiteral().equals(value) }
    }

    override fun serialize(encoder: Encoder, value: AddressComponentType?) {
        if (value == null) {
            encoder.encodeNull()
            return
        }
        encoder.encodeString(value.toCanonicalLiteral())
    }
}
