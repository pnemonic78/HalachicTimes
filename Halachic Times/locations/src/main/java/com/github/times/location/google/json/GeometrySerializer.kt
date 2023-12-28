package com.github.times.location.google.json

import com.google.maps.model.Geometry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure

@OptIn(ExperimentalSerializationApi::class)
class GeometrySerializer : KSerializer<Geometry> {

    private val boundsSerializer = BoundsSerializer()
    private val latLngSerializer = LatLngSerializer()
    private val locationTypeSerializer = LocationTypeSerializer()

    override val descriptor = buildClassSerialDescriptor("Geometry") {
        element("bounds", boundsSerializer.descriptor)
        element("location", latLngSerializer.descriptor)
        element("location_type", locationTypeSerializer.descriptor)
        element("viewport", boundsSerializer.descriptor)
    }

    override fun deserialize(decoder: Decoder): Geometry {
        val result = Geometry()
        decoder.decodeStructure(descriptor) {
            if (decodeSequentially()) {
                result.bounds = decodeSerializableElement(descriptor, 0, boundsSerializer)
                result.location = decodeSerializableElement(descriptor, 1, latLngSerializer)
                result.locationType = decodeSerializableElement(descriptor, 2, locationTypeSerializer)
                result.viewport = decodeSerializableElement(descriptor, 3, boundsSerializer)
            } else while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> result.bounds = decodeSerializableElement(descriptor, 0, boundsSerializer)
                    1 -> result.location = decodeSerializableElement(descriptor, 1, latLngSerializer)
                    2 -> result.locationType = decodeSerializableElement(descriptor, 2, locationTypeSerializer)
                    3 -> result.viewport = decodeSerializableElement(descriptor, 3, boundsSerializer)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
        }
        return result
    }

    override fun serialize(encoder: Encoder, value: Geometry) = TODO()

}
