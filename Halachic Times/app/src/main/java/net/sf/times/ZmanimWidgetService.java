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

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Service that provides the list of halachic times (<em>zmanim</em>) items for
 * the scrollable widget.
 *
 * @author Moshe
 */
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
