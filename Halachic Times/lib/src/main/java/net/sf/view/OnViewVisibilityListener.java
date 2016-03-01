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
package net.sf.view;

import android.view.View;

/**
 * Listener for receiving events when views are shown or hidden.
 *
 * @author Moshe Waisberg
 */
public interface OnViewVisibilityListener {
    /**
     * Called when the visibility of the view or an ancestor of the view has
     * changed.
     *
     * @param changedView
     *         The view whose visibility changed. May be
     *         {@code this} or an ancestor view.
     * @param visibility
     *         The new visibility, one of {@link View#VISIBLE},
     *         {@link View#INVISIBLE} or {@link View#GONE}.
     * @see View#onVisibilityChanged(View, int)
     */
    void onVisibilityChanged(View changedView, int visibility);
}
