package com.github.times

import android.graphics.Typeface
import android.util.TypedValue
import android.widget.TextView
import androidx.core.view.isVisible
import com.github.times.databinding.TimesItemBinding

/**
 * View holder for zman row item.
 *
 * @author Moshe Waisberg
 */
class ZmanItemViewHolder(
    binding: TimesItemBinding,
    private val isSummary: Boolean = true,
    private val emphasisScale: Float = 1f,
    private val listener: OnZmanItemClickListener?
) : ZmanViewHolder(binding.root, binding.title.id) {

    override val title: TextView = binding.title
    override val time: TextView = binding.time
    private val summary: TextView = binding.summary

    override fun bind(item: ZmanimItem?) {
        val zman = item ?: return
        val isEnabled = !zman.isElapsed

        itemView.isEnabled = isEnabled
        itemView.setOnClickListener { listener?.onZmanClick(zman) }

        title.text = zman.title
        title.isEnabled = isEnabled
        if (zman.isEmphasis) {
            title.setTypeface(title.typeface, Typeface.BOLD)
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, title.textSize * emphasisScale)
        }

        time.text = zman.timeLabel
        time.isEnabled = isEnabled
        if (zman.isEmphasis) {
            time.setTypeface(time.typeface, Typeface.BOLD)
            time.setTextSize(TypedValue.COMPLEX_UNIT_PX, time.textSize * emphasisScale)
        }

        if (isSummary) {
            summary.text = zman.summary
            summary.isEnabled = isEnabled
            if (zman.summary.isNullOrEmpty()) summary.isVisible = false
        } else {
            summary.isVisible = false
        }
    }
}