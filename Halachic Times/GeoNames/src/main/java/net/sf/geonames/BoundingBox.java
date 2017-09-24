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
package net.sf.geonames;

import java.awt.geom.Rectangle2D;

/**
 * A rectangular bounding box which is used to describe the bounds of a name.
 *
 * @author Moshe Waisberg
 */
public class BoundingBox extends Rectangle2D.Double {

    public BoundingBox(/*@NamedArg("west")*/ double west, /*@NamedArg("north")*/ double north, /*@NamedArg("east")*/ double east, /*@NamedArg("south")*/ double south) {
        super(-west, -south, east + west, north + south);
    }
}
