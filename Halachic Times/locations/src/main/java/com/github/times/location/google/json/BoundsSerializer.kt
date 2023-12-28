package com.github.times.location.google.json

import com.google.maps.model.Bounds
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure

@OptIn(ExperimentalSerializationApi::class)
class BoundsSerializer : KSerializer<Bounds> {

    private val latLngSerializer = LatLngSerializer()

    override val descriptor = buildClassSerialDescriptor("Bounds") {
        element("northeast", latLngSerializer.descriptor)
        element("southwest", latLngSerializer.descriptor)
    }

    override fun deserialize(decoder: Decoder): Bounds {
        val result = Bounds()
        decoder.decodeStructure(descriptor) {
            if (decodeSequentially()) {
                result.northeast = decodeSerializableElement(descriptor, 0, latLngSerializer)
                result.southwest = decodeSerializableElement(descriptor, 1, latLngSerializer)
            } else while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> result.northeast =
                        decodeSerializableElement(descriptor, 0, latLngSerializer)

                    1 -> result.southwest =
                        decodeSerializableElement(descriptor, 1, latLngSerializer)

                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
        }
        return result
    }

    override fun serialize(encoder: Encoder, value: Bounds) = TODO()
}