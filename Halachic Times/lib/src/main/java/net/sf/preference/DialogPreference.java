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
package net.sf.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * A base class for {@link Preference} objects that are
 * dialog-based. These preferences will, when clicked, open a dialog showing the
 * actual preference controls.
 */
public class DialogPreference extends android.preference.DialogPreference {

    private CharSequence neutralButtonText;

    public DialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Sets the text of the neutral button of the dialog. This will be shown on subsequent dialogs.
     *
     * @param neutralButtonText
     *         The text of the neutral button.
     */
    public void setNeutralButtonText(CharSequence neutralButtonText) {
        this.neutralButtonText = neutralButtonText;
    }

    /**
     * Sets the text of the neutral button of the dialog. This will be shown on subsequent dialogs.
     *
     * @param neutralButtonTextResId
     *         The neutral button text as a resource.
     * @see #setNeutralButtonText(CharSequence)
     */
    public void setNeutralButtonText(int neutralButtonTextResId) {
        setNeutralButtonText(getContext().getText(neutralButtonTextResId));
    }

    /**
     * Returns the text of the neutral button to be shown on subsequent dialogs.
     *
     * @return The text of the neutral button.
     */
    public CharSequence getNeutralButtonText() {
        return neutralButtonText;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        builder.setNeutralButton(getNeutralButtonText(), this);
    }
}
