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

import java.awt.geom.Rectangle2D;

/**
 * A rectangular bounding box which is used to describe the bounds of a name.
 *
 * @author moshe.w
 */
public class BoundingBox extends Rectangle2D.Double {

    public BoundingBox(/*@NamedArg("west")*/ double west, /*@NamedArg("north")*/ double north, /*@NamedArg("east")*/ double east, /*@NamedArg("south")*/ double south) {
        super(-west, -south, east + west, north + south);
    }
}
