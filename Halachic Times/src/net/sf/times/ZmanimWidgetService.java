package net.sf.times;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViewsService;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ZmanimWidgetService extends RemoteViewsService {

	private ZmanimWidgetViewsFactory mFactory;

	public ZmanimWidgetService() {
	}

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		if (mFactory == null) {
			mFactory = new ZmanimWidgetViewsFactory(this, intent);
		}
		return mFactory;
	}

}
