/*
 * Copyright 2008 Marc Wick, geonames.org
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
 * Enumeration for the GeoNames feature classes A,H,L,P,R,S,T,U,V
 * @see https://www.geonames.org/export/codes.html
 * @author marc
 */
@Serializable
enum class FeatureClass {
    /**
     * Administrative Boundary Features
     */
    @SerialName("A")
    A,

    /**
     * Hydrographic Features
     */
    @SerialName("H")
    H,

    /**
     * Area Features
     */
    @SerialName("L")
    L,

    /**
     * Populated Place Features
     */
    @SerialName("P")
    P,

    /**
     * Road / Railroad Features
     */
    @SerialName("R")
    R,

    /**
     * Spot Features
     */
    @SerialName("S")
    S,

    /**
     * Hypsographic Features
     */
    @SerialName("T")
    T,

    /**
     * Undersea Features
     */
    @SerialName("U")
    U,

    /**
     * Vegetation Features
     */
    @SerialName("V")
    V;

    companion object {
        fun fromValue(value: String?): FeatureClass? {
            return if (value.isNullOrEmpty()) null else valueOf(value)
        }
    }
}
