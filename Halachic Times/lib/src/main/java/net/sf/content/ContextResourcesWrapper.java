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
package net.sf.content;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;

/**
 * Context wrapper that also wraps resources.
 */
public class ContextResourcesWrapper extends ContextWrapper {

    private final Resources resources;

    public ContextResourcesWrapper(Context base, Resources resources) {
        super(base);
        this.resources = resources;
    }

    @Override
    public Resources getResources() {
        return resources;
    }
}
