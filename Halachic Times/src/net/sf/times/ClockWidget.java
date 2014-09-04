package net.sf.times;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import android.widget.RemoteViews;

/**
 * Clock widget with hour and title underneath.<br>
 * Based on the default Android digital clock widget.
 * 
 * @author Moshe
 */
public class ClockWidget extends ZmanimWidget {

	/**
	 * Constructs a new widget.
	 */
	public ClockWidget() {
	}

	@Override
	protected int getLayoutId() {
		return R.layout.clock_widget;
	}

	@Override
	protected int getIntentViewId() {
		return R.id.date_gregorian;
	}

	@Override
	protected void bindViews(RemoteViews views, ZmanimAdapter adapter) {
		final int count = adapter.getCount();
		ZmanimItem item;

		for (int position = 0; position < count; position++) {
			item = adapter.getItem(position);
			if (item.elapsed || (item.time == null) || (item.timeLabel == null))
				continue;
			bindView(views, item);
			break;
		}
	}

	@Override
	protected void bindView(RemoteViews views, ZmanimItem item) {
		views.setTextViewText(R.id.time, item.timeLabel);
		views.setTextViewText(android.R.id.title, mContext.getText(item.titleId));
	}

	@Override
	protected boolean isRemoteList() {
		return false;
	}
}
