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
package com.github.times.location

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.times.location.LocationAdapter.LocationItem
import com.github.times.location.databinding.LocationBinding
import com.github.util.getDefaultLocale
import com.github.widget.ArrayAdapter
import java.text.Collator
import java.util.Locale
import java.util.TreeSet

/**
 * Location adapter.
 *
 * @author Moshe Waisberg
 */
open class LocationAdapter @JvmOverloads constructor(
    context: Context,
    items: List<LocationItem>,
    listener: LocationItemListener? = null
) : ArrayAdapter<LocationItem, LocationViewHolder>(R.layout.location, R.id.title, items) {
    /**
     * Interface definition for callbacks to be invoked when an item in this list has been clicked.
     *
     * @author Moshe Waisberg
     */
    interface LocationItemListener {
        /**
         * Callback to be invoked when an item in this list has been clicked.
         */
        fun onItemClick(item: LocationItem)

        /**
         * Callback to be invoked when a "favorite" checkbox in this list has been clicked.
         */
        fun onFavoriteClick(item: LocationItem, checked: Boolean)

        /**
         * Callback to be invoked when an item in this list has been swiped.
         */
        fun onItemSwipe(item: LocationItem)
    }

    /**
     * Listener used to receive a notification upon completion of a filtering operation.
     */
    interface FilterListener {
        /**
         *
         * Notifies the end of a filtering operation.
         *
         * @param adapter the adapter.
         * @param count   the number of values computed by the filter
         */
        fun onFilterComplete(adapter: LocationAdapter?, count: Int)
    }

    protected val comparator: LocationComparator by lazy { LocationComparator() }
    private val collator: Collator = Collator.getInstance()
    private val locale: Locale = context.getDefaultLocale()
    private var listener: LocationItemListener? = null

    init {
        setHasStableIds(false)
        collator.strength = Collator.PRIMARY
        setOnItemListener(listener)
        sortNoNotify()
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)!!.address.id
    }

    override fun createArrayViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int,
        fieldId: Int
    ): LocationViewHolder {
        val binding = LocationBinding.inflate(inflater, parent, false)
        return LocationViewHolder(binding, fieldId, listener)
    }
    /**
     * Sort without notification.
     *
     * @param comparator comparator used to sort the objects contained in this adapter.
     */
    /**
     * Sort without notification.
     */
    private fun sortNoNotify(comparator: Comparator<in LocationItem?> = this.comparator) {
        if (objectsFiltered) {
            sortNoNotify(originalValues, comparator)
        } else {
            sortNoNotify(objects, comparator)
        }
    }

    /**
     * Sort without notification.
     *
     * @param objects    the list of objects to sort.
     * @param comparator comparator used to sort the objects contained in this adapter.
     */
    private fun sortNoNotify(
        objects: MutableList<LocationItem?>,
        comparator: Comparator<in LocationItem?>
    ) {
        // Removes duplicate locations.
        val items: MutableSet<LocationItem?> = TreeSet(comparator)
        items.addAll(objects)
        objects.clear()
        objects.addAll(items)
    }

    /**
     * Sort.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun sort() {
        sortNoNotify()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun sort(comparator: Comparator<in LocationItem?>) {
        sortNoNotify(comparator)
        notifyDataSetChanged()
    }

    override fun createFilter(): ArrayFilter {
        return LocationsFilter()
    }

    open fun notifyItemChanged(address: ZmanimAddress) {
        synchronized(lock) {
            val size = itemCount
            var item: LocationItem?
            for (i in 0 until size) {
                item = getItem(i) ?: continue
                if (item.address == address) {
                    notifyItemChanged(i)
                    return
                }
            }
        }
    }

    fun remove(address: ZmanimAddress?) {
        synchronized(lock) {
            val size = itemCount
            var item: LocationItem?
            for (i in 0 until size) {
                item = getItem(i)
                if (item!!.address.equals(address)) {
                    remove(item)
                    return
                }
            }
        }
    }

    fun delete(address: ZmanimAddress?) {
        synchronized(lock) {
            val size = itemCount
            var item: LocationItem?
            for (i in 0 until size) {
                item = getItem(i)
                if (item!!.address.equals(address)) {
                    delete(item)
                    return
                }
            }
        }
    }

    /**
     * Filter the list of locations to match cities' names that contain the constraint.
     *
     * @author Moshe Waisberg
     */
    protected open inner class LocationsFilter : ArrayFilter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            if (!objectsFiltered) {
                synchronized(lock) {
                    objectsFiltered = true
                    originalValues.clear()
                    originalValues.addAll(objects)
                }
            }
            val values: List<LocationItem> = ArrayList(originalValues.filterNotNull())
            val count = values.size
            val locale = locale
            val constraintString =
                if (constraint.isNullOrEmpty()) null else constraint.toString().lowercase(locale)
            val newValues = mutableListOf<LocationItem>()
            var value: LocationItem
            for (i in 0 until count) {
                value = values[i]
                if (accept(value, constraintString)) {
                    newValues.add(value)
                }
            }
            results.values = newValues
            results.count = newValues.size
            return results
        }

        protected open fun accept(value: LocationItem, constraint: String?): Boolean {
            if (constraint.isNullOrEmpty()) {
                return true
            }
            val valueText = value.labelLower
            val latitude = value.formatLatitude
            val longitude = value.formatLongitude
            return contains(valueText, constraint)
                || latitude.indexOf(constraint) >= 0
                || longitude.indexOf(constraint) >= 0
        }

        /**
         * Does the first string contain the other string?
         *
         * @param s      the source string.
         * @param search the character sequence to search for.
         * @return `true` if `s` contains `search`.
         */
        private fun contains(s: String, search: String?): Boolean {
            if (search == null) return true
            val len1 = s.length
            val len2 = search.length
            if (len1 < len2) return false
            val collator = collator
            if (len1 == len2) {
                return s == search || collator.equals(s, search)
            }
            if (s.contains(search)) return true

            // Let's do a "Collator.contains"
            var lhs: String
            var rhs: String
            val dLen = len1 - len2
            var concat: String
            for (i in 0 until dLen) {
                lhs = s.substring(0, i)
                rhs = s.substring(len2 + i)
                concat = lhs + search + rhs
                if (collator.equals(s, concat)) return true
            }
            return false
        }
    }

    /**
     * Location item.
     *
     * @author Moshe Waisberg
     */
    /*FIXME protected*/
    class LocationItem(
        /**
         * Get the source address.
         *
         * @return the address.
         */
        val address: ZmanimAddress,
        formatter: LocationFormatter
    ) {
        /**
         * Get the label.
         *
         * @return the label.
         */
        val label: CharSequence = address.formatted

        /**
         * Get the label in lower casing.
         *
         * @return the label.
         */
        val labelLower: String = label.toString().lowercase(address.locale)

        /**
         * Get the formatted latitude.
         *
         * @return the latitude.
         */
        val formatLatitude: CharSequence = formatter.formatLatitude(address.latitude)

        /**
         * Get the formatted longitude.
         *
         * @return the longitude.
         */
        val formatLongitude: CharSequence = formatter.formatLongitude(address.longitude)

        /**
         * Get the formatted coordinates.
         *
         * @return the coordinates.
         */
        val coordinates: CharSequence =
            formatter.formatCoordinates(address.latitude, address.longitude, Double.NaN)

        /**
         * Get the address id.
         *
         * @return the id.
         */
        val id: Long
            get() = address.id

        /**
         * Is location a favourite?
         *
         * @return `true` if a favourite.
         */
        val isFavorite: Boolean
            get() = address.isFavorite

        override fun equals(other: Any?): Boolean {
            if (other == null) return false
            if (other is LocationItem) return address == other.address
            if (other is ZmanimAddress) return address.equals(other)
            return super.equals(other)
        }

        override fun toString(): String {
            return address.toString()
        }
    }

    /**
     * Compare two locations by their locations, then by their names, then their IDs.
     *
     * @author Moshe Waisberg
     */
    protected class LocationComparator : Comparator<LocationItem?> {
        private val collator: Collator = Collator.getInstance()

        init {
            collator.strength = Collator.PRIMARY
        }

        override fun compare(item1: LocationItem?, item2: LocationItem?): Int {
            if (item1 === item2) return 0
            if (item1 == null) return -1
            if (item2 == null) return +1

            // Sort first by name.
            val format1 = item1.labelLower
            val format2 = item2.labelLower
            val c = collator.compare(format1, format2)
            if (c != 0) return c

            val addr1 = item1.address
            val addr2 = item2.address

            // Is same location?
            val lat1 = addr1.latitude
            val lat2 = addr2.latitude
            val latD = lat1 - lat2
            if (latD >= EPSILON) return 1
            if (latD <= -EPSILON) return -1

            val lng1 = addr1.longitude
            val lng2 = addr2.longitude
            val lngD = lng1 - lng2
            if (lngD >= EPSILON) return 1
            if (lngD <= -EPSILON) return -1

            // Then sort by id. Positive id is more important.
            val id1 = addr1.id
            val id2 = addr2.id
            return id1.compareTo(id2)
        }

        companion object {
            /**
             * Double subtraction error.
             */
            private const val EPSILON = 1e-6
        }
    }

    /**
     * Set the listener for "item" clicked callbacks.
     *
     * @param listener the listener.
     */
    fun setOnItemListener(listener: LocationItemListener?) {
        this.listener = listener
    }
}