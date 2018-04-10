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
package com.github.view.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

public class LayoutWeightAnimation extends Animation {

    private View view;
    private float fromWeight;
    private float toWeight;
    private boolean increase;

    public LayoutWeightAnimation(View view, float fromWeight, float toWeight) {
        this.view = view;
        this.fromWeight = fromWeight;
        this.toWeight = toWeight;
        increase = toWeight >= fromWeight;
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }

    @Override
    public boolean willChangeTransformationMatrix() {
        return false;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        if (increase)
            lp.weight = (toWeight - fromWeight) * interpolatedTime;
        else
            lp.weight = (fromWeight - toWeight) * (1f - interpolatedTime);
        view.setLayoutParams(lp);
    }
}
