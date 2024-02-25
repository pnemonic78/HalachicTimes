package org.geonames

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BoundingBox(
    @SerialName("west")
    var west: Double = 0.0,

    @SerialName("east")
    var east: Double = 0.0,

    @SerialName("south")
    var south: Double = 0.0,

    @SerialName("north")
    var north: Double = 0.0,

    @SerialName("accuracyLevel")
    var accuracyLevel: Double = 0.0
)