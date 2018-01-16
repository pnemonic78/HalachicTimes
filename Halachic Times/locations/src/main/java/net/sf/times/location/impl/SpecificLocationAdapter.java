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

import net.sf.times.location.LocationAdapter;
import net.sf.times.location.ZmanimAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * Location adapter for specific type of locations.
 *
 * @author Moshe Waisberg
 */
public abstract class SpecificLocationAdapter extends LocationAdapter {

    private final List<LocationItem> specific = new ArrayList<>();

    public SpecificLocationAdapter(Context context, List<LocationItem> items) {
        super(context, items);
        populateSpecific();
    }

    private void populateSpecific() {
        specific.clear();

        ZmanimAddress address;
        for (LocationItem item : objects) {
            address = item.getAddress();
            if (isSpecific(address))
                specific.add(item);
        }
    }

    /**
     * Is the address specific to this adapter?
     *
     * @param address
     *         the address.
     * @return {@code true} to include the address.
     */
    protected abstract boolean isSpecific(ZmanimAddress address);

    @Override
    public int getCount() {
        return specific.size();
    }

    @Override
    protected LocationItem getLocationItem(int position) {
        return specific.get(position);
    }

    @Override
    public int getPosition(LocationItem object) {
        final int size = specific.size();
        LocationItem item;
        for (int i = 0; i < size; i++) {
            item = specific.get(i);
            if (item.equals(object))
                return i;
        }
        return super.getPosition(object);
    }

    @Override
    public void notifyDataSetChanged() {
        populateSpecific();
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        populateSpecific();
        super.notifyDataSetInvalidated();
    }

}
