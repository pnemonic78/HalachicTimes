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

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Root object for Bing address JSON response.
 *
 * @author Moshe Waisberg
 */
class BingAddressResponse {

    public static final int STATUS_OK = 200;

    @SerializedName("authenticationResultCode")
    public String authenticationResultCode;
    @SerializedName("brandLogoUri")
    public Uri brandLogoUri;
    @SerializedName("copyright")
    public String copyright;
    @SerializedName("resourceSets")
    public List<ResourceSet> resourceSets;
    @SerializedName("statusCode")
    public int statusCode;
    @SerializedName("statusDescription")
    public String statusDescription;
    @SerializedName("traceId")
    public String traceId;

    public static class ResourceSet {

        @SerializedName("estimatedTotal")
        public int estimatedTotal;
        @SerializedName("resources")
        public List<BingResource> resources;

    }
}
