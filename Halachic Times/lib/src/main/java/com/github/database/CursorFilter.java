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
package com.github.database;

import android.database.Cursor;

/**
 * An interface for filtering {@link Cursor} objects based on their columns or
 * other information.
 *
 * @author Moshe Waisberg
 */
public interface CursorFilter {

    /**
     * Indicating whether a specific database cursor should be included in a
     * query list.
     *
     * @param cursor the cursor to check.
     * @return {@code true} if the current cursor should be included,
     * {@code false} otherwise.
     */
    boolean accept(Cursor cursor);
}
