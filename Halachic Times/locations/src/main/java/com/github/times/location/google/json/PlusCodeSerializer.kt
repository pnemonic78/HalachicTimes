package com.github.times.location.google.json

import com.google.maps.model.PlusCode
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

@OptIn(ExperimentalSerializationApi::class)
class PlusCodeSerializer : KSerializer<PlusCode> {
    override val descriptor = buildClassSerialDescriptor("PlusCode") {
        element("compound_code", String.serializer().descriptor)
        element("global_code", String.serializer().descriptor)
    }

    override fun deserialize(decoder: Decoder): PlusCode {
        val result = PlusCode()
        decoder.decodeStructure(descriptor) {
            if (decodeSequentially()) {
                result.compoundCode = decodeStringElement(descriptor, 0)
                result.globalCode = decodeStringElement(descriptor, 1)
            } else while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> result.compoundCode = decodeStringElement(descriptor, 0)
                    1 -> result.globalCode = decodeStringElement(descriptor, 1)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
        }
        return result
    }

    override fun serialize(encoder: Encoder, value: PlusCode) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.compoundCode)
            encodeStringElement(descriptor, 1, value.globalCode)
        }
    }
}
