/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.geonames;

import java.util.Comparator;

/**
 * Location comparator.
 *
 * @author Moshe Waisberg
 */
public class LocationComparator implements Comparator<GeoName> {

    public LocationComparator() {
        super();
    }

    @Override
    public int compare(GeoName geo0, GeoName geo1) {
        // West < East
        double lng0 = geo0.getLongitude();
        double lng1 = geo1.getLongitude();
        if (lng0 > lng1)
            return +1;
        if (lng0 < lng1)
            return -1;

        // North < South
        double lat0 = geo0.getLatitude();
        double lat1 = geo1.getLatitude();
        if (lat0 > lat1)
            return +1;
        if (lat0 < lat1)
            return -1;

        int ele0 = geo0.getGrossElevation();
        int ele1 = geo1.getGrossElevation();
        int c = ele0 - ele1;
        if (c != 0)
            return c;

        String name0 = geo0.getName();
        String name1 = geo1.getName();
        c = name0.compareTo(name1);
        if (c != 0)
            return c;

        long id0 = geo0.getGeoNameId();
        long id1 = geo1.getGeoNameId();
        if (id0 > id1)
            return +1;
        if (id0 < id1)
            return -1;
        return 0;
    }

}
