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

import android.app.Application;
import android.content.Context;

import net.sf.preference.ThemedPreferences;

/**
 * Application with a theme.
 *
 * @author Moshe Waisberg
 */
public abstract class ThemedApplication<P extends ThemedPreferences> extends Application {

    private P preferences;

    @Override
    public void onCreate() {
        setTheme(getPreferences().getTheme());
        super.onCreate();
    }

    public P getPreferences() {
        if (preferences == null) {
            initPreferences();
            preferences = createPreferences(this);
        }
        return preferences;
    }

    protected void initPreferences() {
        P.init(this);
    }

    protected P createPreferences(Context context) {
        return (P) new ThemedPreferences(context);
    }
}
