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
package com.github.times.location.impl

import android.content.Context
import android.widget.Filter
import com.github.times.location.LocationAdapter
import com.github.times.location.ZmanimAddress

/**
 * Location adapter for a specific type of locations.
 *
 * @author Moshe Waisberg
 */
abstract class SpecificLocationAdapter(
    context: Context,
    items: List<LocationItem>,
    itemListener: LocationItemListener? = null,
    filterListener: FilterListener? = null
) : LocationAdapter(context, items, itemListener) {

    init {
        val listener = if (filterListener == null) null else Filter.FilterListener { count: Int ->
            filterListener.onFilterComplete(
                this@SpecificLocationAdapter,
                count
            )
        }
        filter.filter(null, listener)
    }

    override fun createFilter(): ArrayFilter {
        return SpecificFilter()
    }

    /**
     * Is the address specific to this adapter?
     *
     * @param item the location item.
     * @return `true` to include the location.
     */
    protected abstract fun isSpecific(item: LocationItem): Boolean

    protected inner class SpecificFilter : LocationsFilter() {
        override fun accept(value: LocationItem, constraint: String?): Boolean {
            return isSpecific(value) && super.accept(value, constraint)
        }
    }

    override fun notifyItemChanged(address: ZmanimAddress) {
        synchronized(lock) {
            val count = itemCount
            var item: LocationItem
            var position = -1
            for (i in 0 until count) {
                item = getItem(i) ?: continue
                if (address == item.address) {
                    position = i
                    break
                }
            }
            if (position >= 0) {
                item = getItem(position) ?: return
                if (isSpecific(item)) {
                    notifyItemChanged(position)
                } else {
                    // Hide the item.
                    if (objectsFiltered) {
                        objects.removeAt(position)
                        notifyItemRemoved(position)
                    }
                }
            } else if (objectsFiltered) {
                val size = originalValues.size
                for (i in 0 until size) {
                    item = originalValues[i] ?: continue
                    if (address == item.address) {
                        position = i
                        break
                    }
                }
                if (position >= 0) {
                    item = originalValues[position] ?: return
                    // Find the sorted position.
                    var positionInsert = count
                    val comparator: Comparator<LocationItem?> = comparator
                    for (i in 0 until count) {
                        if (comparator.compare(item, getItem(i)) < 0) {
                            positionInsert = i
                            break
                        }
                    }
                    objects.add(positionInsert, item)
                    notifyItemInserted(positionInsert)
                } else {
                    // Throw error: item not found!
                }
            }
        }
    }
}