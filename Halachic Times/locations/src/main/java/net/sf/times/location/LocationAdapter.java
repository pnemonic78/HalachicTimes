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
package net.sf.times.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Filter;

import net.sf.util.LocaleUtils;
import net.sf.widget.ArrayAdapter;

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
     * Interface definition for a callback to be invoked when an item in this list has been clicked.
     *
     * @author Moshe Waisberg
     */
    public interface OnItemClickListener {

        void onItemClick(ZmanimAddress address);

    }

    /**
     * Interface definition for a callback to be invoked when a "favorite"
     * checkbox in this list has been clicked.
     *
     * @author Moshe Waisberg
     */
    public interface OnFavoriteClickListener {

        void onFavoriteClick(ZmanimAddress address, boolean checked);

    }

    private LocationComparator comparator;
    private LocationsFilter filter;
    private final Collator collator;
    private final Locale locale;
    private OnItemClickListener itemClickListener;
    private OnFavoriteClickListener favoriteClickListener;

    /**
     * Constructs a new adapter.
     *
     * @param context
     *         the context.
     * @param items
     *         the list of addresses' items.
     */
    public LocationAdapter(Context context, List<LocationItem> items) {
        this(context, items, null);
    }

    /**
     * Constructs a new adapter.
     *
     * @param context
     *         the context.
     * @param items
     *         the list of addresses' items.
     * @param itemClickListener
     *         the item click listener.
     */
    public LocationAdapter(Context context, List<LocationItem> items, OnItemClickListener itemClickListener) {
        this(context, items, itemClickListener, null);
    }

    /**
     * Constructs a new adapter.
     *
     * @param context
     *         the context.
     * @param items
     *         the list of addresses' items.
     * @param itemClickListener
     *         the item click listener.
     * @param favoriteClickListener
     *         the favorite click listener.
     */
    public LocationAdapter(Context context, List<LocationItem> items, OnItemClickListener itemClickListener, OnFavoriteClickListener favoriteClickListener) {
        super(R.layout.location, android.R.id.title, items);
        setHasStableIds(false);
        collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        locale = LocaleUtils.getDefaultLocale(context);
        setOnItemClickListener(itemClickListener);
        setOnFavoriteClickListener(favoriteClickListener);
        sortNoNotify();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getAddress().getId();
    }

    @Override
    protected LocationViewHolder createArrayViewHolder(View view, int fieldId) {
        return new LocationViewHolder(view, fieldId, itemClickListener, favoriteClickListener);
    }

    @NonNull
    protected LocationComparator getComparator() {
        if (comparator == null) {
            comparator = new LocationComparator();
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
     * @param comparator
     *         comparator used to sort the objects contained in this adapter.
     */
    protected void sortNoNotify(Comparator<? super LocationItem> comparator) {
        if (originalValues != null) {
            sortNoNotify(originalValues, comparator);
        } else {
            sortNoNotify(objects, comparator);
        }
    }

    /**
     * Sort without notification.
     *
     * @param objects
     *         the list of objects to sort.
     * @param comparator
     *         comparator used to sort the objects contained in this adapter.
     */
    protected void sortNoNotify(List<LocationItem> objects, Comparator<? super LocationItem> comparator) {
        // Removes duplicate locations.
        Set<LocationItem> items = new TreeSet<>(comparator);

        items.addAll(objects);
        objects.clear();
        objects.addAll(items);
    }

    /**
     * Sort.
     */
    public void sort() {
        sortNoNotify();
        notifyDataSetChanged();
    }

    @Override
    public void sort(Comparator<? super LocationItem> comparator) {
        sortNoNotify(comparator);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new LocationsFilter();
        }
        return filter;
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

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (originalValues == null) {
                originalValues = new ArrayList<>(objects);
            }

            final List<LocationItem> values = new ArrayList<>(originalValues);
            final int count = values.size();

            if (constraint == null) {
                results.values = values;
                results.count = values.size();
            } else {
                final Locale locale = LocationAdapter.this.locale;
                final String constraintString = constraint.toString().toLowerCase(locale);

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
            }

            return results;
        }

        protected boolean accept(LocationItem value, String constraint) {
            if (TextUtils.isEmpty(constraint)) {
                return true;
            }

            String valueText = value.getLabelLower();
            CharSequence latitude = value.getFormatLatitude();
            CharSequence longitude = value.getFormatLongitude();

            if (contains(valueText, constraint) || (TextUtils.indexOf(latitude, constraint) >= 0) || (TextUtils.indexOf(longitude, constraint) >= 0)) {
                return true;
            }
            return false;
        }

        /**
         * Does the first string contain the other string?
         *
         * @param s
         *         the source string.
         * @param search
         *         the character sequence to search for.
         * @return {@code true} if {@code s} contains {@code search}.
         */
        private boolean contains(String s, String search) {
            final int len1 = s.length();
            final int len2 = search.length();

            if (len1 < len2)
                return false;

            final Collator collator = LocationAdapter.this.collator;

            if (len1 == len2) {
                if (s.equals(search) || collator.equals(s, search))
                    return true;
                return false;
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
    protected static class LocationItem {

        private final ZmanimAddress address;
        private final CharSequence label;
        private final String labelLower;
        private final CharSequence latitude;
        private final CharSequence longitude;
        private final CharSequence coordinates;

        /**
         * Constructs a new item.
         *
         * @param address
         *         the address.
         */
        public LocationItem(ZmanimAddress address, LocationFormatter formatter) {
            this.address = address;
            this.label = address.getFormatted();
            this.labelLower = label.toString().toLowerCase(address.getLocale());
            this.latitude = formatter.formatLatitude(address.getLatitude());
            this.longitude = formatter.formatLongitude(address.getLongitude());
            this.coordinates = formatter.formatCoordinates(getAddress());
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
    protected class LocationComparator implements Comparator<LocationItem> {

        /** Double subtraction error. */
        private static final double EPSILON = 1e-6;

        private Collator collator;

        /**
         * Constructs a new comparator.
         */
        public LocationComparator() {
            collator = Collator.getInstance();
            collator.setStrength(Collator.PRIMARY);
        }

        @SuppressLint("DefaultLocale")
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
            return (id1 == id2 ? 0 : (id1 < id2 ? -1 : 1));
        }
    }

    /**
     * Set the listener for "item" clicked callbacks.
     *
     * @param listener
     *         the listener.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    /**
     * Set the listener for "favorite" clicked callbacks.
     *
     * @param listener
     *         the listener.
     */
    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteClickListener = listener;
    }

}
