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

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.github.times.location.LocationAdapter.LocationItemListener
import com.github.times.location.country.Country

/**
 * Swipe handler for location row item.
 *
 * @author Moshe Waisberg
 */
internal class LocationSwipeHandler(private val itemListener: LocationItemListener) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START) {

    private var deleteBg: Drawable? = null

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
        if (viewHolder is LocationViewHolder) {
            val item = viewHolder.item ?: return 0
            val address = item.address
            val id = address.id
            if (id < 0L) return 0
            if (address is City) return 0
            if (address is Country) return 0
        }
        return super.getSwipeDirs(recyclerView, viewHolder)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {
        // We don't want support moving items up/down
        return false
    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        if (viewHolder is LocationViewHolder) {
            val item = viewHolder.item ?: return
            itemListener.onItemSwipe(item)
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView

        // Draw the red delete background
        val right = itemView.right
        val top = itemView.top
        val left = (right + dX).toInt()
        val bottom = itemView.bottom
        var bg = deleteBg
        if (bg == null) {
            val context = recyclerView.context
            bg = ContextCompat.getDrawable(context, R.drawable.bg_swipe_delete) ?: return
            this.deleteBg = bg
        }
        bg.setBounds(left, top, right, bottom)
        bg.draw(c)
    }
}