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
package com.github.view;

import android.view.View;

/**
 * Listener for receiving events when views are shown or hidden.
 *
 * @author Moshe Waisberg
 */
public interface OnViewVisibilityListener {
    /**
     * Called when the visibility of the view or an ancestor of the view has
     * changed.
     *
     * @param changedView The view whose visibility changed. May be
     *                    {@code this} or an ancestor view.
     * @param visibility  The new visibility, one of {@link View#VISIBLE},
     *                    {@link View#INVISIBLE} or {@link View#GONE}.
     * @see View#onVisibilityChanged(View, int)
     */
    void onVisibilityChanged(View changedView, int visibility);
}
