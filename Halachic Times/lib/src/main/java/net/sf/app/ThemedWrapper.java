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
package net.sf.app;

import android.content.ContextWrapper;

import net.sf.preference.ThemedPreferences;

/**
 * Activity that wraps an activity delegate.
 *
 * @author Moshe Waisberg
 */
public class ThemedWrapper<P extends ThemedPreferences> implements ThemedCallbacks<P> {

    private final ContextWrapper context;

    public ThemedWrapper(ContextWrapper context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        context.setTheme(getThemedPreferences().getTheme());
    }

    @Override
    public P getThemedPreferences() {
        return (P) ((ThemedApplication) context.getApplicationContext()).getThemedPreferences();
    }
}
