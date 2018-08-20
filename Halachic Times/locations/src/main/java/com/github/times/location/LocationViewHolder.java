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

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.github.widget.ArrayAdapter;

/**
 * View holder for location row item.
 *
 * @author Moshe Waisberg
 */
class LocationViewHolder extends ArrayAdapter.ArrayViewHolder<LocationAdapter.LocationItem> implements View.OnClickListener {

    protected final TextView cityName;
    protected final TextView coordinates;
    protected final CheckBox favorite;

    protected LocationAdapter.LocationItem item;

    private final LocationAdapter.LocationItemListener itemListener;

    public LocationViewHolder(View itemView, int fieldId, LocationAdapter.LocationItemListener itemListener) {
        super(itemView, fieldId);

        this.cityName = textView;
        this.coordinates = itemView.findViewById(R.id.coordinates);
        this.favorite = itemView.findViewById(android.R.id.checkbox);

        this.itemListener = itemListener;
        itemView.setOnClickListener(this);
        favorite.setOnClickListener(this);
    }

    @Override
    public void bind(LocationAdapter.LocationItem item) {
        this.item = item;

        cityName.setText(item.getLabel());
        coordinates.setText(item.getCoordinates());
        favorite.setChecked(item.isFavorite());
        favorite.setTag(item.getAddress());
    }

    public LocationAdapter.LocationItem getItem() {
        return item;
    }

    @Override
    public void onClick(View view) {
        if (itemListener != null) {
            if (view == favorite) {
                itemListener.onFavoriteClick(item, favorite.isChecked());
            } else {
                itemListener.onItemClick(item);
            }
        }
    }
}
