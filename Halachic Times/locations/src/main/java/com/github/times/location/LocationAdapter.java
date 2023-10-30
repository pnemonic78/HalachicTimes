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
package com.github.times.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.util.LocaleUtils;
import com.github.widget.ArrayAdapter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Location adapter.
 *
 * @author Moshe Waisberg
 */
public class LocationAdapter extends ArrayAdapter<LocationAdapter.LocationItem, LocationViewHolder> {

    /**
     * Interface definition for callbacks to be invoked when an item in this list has been clicked.
     *
     * @author Moshe Waisberg
     */
    public interface LocationItemListener {

        /**
         * Callback to be invoked when an item in this list has been clicked.
         */
        void onItemClick(LocationItem item);

        /**
         * Callback to be invoked when a "favorite" checkbox in this list has been clicked.
         **/
        void onFavoriteClick(LocationItem item, boolean checked);

        /**
         * Callback to be invoked when an item in this list has been swiped.
         **/
        void onItemSwipe(LocationItem item);

    }

    /**
     * Listener used to receive a notification upon completion of a filtering operation.
     */
    public interface FilterListener {
        /**
         * <p>Notifies the end of a filtering operation.</p>
         *
         * @param adapter the adapter.
         * @param count   the number of values computed by the filter
         */
        void onFilterComplete(LocationAdapter adapter, int count);
    }

    private LocationComparator comparator;
    private final Collator collator;
    private final Locale locale;
    private LocationItemListener listener;

    /**
     * Constructs a new adapter.
     *
     * @param context the context.
     * @param items   the list of addresses' items.
     */
    public LocationAdapter(Context context, List<LocationItem> items) {
        this(context, items, null);
    }

    /**
     * Constructs a new adapter.
     *
     * @param context  the context.
     * @param items    the list of addresses' items.
     * @param listener the item listener.
     */
    public LocationAdapter(Context context, List<LocationItem> items, LocationItemListener listener) {
        super(R.layout.location, R.id.title, items);
        setHasStableIds(false);
        collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        locale = LocaleUtils.getDefaultLocale(context);
        setOnItemListener(listener);
        sortNoNotify();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getAddress().getId();
    }

    @NonNull
    @Override
    protected LocationViewHolder createArrayViewHolder(@NonNull View view, int fieldId) {
        return new LocationViewHolder(view, fieldId, listener);
    }

    @NonNull
    protected LocationComparator getComparator() {
        LocationComparator comparator = this.comparator;
        if (comparator == null) {
            comparator = new LocationComparator();
            this.comparator = comparator;
        }
        return comparator;
    }

    /**
     * Sort without notification.
     */
    protected void sortNoNotify() {
        sortNoNotify(getComparator());
    }

    /**
     * Sort without notification.
     *
     * @param comparator comparator used to sort the objects contained in this adapter.
     */
    protected void sortNoNotify(@NonNull Comparator<? super LocationItem> comparator) {
        if (objectsFiltered) {
            sortNoNotify(originalValues, comparator);
        } else {
            sortNoNotify(objects, comparator);
        }
    }

    /**
     * Sort without notification.
     *
     * @param objects    the list of objects to sort.
     * @param comparator comparator used to sort the objects contained in this adapter.
     */
    protected void sortNoNotify(@NonNull List<LocationItem> objects, @NonNull Comparator<? super LocationItem> comparator) {
        // Removes duplicate locations.
        Set<LocationItem> items = new TreeSet<>(comparator);

        items.addAll(objects);
        objects.clear();
        objects.addAll(items);
    }

    /**
     * Sort.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void sort() {
        sortNoNotify();
        notifyDataSetChanged();
    }

    @Override
    @SuppressLint("NotifyDataSetChanged")
    public void sort(@NonNull Comparator<? super LocationItem> comparator) {
        sortNoNotify(comparator);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    protected ArrayFilter createFilter() {
        return new LocationsFilter();
    }

    public void notifyItemChanged(ZmanimAddress address) {
        synchronized (lock) {
            final int size = getItemCount();
            LocationItem item;
            for (int i = 0; i < size; i++) {
                item = getItem(i);
                if (item.address.equals(address)) {
                    notifyItemChanged(i);
                    return;
                }
            }
        }
    }

    public void remove(ZmanimAddress address) {
        synchronized (lock) {
            final int size = getItemCount();
            LocationItem item;
            for (int i = 0; i < size; i++) {
                item = getItem(i);
                if (item.address.equals(address)) {
                    remove(item);
                    return;
                }
            }
        }
    }

    public void delete(ZmanimAddress address) {
        synchronized (lock) {
            final int size = getItemCount();
            LocationItem item;
            for (int i = 0; i < size; i++) {
                item = getItem(i);
                if (item.address.equals(address)) {
                    delete(item);
                    return;
                }
            }
        }
    }

    /**
     * Filter the list of locations to match cities' names that contain the constraint.
     *
     * @author Moshe Waisberg
     */
    protected class LocationsFilter extends ArrayFilter {

        /**
         * Constructs a new filter.
         */
        public LocationsFilter() {
        }

        @NonNull
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (!objectsFiltered) {
                synchronized (lock) {
                    objectsFiltered = true;
                    originalValues.clear();
                    originalValues.addAll(objects);
                }
            }

            final List<LocationItem> values = new ArrayList<>(originalValues);
            final int count = values.size();

            final Locale locale = LocationAdapter.this.locale;
            final String constraintString = TextUtils.isEmpty(constraint) ? null : constraint.toString().toLowerCase(locale);

            final List<LocationItem> newValues = new ArrayList<>();
            LocationItem value;

            for (int i = 0; i < count; i++) {
                value = values.get(i);
                if (accept(value, constraintString)) {
                    newValues.add(value);
                }
            }

            results.values = newValues;
            results.count = newValues.size();

            return results;
        }

        protected boolean accept(@NonNull LocationItem value, @Nullable String constraint) {
            if (TextUtils.isEmpty(constraint)) {
                return true;
            }

            String valueText = value.getLabelLower();
            CharSequence latitude = value.getFormatLatitude();
            CharSequence longitude = value.getFormatLongitude();

            return contains(valueText, constraint)
                || (TextUtils.indexOf(latitude, constraint) >= 0)
                || (TextUtils.indexOf(longitude, constraint) >= 0);
        }

        /**
         * Does the first string contain the other string?
         *
         * @param s      the source string.
         * @param search the character sequence to search for.
         * @return {@code true} if {@code s} contains {@code search}.
         */
        private boolean contains(@NonNull String s, @Nullable String search) {
            if (search == null) return true;
            final int len1 = s.length();
            final int len2 = search.length();

            if (len1 < len2)
                return false;

            final Collator collator = LocationAdapter.this.collator;

            if (len1 == len2) {
                return s.equals(search) || collator.equals(s, search);
            }

            if (s.contains(search))
                return true;

            // Let's do a "Collator.contains"
            String lhs;
            String rhs;
            int dLen = len1 - len2;
            String concat;
            for (int i = 0; i < dLen; i++) {
                lhs = s.substring(0, i);
                rhs = s.substring(len2 + i);
                concat = lhs + search + rhs;
                if (collator.equals(s, concat))
                    return true;
            }

            return false;
        }
    }

    /**
     * Location item.
     *
     * @author Moshe Waisberg
     */
    /*FIXME protected*/public static class LocationItem {

        private final ZmanimAddress address;
        private final CharSequence label;
        private final String labelLower;
        private final CharSequence latitude;
        private final CharSequence longitude;
        private final CharSequence coordinates;

        /**
         * Constructs a new item.
         *
         * @param address the address.
         */
        public LocationItem(ZmanimAddress address, LocationFormatter formatter) {
            this.address = address;
            this.label = address.getFormatted();
            this.labelLower = label.toString().toLowerCase(address.getLocale());
            this.latitude = formatter.formatLatitude(address.getLatitude());
            this.longitude = formatter.formatLongitude(address.getLongitude());
            this.coordinates = formatter.formatCoordinates(address.getLatitude(), address.getLongitude(), Double.NaN);
        }

        /**
         * Get the source address.
         *
         * @return the address.
         */
        public ZmanimAddress getAddress() {
            return address;
        }

        /**
         * Get the label.
         *
         * @return the label.
         */
        public CharSequence getLabel() {
            return label;
        }

        /**
         * Get the label in lower casing.
         *
         * @return the label.
         */
        public String getLabelLower() {
            return labelLower;
        }

        /**
         * Get the formatted latitude.
         *
         * @return the latitude.
         */
        public CharSequence getFormatLatitude() {
            return latitude;
        }

        /**
         * Get the formatted longitude.
         *
         * @return the longitude.
         */
        public CharSequence getFormatLongitude() {
            return longitude;
        }

        /**
         * Get the formatted coordinates.
         *
         * @return the coordinates.
         */
        public CharSequence getCoordinates() {
            return coordinates;
        }

        /**
         * Get the address id.
         *
         * @return the id.
         */
        public long getId() {
            return getAddress().getId();
        }

        /**
         * Is location a favourite?
         *
         * @return {@code true} if a favourite.
         */
        public boolean isFavorite() {
            return getAddress().isFavorite();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (o instanceof LocationItem)
                return getAddress().equals(((LocationItem) o).getAddress());
            if (o instanceof ZmanimAddress)
                return getAddress().equals(o);
            return super.equals(o);
        }

        @Override
        public String toString() {
            return getAddress().toString();
        }
    }

    /**
     * Compare two locations by their locations, then by their names, then their IDs.
     *
     * @author Moshe Waisberg
     */
    protected static class LocationComparator implements Comparator<LocationItem> {

        /**
         * Double subtraction error.
         */
        private static final double EPSILON = 1e-6;

        private Collator collator;

        /**
         * Constructs a new comparator.
         */
        public LocationComparator() {
            collator = Collator.getInstance();
            collator.setStrength(Collator.PRIMARY);
        }

        @Override
        public int compare(LocationItem item1, LocationItem item2) {
            ZmanimAddress addr1 = item1.getAddress();
            ZmanimAddress addr2 = item2.getAddress();

            // Sort first by name.
            String format1 = item1.getLabelLower();
            String format2 = item2.getLabelLower();
            int c = collator.compare(format1, format2);
            if (c != 0)
                return c;

            // Is same location?
            double lat1 = addr1.getLatitude();
            double lat2 = addr2.getLatitude();
            double latD = lat1 - lat2;
            double lng1 = addr1.getLongitude();
            double lng2 = addr2.getLongitude();
            double lngD = lng1 - lng2;
            if (latD >= EPSILON)
                return 1;
            if (latD <= -EPSILON)
                return -1;
            if (lngD >= EPSILON)
                return 1;
            if (lngD < -EPSILON)
                return -1;

            // Then sort by id. Positive id is more important.
            long id1 = addr1.getId();
            long id2 = addr2.getId();
            return Long.compare(id1, id2);
        }
    }

    /**
     * Set the listener for "item" clicked callbacks.
     *
     * @param listener the listener.
     */
    public void setOnItemListener(LocationItemListener listener) {
        this.listener = listener;
    }

}
