package com.github.times.location.google.json

import com.google.maps.model.LatLng
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
class LatLngSerializer : KSerializer<LatLng> {

    override val descriptor = buildClassSerialDescriptor("LatLng") {
        element("lat", Double.serializer().descriptor)
        element("lng", Double.serializer().descriptor)
    }

    override fun deserialize(decoder: Decoder): LatLng {
        val result = LatLng()
        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> result.lat = decodeDoubleElement(descriptor, 0)
                    1 -> result.lng = decodeDoubleElement(descriptor, 1)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
        }
        return result
    }

    override fun serialize(encoder: Encoder, value: LatLng) {
        encoder.encodeStructure(descriptor) {
            encodeDoubleElement(descriptor, 0, value.lat)
            encodeDoubleElement(descriptor, 1, value.lng)
        }
    }
}