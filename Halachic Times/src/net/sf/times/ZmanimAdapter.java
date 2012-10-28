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

import java.util.Calendar;
import java.util.Date;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RemoteViews;
import android.widget.TextView;

/**
 * Adapter for halachic times list.
 * 
 * @author Moshe
 */
public class ZmanimAdapter extends ArrayAdapter<ZmanimItem> {

	/** 1 second. */
	private static final long ONE_SECOND = 1000;
	/** 1 minute. */
	private static final long ONE_MINUTE = 60 * ONE_SECOND;
	/** 1 hour. */
	private static final long ONE_HOUR = 60 * ONE_MINUTE;
	/** 12 hours. */
	private static final long TWELVE_HOURS = 12 * ONE_HOUR;

	/** 11.5&deg; before sunrise. */
	private static final double ZENITH_TALLIS = 101.5;

	/** Holiday id for Shabbath. */
	private static final int SHABBATH = -1;

	/** No candles to light. */
	private static final int CANDLES_NONE = 0;
	/** Number of candles to light for Shabbath. */
	private static final int CANDLES_SHABBATH = 2;
	/** Number of candles to light for a festival. */
	private static final int CANDLES_FESTIVAL = 2;
	/** Number of candles to light for Yom Kippur. */
	private static final int CANDLES_YOM_KIPPUR = 1;

	private final Context mContext;
	private final LayoutInflater mInflater;
	private final ZmanimSettings mSettings;
	private final long mNow = System.currentTimeMillis();
	private final boolean mSummaries;
	private final boolean mElapsed;
	private final int mCandlesOffset;

	/**
	 * Time row item.
	 */
	static class ZmanimItem {

		/** The title id. */
		public int titleId;
		/** The summary. */
		public CharSequence summary;
		/** The time. */
		public CharSequence time;
		/** Has the time elapsed? */
		public boolean elapsed;
		/** The time text id. */
		public int timeId;

		public ZmanimItem() {
			super();
		}
	}

	/**
	 * Creates a new adapter.
	 * 
	 * @param context
	 *            the context.
	 * @param settings
	 *            the application settings.
	 */
	public ZmanimAdapter(Context context, ZmanimSettings settings) {
		super(context, R.layout.times_item, 0);
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mSettings = settings;
		mSummaries = settings.isSummaries();
		mElapsed = settings.isPast();
		mCandlesOffset = settings.getCandleLightingOffset();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, R.layout.times_item);
	}

	/**
	 * Bind the item to the view.
	 * 
	 * @param position
	 *            the row index.
	 * @param convertView
	 *            the view.
	 * @param parent
	 *            the parent view.
	 * @param resource
	 *            the resource layout.
	 * @return the item view.
	 */
	private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
		View view = convertView;
		if (convertView == null)
			view = mInflater.inflate(resource, parent, false);
		ZmanimItem item = getItem(position);

		TextView title = (TextView) view.findViewById(R.id.title);
		title.setText(item.titleId);
		TextView summary = (TextView) view.findViewById(R.id.summary);
		summary.setText(item.summary);
		if (item.summary == null)
			summary.setVisibility(View.GONE);
		TextView time = (TextView) view.findViewById(R.id.time);
		time.setText(item.time);
		boolean enabled = !item.elapsed;
		title.setEnabled(enabled);
		summary.setEnabled(enabled);
		time.setEnabled(enabled);

		return view;
	}

	/**
	 * Bind the times to a list.
	 * 
	 * @param list
	 *            the list.
	 */
	public void bindViews(ViewGroup list) {
		final int count = getCount();
		list.removeAllViews();
		for (int position = 0; position < count; position++) {
			list.addView(getView(position, null, list));
		}
	}

	/**
	 * Bind the times to remote views.
	 * 
	 * @param views
	 *            the remote views.
	 */
	public void bindViews(RemoteViews views) {
		final int count = getCount();
		ZmanimItem item;

		for (int position = 0; position < count; position++) {
			item = getItem(position);
			bindView(views, item);
		}
	}

	/**
	 * Bind the times to remote views.
	 * 
	 * @param views
	 *            the remote views.
	 */
	private void bindView(RemoteViews views, ZmanimItem item) {
		if (item.elapsed || (item.time == null)) {
			views.setViewVisibility(item.titleId, View.GONE);
		} else {
			views.setViewVisibility(item.titleId, View.VISIBLE);
			views.setTextViewText(item.timeId, item.time);
		}
	}

	/**
	 * Adds the item to the array for a valid time.
	 * 
	 * @param labelId
	 *            the label text id.
	 * @param summaryId
	 *            the summary text id.
	 * @param time
	 *            the time in milliseconds.
	 */
	public void add(int labelId, int summaryId, long time) {
		add(labelId, mSummaries && (summaryId != 0) ? mContext.getText(summaryId) : (CharSequence) null, time);
	}

	/**
	 * Adds the item to the array for a valid time.
	 * 
	 * @param labelId
	 *            the label text id.
	 * @param summaryId
	 *            the summary text id.
	 * @param time
	 *            the time in milliseconds.
	 */
	public void add(int labelId, CharSequence summary, long time) {
		if (time == 0)
			return;

		ZmanimItem item = new ZmanimItem();
		item.titleId = labelId;
		item.summary = mSummaries ? summary : null;
		item.time = DateUtils.formatDateTime(getContext(), time, DateUtils.FORMAT_SHOW_TIME);
		item.elapsed = mElapsed ? false : (time < mNow);

		add(item);
	}

	/**
	 * Adds the item to the array for a valid date.
	 * 
	 * @param labelId
	 *            the label text id.
	 * @param summaryId
	 *            the summary text id.
	 * @param date
	 *            the date.
	 */
	public void add(int labelId, int summaryId, Date date) {
		if (date == null)
			return;
		add(labelId, summaryId, (date == null) ? 0L : date.getTime());
	}

	/**
	 * Adds the item to the array for a valid date.
	 * 
	 * @param titleId
	 *            the title text id.
	 * @param summary
	 *            the summary text.
	 * @param date
	 *            the date.
	 */
	public void add(int titleId, CharSequence summary, Date date) {
		if (date == null)
			return;
		add(titleId, summary, (date == null) ? 0L : date.getTime());
	}

	/**
	 * Adds the item to the array for a valid date.
	 * 
	 * @param rowId
	 *            the row layout id.
	 * @param timeId
	 *            the time text id.
	 * @param date
	 *            the date.
	 * @param set
	 *            set for remote views?
	 */
	public void add(int rowId, int timeId, Date date, boolean set) {
		add(rowId, timeId, (date == null) ? 0L : date.getTime(), set);
	}

	/**
	 * Adds the item to the array for a valid date.
	 * 
	 * @param rowId
	 *            the row layout id.
	 * @param timeId
	 *            the time text id.
	 * @param time
	 *            the time in milliseconds.
	 * @param set
	 *            set for remote views?
	 */
	public void add(int rowId, int timeId, long time, boolean set) {
		ZmanimItem item = new ZmanimItem();
		item.titleId = rowId;
		item.timeId = timeId;
		item.time = DateUtils.formatDateTime(getContext(), time, DateUtils.FORMAT_SHOW_TIME);
		item.elapsed = /* mElapsed && !set ? false : */(time < mNow);

		add(item);
	}

	/**
	 * Populate the list of times.
	 * 
	 * @param cal
	 *            the calendar.
	 * @param inIsrael
	 *            is in Israel?
	 * @param remote
	 *            is for remote views?
	 */
	public void populate(ComplexZmanimCalendar cal, boolean inIsrael, boolean remote) {
		int candlesCount = 0;
		Date candlesWhen = cal.getCandleLighting();
		if (candlesWhen != null)
			candlesCount = getCandles(cal.getCalendar(), inIsrael);

		Date date;
		int summary;
		String opinion = mSettings.getDawn();
		if ("19.8".equals(opinion)) {
			date = cal.getAlos19Point8Degrees();
			summary = R.string.dawn_19;
		} else if ("120".equals(opinion)) {
			date = cal.getAlos120();
			summary = R.string.dawn_120;
		} else if ("18".equals(opinion)) {
			date = cal.getAlos18Degrees();
			summary = R.string.dawn_18;
		} else if ("26".equals(opinion)) {
			date = cal.getAlos26Degrees();
			summary = R.string.dawn_26;
		} else if ("120_zmanis".equals(opinion)) {
			date = cal.getAlos120Zmanis();
			summary = R.string.dawn_120_zmanis;
		} else if ("16.1".equals(opinion)) {
			date = cal.getAlos16Point1Degrees();
			summary = R.string.dawn_16;
		} else if ("96".equals(opinion)) {
			date = cal.getAlos96();
			summary = R.string.dawn_96;
		} else if ("90".equals(opinion)) {
			date = cal.getAlos90();
			summary = R.string.dawn_90;
		} else if ("96_zmanis".equals(opinion)) {
			date = cal.getAlos90Zmanis();
			summary = R.string.dawn_96_zmanis;
		} else if ("90_zmanis".equals(opinion)) {
			date = cal.getAlos90Zmanis();
			summary = R.string.dawn_90_zmanis;
		} else if ("72".equals(opinion)) {
			date = cal.getAlos72();
			summary = R.string.dawn_72;
		} else if ("72_zmanis".equals(opinion)) {
			date = cal.getAlos72Zmanis();
			summary = R.string.dawn_72_zmanis;
		} else if ("60".equals(opinion)) {
			date = cal.getAlos60();
			summary = R.string.dawn_60;
		} else {
			date = cal.getAlosHashachar();
			summary = R.string.dawn_16;
		}
		if (remote)
			add(R.id.dawn_row, R.id.dawn_time, date, true);
		else
			add(R.string.dawn, summary, date);

		date = cal.getSunriseOffsetByDegrees(ZENITH_TALLIS);
		if (remote)
			add(R.id.earliest_row, R.id.earliest_time, date, true);
		else
			add(R.string.earliest, R.string.earliest_summary, date);

		date = cal.getSunrise();
		if (remote)
			add(R.id.sunrise_row, R.id.sunrise_time, date, true);
		else
			add(R.string.sunrise, R.string.sunrise_summary, date);

		opinion = mSettings.getLastShema();
		if ("16.1_sunset".equals(opinion)) {
			date = cal.getSofZmanShmaAlos16Point1ToSunset();
			summary = R.string.shema_16_sunset;
		} else if ("7.083".equals(opinion)) {
			date = cal.getSofZmanShmaAlos16Point1ToTzaisGeonim7Point083Degrees();
			summary = R.string.shema_7;
		} else if ("19.8".equals(opinion)) {
			date = cal.getSofZmanShmaMGA19Point8Degrees();
			summary = R.string.shema_19;
		} else if ("120".equals(opinion)) {
			date = cal.getSofZmanShmaMGA120Minutes();
			summary = R.string.shema_120;
		} else if ("18".equals(opinion)) {
			date = cal.getSofZmanShmaMGA18Degrees();
			summary = R.string.shema_18;
		} else if ("96".equals(opinion)) {
			date = cal.getSofZmanShmaMGA96Minutes();
			summary = R.string.shema_96;
		} else if ("16.1".equals(opinion)) {
			date = cal.getSofZmanShmaMGA16Point1Degrees();
			summary = R.string.shema_16;
		} else if ("90".equals(opinion)) {
			date = cal.getSofZmanShmaMGA90Minutes();
			summary = R.string.shema_90;
		} else if ("96_zmanis".equals(opinion)) {
			date = cal.getSofZmanShmaMGA96MinutesZmanis();
			summary = R.string.shema_96_zmanis;
		} else if ("90_zmanis".equals(opinion)) {
			date = cal.getSofZmanShmaMGA90MinutesZmanis();
			summary = R.string.shema_90_zmanis;
		} else if ("72".equals(opinion)) {
			date = cal.getSofZmanShmaMGA72Minutes();
			summary = R.string.shema_72;
		} else if ("MGA".equals(opinion)) {
			date = cal.getSofZmanShmaMGA();
			summary = R.string.shema_mga;
		} else if ("AT".equals(opinion)) {
			date = cal.getSofZmanShmaAteretTorah();
			summary = R.string.shema_ateret;
		} else if ("3".equals(opinion)) {
			date = cal.getSofZmanShma3HoursBeforeChatzos();
			summary = R.string.shema_3;
		} else if ("72_zmanis".equals(opinion)) {
			date = cal.getSofZmanShmaMGA72MinutesZmanis();
			summary = R.string.shema_72_zmanis;
		} else if ("FL".equals(opinion)) {
			date = cal.getSofZmanShmaFixedLocal();
			summary = R.string.shema_fixed;
		} else if ("GRA".equals(opinion)) {
			date = cal.getSofZmanShmaGRA();
			summary = R.string.shema_gra;
		} else {
			date = cal.getSofZmanShmaMGA();
			summary = R.string.shema_mga;
		}
		if (remote)
			add(R.id.shema_row, R.id.shema_time, date, true);
		else
			add(R.string.shema, summary, date);

		date = cal.getSofZmanTfilaMGA();
		if (remote)
			add(R.id.prayers_mga_row, R.id.prayers_mga_time, date, true);
		else
			add(R.string.prayers_mga, R.string.prayers_mga_summary, date);

		date = cal.getSofZmanTfilaGRA();
		if (remote)
			add(R.id.prayers_gra_row, R.id.prayers_gra_time, date, true);
		else
			add(R.string.prayers_gra, R.string.prayers_gra_summary, date);

		date = cal.getChatzos();
		if (remote)
			add(R.id.midday_row, R.id.midday_time, date, true);
		else
			add(R.string.midday, R.string.midday_summary, date);

		date = cal.getMinchaGedola();
		if (remote)
			add(R.id.earliest_mincha_row, R.id.earliest_mincha_time, date, true);
		else
			add(R.string.earliest_mincha, R.string.earliest_mincha_summary, date);

		date = cal.getMinchaKetana();
		if (remote)
			add(R.id.mincha_row, R.id.mincha_time, date, true);
		else
			add(R.string.mincha, R.string.mincha_summary, date);

		date = cal.getPlagHamincha();
		if (remote)
			add(R.id.plug_hamincha_row, R.id.plug_hamincha_time, date, true);
		else
			add(R.string.plug_hamincha, R.string.plug_hamincha_summary, date);

		date = cal.getCandleLighting();
		if (remote)
			add(R.id.candles_row, R.id.candles_time, (candlesCount > 0) ? date : null, true);
		else if (candlesCount > 0) {
			String summaryText = mContext.getString(R.string.candles_summary, mCandlesOffset);
			add(R.string.candles, summaryText, date);
		}

		date = cal.getSunset();
		if (remote)
			add(R.id.sunset_row, R.id.sunset_time, date, true);
		else
			add(R.string.sunset, R.string.sunset_summary, date);

		date = cal.getTzais();
		if (remote)
			add(R.id.candles2_row, R.id.candles2_time, (candlesCount < 0) ? date : null, true);
		else if (candlesCount < 0)
			add(R.string.candles, R.string.nightfall_3stars_summary, date);
		if (remote)
			add(R.id.nightfall_3stars_row, R.id.nightfall_3stars_time, date, true);
		else
			add(R.string.nightfall_3stars, R.string.nightfall_3stars_summary, date);

		date = cal.getTzais72();
		if (remote)
			add(R.id.nightfall_72min_row, R.id.nightfall_72min_time, date, true);
		else
			add(R.string.nightfall_72min, R.string.nightfall_72min_summary, date);

		long time = cal.getChatzos().getTime() + TWELVE_HOURS;
		if (remote)
			add(R.id.midnight_row, R.id.midnight_time, time, true);
		else
			add(R.string.midnight, R.string.midnight_summary, time);
	}

	/**
	 * Get the number of candles to light.
	 * 
	 * @param cal
	 *            the Gregorian date.
	 * @param inIsrael
	 *            is in Israel?
	 * @return the number of candles to light. Positive values indicate lighting
	 *         times before sunset. Negative values indicate lighting times
	 *         after nightfall.
	 */
	private int getCandles(Calendar cal, boolean inIsrael) {
		final int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		final boolean isShabbath = (dayOfWeek == Calendar.SATURDAY);

		// Check if the following day is special, because we can't check
		// EREV_CHANUKAH.
		cal.add(Calendar.DAY_OF_MONTH, 1);
		JewishCalendar jcal = new JewishCalendar(cal);
		jcal.setInIsrael(inIsrael);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		int holiday = jcal.getYomTovIndex();

		int candles = CANDLES_NONE;

		switch (holiday) {
		case JewishCalendar.ROSH_HASHANA:
		case JewishCalendar.SUCCOS:
		case JewishCalendar.SHEMINI_ATZERES:
		case JewishCalendar.SIMCHAS_TORAH:
		case JewishCalendar.PESACH:
		case JewishCalendar.SHAVUOS:
			candles = CANDLES_FESTIVAL;
			break;
		case JewishCalendar.YOM_KIPPUR:
			candles = CANDLES_YOM_KIPPUR;
			break;
		case JewishCalendar.CHANUKAH:
			candles = jcal.getDayOfChanukah();
			break;
		default:
			if (dayOfWeek == Calendar.FRIDAY) {
				holiday = SHABBATH;
				candles = CANDLES_SHABBATH;
			}
			break;
		}

		// Forbidden to light candles during Shabbath.
		return isShabbath ? -candles : candles;
	}
}
