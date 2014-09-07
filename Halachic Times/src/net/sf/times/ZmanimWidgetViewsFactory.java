package net.sf.times;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sf.times.location.ZmanimAddress;
import net.sf.times.location.ZmanimLocationListener;
import net.sf.times.location.ZmanimLocations;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;
import net.sourceforge.zmanim.util.GeoLocation;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ZmanimWidgetViewsFactory implements RemoteViewsFactory, ZmanimLocationListener {

	/** The context. */
	private final Context mContext;
	/** Provider for locations. */
	private ZmanimLocations mLocations;
	/** The settings and preferences. */
	private ZmanimSettings mSettings;
	private ZmanimAdapter mAdapter;
	/** Position index of next Hebrew day. */
	private int mPositionTomorrow;
	private int mColorDisabled = Color.DKGRAY;
	private int mColorEnabled = Color.WHITE;

	public ZmanimWidgetViewsFactory(Context context, Intent intent) {
		mContext = context;
	}

	@Override
	public int getCount() {
		return 1 + mAdapter.getCount() + (mPositionTomorrow > 0 ? 1 : 0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	@Override
	public RemoteViews getViewAt(int position) {
		String pkg = mContext.getPackageName();
		ZmanimAdapter adapter = mAdapter;
		RemoteViews row;

		if (position == 0) {
			ComplexZmanimCalendar zmanCal = adapter.getCalendar();
			JewishDate jewishDate = new JewishDate(zmanCal.getCalendar());
			CharSequence dateHebrew = adapter.formatDate(jewishDate);

			row = new RemoteViews(pkg, R.layout.widget_date);
			bindViewGrouping(row, position, dateHebrew);
			return row;
		}
		position--;
		if (position == mPositionTomorrow) {
			ComplexZmanimCalendar zmanCal = adapter.getCalendar();
			JewishDate jewishDate = new JewishDate(zmanCal.getCalendar());
			jewishDate.forward();
			CharSequence dateHebrew = adapter.formatDate(jewishDate);

			row = new RemoteViews(pkg, R.layout.widget_date);
			bindViewGrouping(row, position, dateHebrew);
			return row;
		}
		if (position > mPositionTomorrow) {
			position--;
		}

		ZmanimItem item = adapter.getItem(position);
		row = new RemoteViews(pkg, R.layout.widget_item);
		bindView(row, position, item);
		return row;
	}

	@Override
	public int getViewTypeCount() {
		return 1 + mAdapter.getViewTypeCount();
	}

	@Override
	public boolean hasStableIds() {
		return mAdapter.hasStableIds();
	}

	@Override
	public void onCreate() {
		if (mLocations != null)
			mLocations.start(this);
	}

	@Override
	public void onDataSetChanged() {
		populateAdapter();
	}

	@Override
	public void onDestroy() {
		if (mLocations != null)
			mLocations.stop(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		onDataSetChanged();
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onAddressChanged(Location location, ZmanimAddress address) {
		onDataSetChanged();
	}

	@Override
	public void onElevationChanged(Location location) {
		onDataSetChanged();
	}

	private void populateAdapter() {
		Context context = mContext;

		if (mSettings == null)
			mSettings = new ZmanimSettings(context);
		if (mLocations == null) {
			ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
			mLocations = app.getLocations();
			mLocations.start(this);
		}
		GeoLocation gloc = mLocations.getGeoLocation();
		if (gloc == null)
			return;
		ComplexZmanimCalendar today = new ComplexZmanimCalendar(gloc);
		final boolean inIsrael = mLocations.inIsrael();

		ZmanimAdapter adapter = new ZmanimAdapter(context, mSettings, today, inIsrael);
		adapter.populate(false);
		mAdapter = adapter;

		mPositionTomorrow = -1;
		ZmanimItem item;
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			item = adapter.getItem(i);
			if (item.titleId == R.string.sunset) {
				mPositionTomorrow = i + 1;
				break;
			}
		}
	}

	/**
	 * Bind the item to the remote view.
	 * 
	 * @param row
	 *            the remote list row.
	 * @param position
	 *            the position index.
	 * @param item
	 *            the zman item.
	 */
	private void bindView(RemoteViews row, int position, ZmanimItem item) {
		row.setTextViewText(android.R.id.title, mContext.getText(item.titleId));
		row.setTextViewText(R.id.time, item.timeLabel);
		// FIXME - the application must notify the widget that "past times" has
		// changed.
		if (item.elapsed) {
			// Using {@code row.setBoolean(id, "setEnabled", enabled)} throws
			// error.
			row.setTextColor(android.R.id.title, mColorDisabled);
			row.setTextColor(R.id.time, mColorDisabled);
		} else {
			row.setTextColor(android.R.id.title, mColorEnabled);
			row.setTextColor(R.id.time, mColorEnabled);
		}
		// Enable clicking to open the main activity.
		row.setOnClickFillInIntent(R.id.widget_item, new Intent());
	}

	/**
	 * Bind the date group header to a list.
	 * 
	 * @param row
	 *            the remote list row.
	 * @param position
	 *            the position index.
	 * @param label
	 *            the formatted Hebrew date label.
	 */
	private void bindViewGrouping(RemoteViews row, int position, CharSequence label) {
		row.setTextViewText(R.id.date_hebrew, label);
	}
}
