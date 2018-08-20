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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Swipe handler for location row item.
 *
 * @author Moshe Waisberg
 */
class LocationSwipeHandler extends ItemTouchHelper.SimpleCallback {

    private final LocationAdapter.LocationItemListener itemListener;
    @ColorInt
    private int deleteColor = Color.RED;
    private final Paint deletePaint = new Paint();

    public LocationSwipeHandler(@NonNull LocationAdapter.LocationItemListener itemListener) {
        super(0, ItemTouchHelper.START);
        this.itemListener = itemListener;
        deletePaint.setColor(deleteColor);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof LocationViewHolder) {
            LocationViewHolder locationViewHolder = (LocationViewHolder) viewHolder;
            LocationAdapter.LocationItem item = locationViewHolder.item;
            ZmanimAddress address = item.getAddress();
            long id = address.getId();

            if (id < 0L) {
                return 0;
            }
            if (address instanceof City) {
                return 0;
            }
            if (address instanceof Country) {
                return 0;
            }
        }
        return super.getSwipeDirs(recyclerView, viewHolder);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // We don't want support moving items up/down
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if (viewHolder instanceof LocationViewHolder) {
            LocationViewHolder locationViewHolder = (LocationViewHolder) viewHolder;
            LocationAdapter.LocationItem item = locationViewHolder.item;
            itemListener.onItemSwipe(item);
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        final View itemView = viewHolder.itemView;

        // Draw the red delete background
        float right = itemView.getRight();
        float top = itemView.getTop();
        float left = right + dX;
        float bottom = itemView.getBottom();
        c.drawRect(left, top, right, bottom, deletePaint);
    }
}
