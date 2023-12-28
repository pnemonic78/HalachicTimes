package com.github.times.location.google.json

import com.google.maps.model.ElevationResult
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

class ElevationResultSerializer : KSerializer<ElevationResult> {

    private val latLngSerializer = LatLngSerializer()

    override val descriptor = buildClassSerialDescriptor("ElevationResult") {
        element("elevation", Double.serializer().descriptor)
        element("location", latLngSerializer.descriptor)
        element("resolution", Double.serializer().descriptor)
    }

    override fun deserialize(decoder: Decoder): ElevationResult {
        val result = ElevationResult()
        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> result.elevation = decodeDoubleElement(descriptor, 0)

                    1 -> result.location =
                        decodeSerializableElement(descriptor, 1, latLngSerializer)

                    2 -> result.resolution = decodeDoubleElement(descriptor, 2)

                    CompositeDecoder.DECODE_DONE -> break

                    else -> error("Unexpected index: $index")
                }
            }
        }
        return result
    }

    override fun serialize(encoder: Encoder, value: ElevationResult) {
        encoder.encodeStructure(descriptor) {
            encodeDoubleElement(descriptor, 0, value.elevation)
            encodeSerializableElement(descriptor, 1, latLngSerializer, value.location)
            encodeDoubleElement(descriptor, 2, value.resolution)
        }
    }
}