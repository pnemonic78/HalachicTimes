package com.github.times.location.google.json

import com.google.maps.model.AddressType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
class AddressTypeSerializer : KSerializer<AddressType?> {

    override val descriptor = PrimitiveSerialDescriptor("AddressType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): AddressType? {
        val value = decoder.decodeString()
        return AddressType.entries.firstOrNull { it.toCanonicalLiteral().equals(value) }
    }

    override fun serialize(encoder: Encoder, value: AddressType?) {
        if (value == null) {
            encoder.encodeNull()
            return
        }
        encoder.encodeString(value.toCanonicalLiteral())
    }
}
