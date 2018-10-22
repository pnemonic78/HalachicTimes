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
package com.github.times.location.bing;

import com.google.gson.annotations.SerializedName;

/**
 * Address object for Bing address JSON response.
 *
 * @author Moshe Waisberg
 */
class BingAddress {

    @SerializedName("addressLine")
    public String addressLine;
    @SerializedName("adminDistrict")
    public String adminDistrict;
    @SerializedName("adminDistrict2")
    public String adminDistrict2;
    @SerializedName("countryRegion")
    public String countryRegion;
    @SerializedName("formattedAddress")
    public String formattedAddress;
    @SerializedName("locality")
    public String locality;
    @SerializedName("postalCode")
    public String postalCode;

}
