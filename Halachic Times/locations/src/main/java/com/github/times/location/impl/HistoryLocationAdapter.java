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
package com.github.times.location.impl;

import android.content.Context;

import java.util.List;

/**
 * Location adapter for locations the user has "previously visited".
 *
 * @author Moshe Waisberg
 */
public class HistoryLocationAdapter extends SpecificLocationAdapter {

    public HistoryLocationAdapter(Context context, List<LocationItem> items) {
        super(context, items);
    }

    public HistoryLocationAdapter(Context context, List<LocationItem> items, LocationItemListener itemListener) {
        super(context, items, itemListener);
    }

    public HistoryLocationAdapter(Context context, List<LocationItem> items, FilterListener filterListener) {
        super(context, items, filterListener);
    }

    public HistoryLocationAdapter(Context context, List<LocationItem> items, LocationItemListener itemListener, FilterListener filterListener) {
        super(context, items, itemListener, filterListener);
    }

    @Override
    protected boolean isSpecific(LocationItem item) {
        //FIXME should only filter on items that user ever clicked (e.g. user only ever clicked the favorite star).
        return item.getId() > 0L;
    }

}
