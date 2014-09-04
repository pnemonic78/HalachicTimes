package net.sf.times;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.widget.RemoteViews;

/**
 * Clock widget with hour and title underneath.<br>
 * Based on the default Android digital clock widget.
 * 
 * @author Moshe
 */
public class ClockWidget extends ZmanimWidget {

	private DateFormat mTimeFormat;

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
		if (mTimeFormat == null) {
			Context context = mContext;
			boolean time24 = android.text.format.DateFormat.is24HourFormat(context);
			String pattern = context.getString(time24 ? R.string.clock_24_hours_format : R.string.clock_12_hours_format);
			mTimeFormat = new SimpleDateFormat(pattern, Locale.getDefault());
		}

		CharSequence label = mTimeFormat.format(item.time);
		SpannableStringBuilder spans = new SpannableStringBuilder(label);
		int indexMinutes = TextUtils.indexOf(label, ':');
		spans.setSpan(new TypefaceSpan("sans-serif"), 0, indexMinutes, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		spans.setSpan(new StyleSpan(Typeface.BOLD), 0, indexMinutes, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		views.setTextViewText(R.id.time, spans);
		views.setTextViewText(android.R.id.title, mContext.getText(item.titleId));
	}

	@Override
	protected boolean isRemoteList() {
		return false;
	}

	@Override
	@SuppressLint("NewApi")
	protected void notifyAppWidgetViewDataChanged(Context context) {
		mTimeFormat = null;
		super.notifyAppWidgetViewDataChanged(context);
	}
}
