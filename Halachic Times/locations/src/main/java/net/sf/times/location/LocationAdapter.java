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
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
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
public class LocationAdapter extends ArrayAdapter<LocationAdapter.LocationItem, LocationViewHolder> implements OnClickListener {

    private LocationComparator comparator;
    private LocationsFilter filter;
    private final Collator collator;
    private final Locale locale;
    private OnFavoriteClickListener onFavoriteClickListener;

    /**
     * Constructs a new adapter.
     *
     * @param context
     *         the context.
     * @param items
     *         the list of addresses' items.
     */
    public LocationAdapter(Context context, List<LocationItem> items) {
        super(R.layout.location, android.R.id.title, items);
        setHasStableIds(false);
        collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        locale = LocaleUtils.getDefaultLocale(context);
        sortNoNotify();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getAddress().getId();
    }

    @Override
    protected LocationViewHolder createArrayViewHolder(View view, int fieldId) {
        LocationViewHolder viewHolder = new LocationViewHolder(view, fieldId);
        viewHolder.favorite.setOnClickListener(this);
        return viewHolder;
    }

    /**
     * Sort without notification.
     */
    protected void sortNoNotify() {
        if (comparator == null) {
            comparator = new LocationComparator();
        }
        sortNoNotify(comparator);
    }

    /**
     * Sort without notification.
     *
     * @param comparator
     *         comparator used to sort the objects contained in this adapter.
     */
    protected void sortNoNotify(Comparator<? super LocationItem> comparator) {
        // Remove duplicate locations.
        Set<LocationItem> items = new TreeSet<>(comparator);
        if (mOriginalValues != null) {
            items.addAll(mOriginalValues);
            mOriginalValues.clear();
            mOriginalValues.addAll(items);
        } else {
            items.addAll(mObjects);
            mObjects.clear();
            mObjects.addAll(items);
        }
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
        synchronized (mLock) {
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

            if (mOriginalValues == null) {
                mOriginalValues = new ArrayList<LocationItem>(mObjects);
            }

            final List<LocationItem> values = new ArrayList<LocationItem>(mOriginalValues);
            final int count = values.size();

            if (TextUtils.isEmpty(constraint)) {
                results.values = values;
                results.count = values.size();
            } else {
                final Locale locale = LocationAdapter.this.locale;
                final String constraintString = constraint.toString().toLowerCase(locale);
                CharSequence latitude;
                CharSequence longitude;

                final List<LocationItem> newValues = new ArrayList<LocationItem>();
                LocationItem value;
                String valueText;

                for (int i = 0; i < count; i++) {
                    value = values.get(i);
                    valueText = value.getLabelLower();
                    latitude = value.getFormatLatitude();
                    longitude = value.getFormatLongitude();

                    if (contains(valueText, constraintString) || (TextUtils.indexOf(latitude, constraintString) >= 0) || (TextUtils.indexOf(longitude, constraintString) >= 0)) {
                        newValues.add(value);
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
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
            String format1 = item1.getLabelLower().toString();
            String format2 = item2.getLabelLower().toString();
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
     * Interface definition for a callback to be invoked when a "favorite"
     * checkbox in this list has been clicked.
     *
     * @author Moshe Waisberg
     */
    public interface OnFavoriteClickListener {

        void onFavoriteClick(LocationAdapter adapter, CompoundButton button, ZmanimAddress address);

    }

    /**
     * Get the listener for "favorite" clicked callbacks.
     *
     * @return the listener.
     */
    public OnFavoriteClickListener getOnFavoriteClickListener() {
        return onFavoriteClickListener;
    }

    /**
     * Set the listener for "favorite" clicked callbacks.
     *
     * @param listener
     *         the listener.
     */
    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.onFavoriteClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();

        if (id == android.R.id.checkbox) {
            CompoundButton buttonView = (CompoundButton) v;
            ZmanimAddress address = (ZmanimAddress) buttonView.getTag();

            if ((address != null) && (onFavoriteClickListener != null)) {
                onFavoriteClickListener.onFavoriteClick(this, buttonView, address);
            }
        }
    }

}
