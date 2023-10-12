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
package com.github.times.location.google;

import com.google.maps.errors.ApiException;
import com.google.maps.internal.ApiResponse;
import com.google.maps.model.GeocodingResult;

/**
 * Geocoder response.
 *
 * @author Moshe Waisberg
 */
public class GeocodingResponse implements ApiResponse<GeocodingResult[]> {

    public String status;
    public String errorMessage;
    public GeocodingResult[] results;

    @Override
    public boolean successful() {
        return "OK".equals(status) || "ZERO_RESULTS".equals(status);
    }

    @Override
    public GeocodingResult[] getResult() {
        return results;
    }

    @Override
    public ApiException getError() {
        if (successful()) {
            return null;
        }
        return ApiException.from(status, errorMessage);
    }
}
