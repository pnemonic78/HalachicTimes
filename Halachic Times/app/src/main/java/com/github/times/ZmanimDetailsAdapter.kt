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
package com.github.times;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.github.times.databinding.TimesDetailBinding;
import com.github.times.preference.ZmanimPreferences;

/**
 * Adapter for all opinions of an halachic time.
 *
 * @author Moshe Waisberg
 */
public class ZmanimDetailsAdapter extends ZmanimAdapter<ZmanDetailsViewHolder> {

    public ZmanimDetailsAdapter(Context context, ZmanimPreferences settings) {
        super(context, settings, null);
    }

    @NonNull
    @Override
    protected ZmanDetailsViewHolder createArrayViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType, int fieldId) {
        TimesDetailBinding binding = TimesDetailBinding.inflate(inflater, parent, false);
        return new ZmanDetailsViewHolder(binding);
    }
}
