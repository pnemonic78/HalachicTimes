package com.github.times.location.google.json

import com.google.maps.model.LocationType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class LocationTypeSerializer : KSerializer<LocationType> {

    override val descriptor =
        PrimitiveSerialDescriptor("LocationType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocationType {
        return when (decoder.decodeString()) {
            "APPROXIMATE" -> LocationType.APPROXIMATE
            "GEOMETRIC_CENTER" -> LocationType.GEOMETRIC_CENTER
            "RANGE_INTERPOLATED" -> LocationType.RANGE_INTERPOLATED
            "ROOFTOP" -> LocationType.ROOFTOP
            else -> LocationType.UNKNOWN
        }
    }

    override fun serialize(encoder: Encoder, value: LocationType) = TODO()
}
