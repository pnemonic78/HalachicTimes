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

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViewsService;

/**
 * Service that provides the list of halachic times (<em>zmanim</em>) items for
 * the scrollable widget.
 *
 * @author Moshe
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ZmanimWidgetService extends RemoteViewsService {

    private ZmanimWidgetViewsFactory factory;

    public ZmanimWidgetService() {
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        if (factory == null) {
            factory = new ZmanimWidgetViewsFactory(this, intent);
        }
        return factory;
    }

}
