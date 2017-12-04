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
package net.sf.compass;

import android.os.Bundle;
import android.view.View;

import net.sf.times.compass.CompassView;

/**
 * Show the compass.
 *
 * @author Moshe Waisberg
 */
public class CompassFragment extends net.sf.times.compass.CompassFragment {

    public CompassFragment() {
        setHoliest(Double.NaN, Double.NaN, 0);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((CompassView) view).setTicks(true);
    }
}
