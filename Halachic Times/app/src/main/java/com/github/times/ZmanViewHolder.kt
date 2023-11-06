package com.github.times

import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import com.github.widget.ArrayAdapter.ArrayViewHolder

/**
 * View holder for zman row item.
 *
 * @author Moshe Waisberg
 */
abstract class ZmanViewHolder(
    itemView: View,
    @IdRes titleId: Int
) : ArrayViewHolder<ZmanimItem>(itemView, titleId) {

    protected abstract val title: TextView
    abstract val time: TextView

    override fun bind(item: ZmanimItem?) {
        val zman = item ?: return
        val isEnabled = !zman.elapsed

        itemView.isEnabled = isEnabled

        title.text = zman.title
        title.isEnabled = isEnabled

        time.text = zman.timeLabel
        time.isEnabled = isEnabled
    }
}