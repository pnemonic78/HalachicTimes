package com.github.times

import android.widget.TextView
import com.github.times.databinding.TimesDetailBinding

/**
 * View holder for zman row item.
 *
 * @author Moshe Waisberg
 */
class ZmanDetailsViewHolder(binding: TimesDetailBinding) :
    ZmanViewHolder(binding.root, binding.title.id) {

    override val title: TextView = binding.title
    override val time: TextView = binding.time

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