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

/**
 * Location adapter for "favorite" locations.
 *
 * @author Moshe Waisberg
 */
class FavoritesLocationAdapter : SpecificLocationAdapter {
    constructor(context: Context, items: List<LocationItem?>) : super(context, items)

    constructor(
        context: Context,
        items: List<LocationItem?>,
        itemListener: LocationItemListener?
    ) : super(context, items, itemListener)

    constructor(
        context: Context,
        items: List<LocationItem?>,
        filterListener: FilterListener?
    ) : super(context, items, filterListener)

    constructor(
        context: Context,
        items: List<LocationItem?>,
        itemListener: LocationItemListener?,
        filterListener: FilterListener?
    ) : super(context, items, itemListener, filterListener)

    override fun isSpecific(item: LocationItem): Boolean {
        return item.isFavorite
    }
}