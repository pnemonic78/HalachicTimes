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
package net.sf.times;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import net.sf.times.compass.R;
import net.sf.times.preference.ZmanimSettings;

/**
 * Adapter for all opinions of an halachic time.
 *
 * @author Moshe Waisberg
 */
public class ZmanimDetailsAdapter extends ZmanimAdapter {

    public ZmanimDetailsAdapter(Context context, ZmanimSettings settings) {
        super(context, settings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, R.layout.times_detail);
    }
}