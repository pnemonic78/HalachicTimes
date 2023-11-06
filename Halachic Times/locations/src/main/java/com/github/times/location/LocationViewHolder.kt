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

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.github.times.location.LocationAdapter.LocationItem
import com.github.times.location.LocationAdapter.LocationItemListener
import com.github.times.location.databinding.LocationBinding
import com.github.widget.ArrayAdapter.ArrayViewHolder

/**
 * View holder for location row item.
 *
 * @author Moshe Waisberg
 */
class LocationViewHolder(
    binding: LocationBinding,
    fieldId: Int,
    private val itemListener: LocationItemListener?
) : ArrayViewHolder<LocationItem>(binding.root, fieldId), View.OnClickListener {

    private val cityName: TextView = binding.title
    private val coordinates: TextView = binding.coordinates
    private val favorite: CheckBox = binding.checkbox

    var item: LocationItem? = null
        private set

    init {
        itemView.setOnClickListener(this)
        favorite.setOnClickListener(this)
    }

    override fun bind(item: LocationItem?) {
        this.item = item
        if (item != null) {
            cityName.text = item.label
            coordinates.text = item.coordinates
            favorite.isChecked = item.isFavorite
        } else {
            cityName.text = ""
            coordinates.text = ""
            favorite.isChecked = false
        }
    }

    override fun onClick(view: View) {
        val item = this.item
        val listener = itemListener
        if ((listener != null) && (item != null)) {
            if (view === favorite) {
                listener.onFavoriteClick(item, favorite.isChecked)
            } else {
                listener.onItemClick(item)
            }
        }
    }
}