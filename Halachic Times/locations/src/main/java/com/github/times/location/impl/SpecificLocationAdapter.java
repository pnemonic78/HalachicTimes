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
import android.widget.Filter;

import com.github.times.location.LocationAdapter;
import com.github.times.location.ZmanimAddress;

import java.util.Comparator;
import java.util.List;

/**
 * Location adapter for a specific type of locations.
 *
 * @author Moshe Waisberg
 */
public abstract class SpecificLocationAdapter extends LocationAdapter {

    private SpecificFilter filter;

    public SpecificLocationAdapter(Context context, List<LocationItem> items) {
        this(context, items, (LocationItemListener) null);
    }

    public SpecificLocationAdapter(Context context, List<LocationItem> items, LocationItemListener itemListener) {
        this(context, items, itemListener, null);
    }

    public SpecificLocationAdapter(Context context, List<LocationItem> items, final FilterListener filterListener) {
        this(context, items, null, filterListener);
    }

    public SpecificLocationAdapter(Context context, List<LocationItem> items, LocationItemListener itemListener, final FilterListener filterListener) {
        super(context, items, itemListener);
        getFilter().filter(null, (filterListener == null) ? null : new Filter.FilterListener() {
            @Override
            public void onFilterComplete(int count) {
                filterListener.onFilterComplete(SpecificLocationAdapter.this, count);
            }
        });
    }

    @Override
    protected ArrayFilter createFilter() {
        return new SpecificFilter();
    }

    /**
     * Is the address specific to this adapter?
     *
     * @param item
     *         the location item.
     * @return {@code true} to include the location.
     */
    protected abstract boolean isSpecific(LocationItem item);

    protected class SpecificFilter extends LocationsFilter {
        @Override
        protected boolean accept(LocationItem value, String constraint) {
            return isSpecific(value) && super.accept(value, constraint);
        }
    }

    @Override
    public void notifyItemChanged(ZmanimAddress address) {
        synchronized (lock) {
            final int count = getItemCount();
            LocationItem item;
            int position = -1;
            for (int i = 0; i < count; i++) {
                item = getItem(i);
                if (item.getAddress().equals(address)) {
                    position = i;
                    break;
                }
            }

            if (position >= 0) {
                item = getItem(position);
                if (isSpecific(item)) {
                    notifyItemChanged(position);
                } else {
                    // Hide the item.
                    if (objectsFiltered) {
                        objects.remove(position);
                        notifyItemRemoved(position);
                    }
                }
            } else if (objectsFiltered) {
                final int size = originalValues.size();
                for (int i = 0; i < size; i++) {
                    item = originalValues.get(i);
                    if (item.getAddress().equals(address)) {
                        position = i;
                        break;
                    }
                }
                if (position >= 0) {
                    item = originalValues.get(position);
                    // Find the sorted position.
                    int positionInsert = count;
                    Comparator<LocationItem> comparator = getComparator();
                    for (int i = 0; i < count; i++) {
                        if (comparator.compare(item, getItem(i)) < 0) {
                            positionInsert = i;
                            break;
                        }
                    }
                    objects.add(positionInsert, item);
                    notifyItemInserted(positionInsert);
                } else {
                    // Throw error: item not found!
                }
            }
        }
    }
}
