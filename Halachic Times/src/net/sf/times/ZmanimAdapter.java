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

	/** 12 hours (half a full day). */
	private static final long TWELVE_HOURS = 12 * DateUtils.HOUR_IN_MILLIS;

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
		String opinion;

		opinion = mSettings.getDawn();
		if ("19.8".equals(opinion)) {
			date = cal.getAlos19Point8Degrees();
			summary = R.string.dawn_19;
		} else if ("120".equals(opinion)) {
			date = cal.getAlos120();
			summary = R.string.dawn_120;
		} else if ("120_zmanis".equals(opinion)) {
			date = cal.getAlos120Zmanis();
			summary = R.string.dawn_120_zmanis;
		} else if ("18".equals(opinion)) {
			date = cal.getAlos18Degrees();
			summary = R.string.dawn_18;
		} else if ("26".equals(opinion)) {
			date = cal.getAlos26Degrees();
			summary = R.string.dawn_26;
		} else if ("16.1".equals(opinion)) {
			date = cal.getAlos16Point1Degrees();
			summary = R.string.dawn_16;
		} else if ("96".equals(opinion)) {
			date = cal.getAlos96();
			summary = R.string.dawn_96;
		} else if ("96_zmanis".equals(opinion)) {
			date = cal.getAlos90Zmanis();
			summary = R.string.dawn_96_zmanis;
		} else if ("90".equals(opinion)) {
			date = cal.getAlos90();
			summary = R.string.dawn_90;
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

		opinion = mSettings.getTallis();
		if ("10.2".equals(opinion)) {
			date = cal.getMisheyakir10Point2Degrees();
			summary = R.string.tallis_10;
		} else if ("11".equals(opinion)) {
			date = cal.getMisheyakir11Degrees();
			summary = R.string.tallis_11;
		} else {
			date = cal.getMisheyakir11Point5Degrees();
			summary = R.string.tallis_summary;
		}
		if (remote)
			add(R.id.tallis_row, R.id.tallis_time, date, true);
		else
			add(R.string.tallis, summary, date);

		opinion = mSettings.getSunrise();
		if ("sea".equals(opinion)) {
			date = cal.getSeaLevelSunrise();
			summary = R.string.sunrise_sea;
		} else {
			date = cal.getSunrise();
			summary = R.string.sunrise_summary;
		}
		if (remote)
			add(R.id.sunrise_row, R.id.sunrise_time, date, true);
		else
			add(R.string.sunrise, summary, date);

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

		opinion = mSettings.getLastTfila();
		if ("120".equals(opinion)) {
			date = cal.getSofZmanTfilaMGA120Minutes();
			summary = R.string.prayers_120;
		} else if ("96".equals(opinion)) {
			date = cal.getSofZmanTfilaMGA96Minutes();
			summary = R.string.prayers_96;
		} else if ("96_zmanis".equals(opinion)) {
			date = cal.getSofZmanTfilaMGA96MinutesZmanis();
			summary = R.string.prayers_96_zmanis;
		} else if ("19.8".equals(opinion)) {
			date = cal.getSofZmanTfilaMGA19Point8Degrees();
			summary = R.string.prayers_19;
		} else if ("90".equals(opinion)) {
			date = cal.getSofZmanTfilaMGA90Minutes();
			summary = R.string.prayers_90;
		} else if ("90_zmanis".equals(opinion)) {
			date = cal.getSofZmanTfilaMGA90MinutesZmanis();
			summary = R.string.prayers_90_zmanis;
		} else if ("AT".equals(opinion)) {
			date = cal.getSofZmanTfilahAteretTorah();
			summary = R.string.prayers_ateret;
		} else if ("18".equals(opinion)) {
			date = cal.getSofZmanTfilaMGA18Degrees();
			summary = R.string.prayers_18;
		} else if ("fixed".equals(opinion)) {
			date = cal.getSofZmanTfilaFixedLocal();
			summary = R.string.prayers_fixed;
		} else if ("16.1".equals(opinion)) {
			date = cal.getSofZmanTfilaMGA16Point1Degrees();
			summary = R.string.prayers_16;
		} else if ("72".equals(opinion)) {
			date = cal.getSofZmanTfilaMGA72Minutes();
			summary = R.string.prayers_72;
		} else if ("72_zmanis".equals(opinion)) {
			date = cal.getSofZmanTfilaMGA72MinutesZmanis();
			summary = R.string.prayers_72_zmanis;
		} else if ("2".equals(opinion)) {
			date = cal.getSofZmanTfila2HoursBeforeChatzos();
			summary = R.string.prayers_2;
		} else if ("GRA".equals(opinion)) {
			date = cal.getSofZmanTfilaGRA();
			summary = R.string.prayers_gra_summary;
		} else {
			date = cal.getSofZmanTfilaMGA();
			summary = R.string.prayers_mga_summary;
		}
		if (remote)
			add(R.id.prayers_row, R.id.prayers_time, date, true);
		else
			add(R.string.prayers, summary, date);

		opinion = mSettings.getMidday();
		if ("fixed".equals(opinion)) {
			date = cal.getFixedLocalChatzos();
			summary = R.string.midday_fixed;
		} else {
			date = cal.getChatzos();
			summary = R.string.midday_summary;
		}
		if (remote)
			add(R.id.midday_row, R.id.midday_time, date, true);
		else
			add(R.string.midday, summary, date);
		Date midday = date;

		opinion = mSettings.getEarliestMincha();
		if ("16.1".equals(opinion)) {
			date = cal.getMinchaGedola16Point1Degrees();
			summary = R.string.earliest_mincha_16;
		} else if ("30".equals(opinion)) {
			date = cal.getMinchaGedola30Minutes();
			summary = R.string.earliest_mincha_30;
		} else if ("AT".equals(opinion)) {
			date = cal.getMinchaGedolaAteretTorah();
			summary = R.string.earliest_mincha_ateret;
		} else if ("72".equals(opinion)) {
			date = cal.getMinchaGedola72Minutes();
			summary = R.string.earliest_mincha_72;
		} else {
			date = cal.getMinchaGedola();
			summary = R.string.earliest_mincha_summary;
		}
		if (remote)
			add(R.id.earliest_mincha_row, R.id.earliest_mincha_time, date, true);
		else
			add(R.string.earliest_mincha, summary, date);

		opinion = mSettings.getMincha();
		if ("16.1".equals(opinion)) {
			date = cal.getMinchaKetana16Point1Degrees();
			summary = R.string.mincha_16;
		} else if ("72".equals(opinion)) {
			date = cal.getMinchaKetana72Minutes();
			summary = R.string.mincha_72;
		} else if ("AT".equals(opinion)) {
			date = cal.getMinchaKetanaAteretTorah();
			summary = R.string.mincha_ateret;
		} else {
			date = cal.getMinchaKetana();
			summary = R.string.mincha_summary;
		}
		if (remote)
			add(R.id.mincha_row, R.id.mincha_time, date, true);
		else
			add(R.string.mincha, summary, date);

		opinion = mSettings.getPlugHamincha();
		if ("alos".equals(opinion)) {
			date = cal.getPlagAlosToSunset();
			summary = R.string.plug_hamincha_alos;
		} else if ("16.1_alos".equals(opinion)) {
			date = cal.getPlagAlos16Point1ToTzaisGeonim7Point083Degrees();
			summary = R.string.plug_hamincha_16_alos;
		} else if ("AT".equals(opinion)) {
			date = cal.getPlagHaminchaAteretTorah();
			summary = R.string.plug_hamincha_ateret;
		} else if ("60".equals(opinion)) {
			date = cal.getPlagHamincha60Minutes();
			summary = R.string.plug_hamincha_60;
		} else if ("72".equals(opinion)) {
			date = cal.getPlagHamincha72Minutes();
			summary = R.string.plug_hamincha_72;
		} else if ("72_zmanis".equals(opinion)) {
			date = cal.getPlagHamincha72MinutesZmanis();
			summary = R.string.plug_hamincha_72_zmanis;
		} else if ("16.1".equals(opinion)) {
			date = cal.getPlagHamincha16Point1Degrees();
			summary = R.string.plug_hamincha_16;
		} else if ("18".equals(opinion)) {
			date = cal.getPlagHamincha18Degrees();
			summary = R.string.plug_hamincha_18;
		} else if ("90".equals(opinion)) {
			date = cal.getPlagHamincha90Minutes();
			summary = R.string.plug_hamincha_90;
		} else if ("90_zmanis".equals(opinion)) {
			date = cal.getPlagHamincha90MinutesZmanis();
			summary = R.string.plug_hamincha_90_zmanis;
		} else if ("19.8".equals(opinion)) {
			date = cal.getPlagHamincha19Point8Degrees();
			summary = R.string.plug_hamincha_19;
		} else if ("96".equals(opinion)) {
			date = cal.getPlagHamincha96Minutes();
			summary = R.string.plug_hamincha_96;
		} else if ("96_zmanis".equals(opinion)) {
			date = cal.getPlagHamincha96MinutesZmanis();
			summary = R.string.plug_hamincha_96_zmanis;
		} else if ("120".equals(opinion)) {
			date = cal.getPlagHamincha120Minutes();
			summary = R.string.plug_hamincha_120;
		} else if ("120_zmanis".equals(opinion)) {
			date = cal.getPlagHamincha120MinutesZmanis();
			summary = R.string.plug_hamincha_120_zmanis;
		} else if ("26".equals(opinion)) {
			date = cal.getPlagHamincha26Degrees();
			summary = R.string.plug_hamincha_26;
		} else {
			date = cal.getPlagHamincha();
			summary = R.string.plug_hamincha_summary;
		}
		if (remote)
			add(R.id.plug_hamincha_row, R.id.plug_hamincha_time, date, true);
		else
			add(R.string.plug_hamincha, summary, date);

		date = cal.getCandleLighting();
		if (remote)
			add(R.id.candles_row, R.id.candles_time, (candlesCount > 0) ? date : null, true);
		else if (candlesCount > 0) {
			String summaryText = mContext.getString(R.string.candles_summary, mCandlesOffset);
			add(R.string.candles, summaryText, date);
		}

		opinion = mSettings.getSunset();
		if ("sea".equals(opinion)) {
			date = cal.getSeaLevelSunset();
			summary = R.string.sunset_sea;
		} else {
			date = cal.getSunset();
			summary = R.string.sunset_summary;
		}
		if (remote)
			add(R.id.sunset_row, R.id.sunset_time, date, true);
		else
			add(R.string.sunset, summary, date);

		opinion = mSettings.getNightfall();
		if ("120".equals(opinion)) {
			date = cal.getTzais120();
			summary = R.string.nightfall_120;
		} else if ("120_zmanis".equals(opinion)) {
			date = cal.getTzais120Zmanis();
			summary = R.string.nightfall_120_zmanis;
		} else if ("16.1".equals(opinion)) {
			date = cal.getTzais16Point1Degrees();
			summary = R.string.nightfall_16;
		} else if ("18".equals(opinion)) {
			date = cal.getTzais18Degrees();
			summary = R.string.nightfall_18;
		} else if ("19.8".equals(opinion)) {
			date = cal.getTzais19Point8Degrees();
			summary = R.string.nightfall_19;
		} else if ("26".equals(opinion)) {
			date = cal.getTzais26Degrees();
			summary = R.string.nightfall_26;
		} else if ("60".equals(opinion)) {
			date = cal.getTzais60();
			summary = R.string.nightfall_60;
		} else if ("72".equals(opinion)) {
			date = cal.getTzais72();
			summary = R.string.nightfall_72;
		} else if ("72_zmanis".equals(opinion)) {
			date = cal.getTzais72Zmanis();
			summary = R.string.nightfall_72_zmanis;
		} else if ("90".equals(opinion)) {
			date = cal.getTzais90();
			summary = R.string.nightfall_90;
		} else if ("90_zmanis".equals(opinion)) {
			date = cal.getTzais90Zmanis();
			summary = R.string.nightfall_90_zmanis;
		} else if ("96".equals(opinion)) {
			date = cal.getTzais96();
			summary = R.string.nightfall_96;
		} else if ("96_zmanis".equals(opinion)) {
			date = cal.getTzais96Zmanis();
			summary = R.string.nightfall_96_zmanis;
		} else if ("AT".equals(opinion)) {
			date = cal.getTzaisAteretTorah();
			summary = R.string.nightfall_ateret;
		} else if ("3.65".equals(opinion)) {
			date = cal.getTzaisGeonim3Point65Degrees();
			summary = R.string.nightfall_3_65;
		} else if ("4.37".equals(opinion)) {
			date = cal.getTzaisGeonim4Point37Degrees();
			summary = R.string.nightfall_4_37;
		} else if ("4.61".equals(opinion)) {
			date = cal.getTzaisGeonim4Point61Degrees();
			summary = R.string.nightfall_4_61;
		} else if ("4.8".equals(opinion)) {
			date = cal.getTzaisGeonim4Point8Degrees();
			summary = R.string.nightfall_4_8;
		} else if ("5.88".equals(opinion)) {
			date = cal.getTzaisGeonim5Point88Degrees();
			summary = R.string.nightfall_5_88;
		} else if ("5.95".equals(opinion)) {
			date = cal.getTzaisGeonim5Point95Degrees();
			summary = R.string.nightfall_5_95;
		} else if ("7.083".equals(opinion)) {
			date = cal.getTzaisGeonim7Point083Degrees();
			summary = R.string.nightfall_7;
		} else if ("8.5".equals(opinion)) {
			date = cal.getTzaisGeonim8Point5Degrees();
			summary = R.string.nightfall_8;
		} else {
			date = cal.getTzais();
			summary = R.string.nightfall_3stars;
		}
		if (remote) {
			add(R.id.candles2_row, R.id.candles2_time, (candlesCount < 0) ? date : null, true);
			add(R.id.nightfall_row, R.id.nightfall_time, date, true);
		} else if (candlesCount < 0) {
			add(R.string.candles, summary, date);
			add(R.string.nightfall, summary, date);
		}

		opinion = mSettings.getMidnight();
		if ("12".equals(opinion)) {
			date = midday;
			if (date != null)
				date.setTime(date.getTime() + TWELVE_HOURS);
			summary = R.string.midnight_12;
		} else {
			date = cal.getSolarMidnight();
			summary = R.string.midnight_summary;
		}
		if (remote)
			add(R.id.midnight_row, R.id.midnight_time, date, true);
		else
			add(R.string.midnight, summary, date);
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
