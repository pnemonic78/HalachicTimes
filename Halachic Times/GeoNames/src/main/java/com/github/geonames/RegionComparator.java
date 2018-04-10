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

import java.util.Comparator;

/**
 * Region comparator.
 *
 * @author Moshe Waisberg
 */
public class RegionComparator implements Comparator<CountryRegion> {

    public RegionComparator() {
        super();
    }

    @Override
    public int compare(CountryRegion region0, CountryRegion region1) {
        String name0 = region0.getCountryCode();
        String name1 = region1.getCountryCode();
        int name = name0.compareTo(name1);
        if (name != 0)
            return name;

        int npoints0 = region0.npoints;
        int npoints1 = region1.npoints;
        int npoints = npoints0 - npoints1;
        if (npoints != 0)
            return npoints;

        int x0;
        int x1;
        int x;
        for (int i = 0; i < npoints0; i++) {
            x0 = region0.xpoints[i];
            x1 = region1.xpoints[i];
            x = x0 - x1;
            if (x != 0)
                return npoints;
        }

        int y0;
        int y1;
        int y;
        for (int i = 0; i < npoints0; i++) {
            y0 = region0.ypoints[i];
            y1 = region1.ypoints[i];
            y = y0 - y1;
            if (y != 0)
                return npoints;
        }

        return 0;
    }

}
