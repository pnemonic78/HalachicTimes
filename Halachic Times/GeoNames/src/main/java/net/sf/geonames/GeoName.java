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

import java.util.HashMap;
import java.util.Map;

/**
 * GeoName POJO.
 *
 * @author Moshe Waisberg
 */
public class GeoName extends GeoNameRecord {

    private final Map<String, AlternateName> alternateNamesMap = new HashMap<>();

    @Override
    public void setName(String name) {
        super.setName(name);
        if ((name != null) && alternateNamesMap.isEmpty()) {
            AlternateName alternateName = new AlternateName("en", name);
            alternateNamesMap.put(alternateName.getLanguage(), alternateName);
        }
    }

    public Map<String, AlternateName> getAlternateNamesMap() {
        return alternateNamesMap;
    }

    public void setAlternateNames(Map<String, AlternateName> alternateNames) {
        this.alternateNamesMap.clear();
        if (alternateNamesMap != null) {
            this.alternateNamesMap.putAll(alternateNamesMap);
        }
    }
}
