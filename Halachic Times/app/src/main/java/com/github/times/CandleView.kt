/*
 * Copyright 2019, Moshe Waisberg
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
package com.github.times

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView

/**
 * Flicker animation for 1 candle.
 *
 * @author Moshe Waisberg
 */
class CandleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private val animation: CandleAnimation by lazy { CandleAnimation(handler, this) }

    init {
        scaleType = ScaleType.FIT_CENTER
        setImageResource(R.drawable.candle)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) {
            startFlicker()
        } else {
            stopFlicker()
        }
    }

    /**
     * Start the flicker animation.
     */
    private fun startFlicker() {
        handler?.post(animation)
    }

    /**
     * Stop the flicker animation.
     */
    private fun stopFlicker() {
        handler?.removeCallbacks(animation)
    }

    /**
     * Either start or stop the flicker animation.
     *
     * @param enabled `true` to start - else stop.
     */
    fun flicker(enabled: Boolean) {
        if (enabled) {
            startFlicker()
        } else {
            stopFlicker()
        }
    }
}