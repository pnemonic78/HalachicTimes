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
package com.github.times

import android.graphics.drawable.Drawable
import android.os.Handler
import android.text.format.DateUtils
import android.widget.ImageView
import kotlin.random.Random

/**
 * Flicker animation for 1 candle.
 *
 * @author Moshe Waisberg
 */
class CandleAnimation @JvmOverloads constructor(
    private val handler: Handler,
    /** The image view. */
    view: ImageView,
    /** The randomizer. */
    val random: Random? = Random.Default
) : Runnable {
    private val candle: Drawable = view.drawable

    override fun run() {
        val level = (candle.level + 1) % MAX_LEVELS
        candle.level = LEVELS[level]

        val random = this.random
        if (random == null) {
            handler.postDelayed(this, PERIOD)
        } else {
            handler.postDelayed(this, random.nextInt(PERIOD_INT).toLong())
        }
    }

    companion object {
        private const val PERIOD = DateUtils.SECOND_IN_MILLIS / 2
        private const val PERIOD_INT = PERIOD.toInt()
        private val LEVELS = intArrayOf(0, 1, 2, 3, 2, 1, 0, 4, 5, 6, 7, 6, 5, 4)
        private val MAX_LEVELS = LEVELS.size
    }
}