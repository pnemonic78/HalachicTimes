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
package net.sf.times;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import net.sf.times.preference.ZmanimPreferences;

/**
 * Adapter for all opinions of an halachic time.
 *
 * @author Moshe Waisberg
 */
public class ZmanimDetailsAdapter extends ZmanimAdapter {

    public ZmanimDetailsAdapter(Context context, ZmanimPreferences settings) {
        super(context, settings);
    }

    @Override
    protected ViewHolder createViewHolder(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.times_detail, parent, false);
        return new ViewHolder(view);
    }
}
