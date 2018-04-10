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
package com.github.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import com.github.lib.R;

/**
 * Preference that shows a number picker.
 *
 * @author Moshe Waisberg
 */
public class NumberPickerPreference extends DialogPreference {

    private static final int[] ATTRIBUTES = {android.R.attr.max};

    private NumberPicker picker;
    private int value;
    private int max;
    private int min;
    private boolean progressSet;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs, ATTRIBUTES, defStyleAttr, defStyleRes);
        this.max = a.getInt(0, 100);
        a.recycle();

        if (getDialogLayoutResource() == 0) {
            setDialogLayoutResource(R.layout.preference_dialog_numberpicker);
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        NumberPicker picker = view.findViewById(R.id.picker);
        this.picker = picker;

        int value = getValue();

        picker.setMinValue(getMin());
        picker.setMaxValue(getMax());
        picker.setEnabled(isEnabled());
        if (value != picker.getValue()) {
            picker.setValue(value);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            picker.clearFocus();
            int value = picker.getValue();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }

    @Override
    protected Integer onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(value) : (Integer) defaultValue);
    }

    /**
     * Saves the value to the {@link SharedPreferences}.
     *
     * @param value the value.
     */
    public void setValue(int value) {
        // Always persist/notify the first time; don't assume the field's
        // default value.
        final boolean changed = this.value != value;
        if (changed || !progressSet) {
            this.value = value;
            progressSet = true;
            persistInt(value);
            if (changed) {
                notifyDependencyChange(shouldDisableDependents());
                notifyChanged();
            }
        }
    }

    /**
     * Gets the value from the {@link SharedPreferences}.
     *
     * @return the value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Set the range of the value bar to {@code 0}...{@code max}.
     *
     * @param max the upper range of this value bar.
     */
    public void setMax(int max) {
        this.max = max;
        if (picker != null) {
            picker.setMaxValue(max);
        }
    }

    /**
     * Get the maximum value.
     *
     * @return the upper range of this value bar.
     */
    public int getMax() {
        return max;
    }

    /**
     * Set the range of the value bar to {@code min}...{@code max}.
     *
     * @param min the lower range of this value bar.
     */
    public void setMin(int min) {
        this.min = min;
        if (picker != null) {
            picker.setMinValue(min);
        }
    }

    /**
     * Get the minimum value.
     *
     * @return the lower range of this value bar.
     */
    public int getMin() {
        return min;
    }
}
