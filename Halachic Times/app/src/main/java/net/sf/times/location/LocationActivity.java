/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.times.location;

import android.app.Activity;
import android.content.Context;

import net.sf.times.ZmanimActivity;
import net.sf.times.preference.ZmanimSettings;

/**
 * Pick a city from the list.
 *
 * @author Moshe Waisberg
 */
public class LocationActivity extends LocationTabActivity {

    @Override
    protected int getThemeId() {
        Context context = this;
        ZmanimSettings settings = new ZmanimSettings(context);
        return settings.getTheme();
    }

    @Override
    protected Class<? extends Activity> getSearchActivity() {
        return ZmanimActivity.class;
    }
}
