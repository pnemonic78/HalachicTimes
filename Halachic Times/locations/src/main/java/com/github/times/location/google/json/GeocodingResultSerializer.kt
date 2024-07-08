package com.github.times.location.google.json

import com.github.json.NullableStringSerializer
import com.google.maps.model.AddressComponent
import com.google.maps.model.AddressType
import com.google.maps.model.GeocodingResult
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
class GeocodingResultSerializer : KSerializer<GeocodingResult> {

    private val addressComponentSerializer = AddressComponentSerializer()
    private val addressComponentsSerializer =
        ArraySerializer<AddressComponent, AddressComponent?>(addressComponentSerializer)
    private val addressTypeSerializer = AddressTypeSerializer()
    private val formattedAddressesSerializer =
        ArraySerializer<String, String?>(NullableStringSerializer)
    private val geometrySerializer = GeometrySerializer()
    private val plusCodeSerializer = PlusCodeSerializer()
    private val typesSerializer = ArraySerializer<AddressType, AddressType?>(addressTypeSerializer)

    override val descriptor = buildClassSerialDescriptor("GeocodingResult") {
        element("address_components", addressComponentsSerializer.descriptor)
        element("formatted_address", String.serializer().descriptor)
        element("geometry", geometrySerializer.descriptor)
        element("partial_match", Boolean.serializer().descriptor)
        element("place_id", String.serializer().descriptor)
        element("plus_code", plusCodeSerializer.descriptor)
        element("postcode_localities", formattedAddressesSerializer.descriptor)
        element("types", typesSerializer.descriptor)
    }

    override fun deserialize(decoder: Decoder): GeocodingResult {
        val result = GeocodingResult()
        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> result.addressComponents = decodeSerializableElement(descriptor, 0, addressComponentsSerializer)
                    1 -> result.formattedAddress = decodeStringElement(descriptor, 1)
                    2 -> result.geometry = decodeSerializableElement(descriptor, 2, geometrySerializer)
                    3 -> result.partialMatch = decodeBooleanElement(descriptor, 3)
                    4 -> result.placeId = decodeStringElement(descriptor, 4)
                    5 -> result.plusCode = decodeSerializableElement(descriptor, 5, plusCodeSerializer)
                    6 -> result.postcodeLocalities = decodeSerializableElement(descriptor, 6, formattedAddressesSerializer)
                    7 -> result.types = decodeSerializableElement(descriptor, 7, typesSerializer)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
        }
        return result
    }

    override fun serialize(encoder: Encoder, value: GeocodingResult) = TODO()
}