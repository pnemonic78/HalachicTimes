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
package com.github.geonames;

import static com.github.geonames.Features.FEATURE_P;

/**
 * Name filter for capital cities.
 *
 * @author Moshe Waisberg
 */
public class CityFilter implements NameFilter {

    @Override
    public boolean accept(GeoNamesToponym name) {
        return FEATURE_P.equals(name.getFeatureClass());
    }

    @Override
    public void replaceLocation(GeoNamesToponym name) {
    }
}
