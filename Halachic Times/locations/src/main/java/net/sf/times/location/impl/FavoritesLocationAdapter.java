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
package net.sf.times.location.impl;

import android.content.Context;

import java.util.List;

/**
 * Location adapter for "favorite" locations.
 *
 * @author Moshe Waisberg
 */
public class FavoritesLocationAdapter extends SpecificLocationAdapter {

    public FavoritesLocationAdapter(Context context, List<LocationItem> items) {
        super(context, items);
    }

    public FavoritesLocationAdapter(Context context, List<LocationItem> items, LocationItemListener itemListener) {
        super(context, items, itemListener);
    }

    public FavoritesLocationAdapter(Context context, List<LocationItem> items, FilterListener filterListener) {
        super(context, items, filterListener);
    }

    public FavoritesLocationAdapter(Context context, List<LocationItem> items, LocationItemListener itemListener, FilterListener filterListener) {
        super(context, items, itemListener, filterListener);
    }

    @Override
    protected boolean isSpecific(LocationItem item) {
        return item.isFavorite();
    }
}
