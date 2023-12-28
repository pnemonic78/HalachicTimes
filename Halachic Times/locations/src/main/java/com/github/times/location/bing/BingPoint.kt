/*
 * Copyright 2012, Moshe Waisberg
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
 */
package com.github.times.location.bing

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Point object for Bing address JSON response.
 *
 * @author Moshe Waisberg
 */
@Serializable
class BingPoint {
    @SerialName("type")
    var type: String? = null

    @SerialName("coordinates")
    var coordinates: List<Double>? = null

    @SerialName("calculationMethod")
    var calculationMethod: String? = null

    @SerialName("usageTypes")
    var usageTypes: List<String>? = null
}