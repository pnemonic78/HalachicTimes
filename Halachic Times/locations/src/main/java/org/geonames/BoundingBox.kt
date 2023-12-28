/*
 * Copyright 2012 Marc Wick, geonames.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.geonames

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author Marc Wick
 * @since 15.08.2012
 */
@Serializable
class BoundingBox(
    /** the west */
    @SerialName("west")
    var west: Double,
    /** the east */
    @SerialName("east")
    var east: Double,
    /** the south */
    @SerialName("south")
    var south: Double,
    /** the north */
    @SerialName("north")
    var north: Double
)