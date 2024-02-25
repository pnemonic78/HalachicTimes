package org.geonames.json

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

open class DateSerializer(private val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)) : KSerializer<Date?> {
    init {
        formatter.timeZone = TimeZone.getTimeZone("UTC")
    }

    override val descriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): Date? {
        if (decoder.decodeNotNullMark()) {
            val value = decoder.decodeString()
            return formatter.parse(value)
        }
        return decoder.decodeNull()
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Date?) {
        if (value == null) {
            encoder.encodeNull()
            return
        }
        encoder.encodeString(formatter.format(value.time))
    }
}