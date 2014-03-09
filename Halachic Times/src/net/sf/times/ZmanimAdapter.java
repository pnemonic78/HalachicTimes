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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;
import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Adapter for halachic times list.
 * <p>
 * See also Wikipedia article on <a
 * href="http://en.wikipedia.org/wiki/Zmanim">Zmanim</a>.
 * 
 * @author Moshe Waisberg
 */
public class ZmanimAdapter extends ArrayAdapter<ZmanimItem> {

	/** 12 hours (half a full day). */
	protected static final long TWELVE_HOURS = DateUtils.DAY_IN_MILLIS >> 1;

	/** Holiday id for Shabbath. */
	public static final int SHABBATH = 100;

	/** No candles to light. */
	private static final int CANDLES_NONE = 0;
	/** Number of candles to light for Shabbath. */
	private static final int CANDLES_SHABBATH = 2;
	/** Number of candles to light for a festival. */
	private static final int CANDLES_FESTIVAL = 2;
	/** Number of candles to light for Yom Kippurim. */
	private static final int CANDLES_YOM_KIPPUR = 1;

	/** Flag indicating lighting times before sunset. */
	private static final int BEFORE_SUNSET = 0x00000000;
	/** Flag indicating lighting times at sunset. */
	private static final int AT_SUNSET = 0x10000000;
	/** Flag indicating lighting times after nightfall. */
	private static final int MOTZE_SHABBATH = 0x20000000;

	protected static final int CANDLES_MASK = 0x0000000F;
	protected static final int HOLIDAY_MASK = 0x000000FF;
	protected static final int MOTZE_MASK = 0xF0000000;

	private static final String OPINION_10_2 = "10.2";
	private static final String OPINION_11 = "11";
	private static final String OPINION_12 = "12";
	private static final String OPINION_120 = "120";
	private static final String OPINION_120_ZMANIS = "120_zmanis";
	private static final Object OPINION_13 = "13.24";
	private static final String OPINION_15 = "15";
	private static final String OPINION_16_1 = "16.1";
	private static final String OPINION_16_1_ALOS = "16.1_alos";
	private static final String OPINION_16_1_SUNSET = "16.1_sunset";
	private static final String OPINION_18 = "18";
	private static final String OPINION_19_8 = "19.8";
	private static final String OPINION_2 = "2";
	private static final String OPINION_26 = "26";
	private static final String OPINION_3 = "3";
	private static final String OPINION_3_65 = "3.65";
	private static final String OPINION_3_676 = "3.676";
	private static final String OPINION_30 = "30";
	private static final String OPINION_4_37 = "4.37";
	private static final String OPINION_4_61 = "4.61";
	private static final String OPINION_4_8 = "4.8";
	private static final String OPINION_5_88 = "5.88";
	private static final String OPINION_5_95 = "5.95";
	private static final Object OPINION_58 = "58.5";
	private static final String OPINION_60 = "60";
	private static final String OPINION_7_083 = "7.083";
	private static final String OPINION_72 = "72";
	private static final String OPINION_72_ZMANIS = "72_zmanis";
	private static final String OPINION_8_5 = "8.5";
	private static final String OPINION_90 = "90";
	private static final String OPINION_90_ZMANIS = "90_zmanis";
	private static final String OPINION_96 = "96";
	private static final String OPINION_96_ZMANIS = "96_zmanis";
	private static final String OPINION_ATERET = "AT";
	private static final String OPINION_GRA = "GRA";
	private static final String OPINION_MGA = "MGA";
	protected static final String OPINION_FIXED = "fixed";
	private static final String OPINION_SEA = "sea";

	protected final LayoutInflater mInflater;
	protected final ZmanimSettings mSettings;
	protected final ComplexZmanimCalendar mCalendar;
	protected final boolean mInIsrael;
	protected final long mNow = System.currentTimeMillis();
	protected final boolean mSummaries;
	protected final boolean mElapsed;
	protected final int mCandlesOffset;
	private final DateFormat mTimeFormat;
	private Comparator<ZmanimItem> mComparator;

	/**
	 * Time row item.
	 */
	protected static class ZmanimItem implements Comparable<ZmanimItem> {

		/** The title id. */
		public int titleId;
		/** The summary. */
		public CharSequence summary;
		/** The time text id. */
		public int timeId;
		/** The time. */
		public Date time;
		/** The time label. */
		public CharSequence timeLabel;
		/** Has the time elapsed? */
		public boolean elapsed;

		/**
		 * Creates a new row item.
		 */
		public ZmanimItem() {
			super();
		}

		@Override
		public int compareTo(ZmanimItem another) {
			long t1 = this.time.getTime();
			long t2 = another.time.getTime();
			if (t1 == t2)
				return this.titleId - another.titleId;
			return (t1 < t2) ? -1 : +1;
		}
	}

	/**
	 * Compare two time items.
	 */
	protected static class ZmanimComparator implements Comparator<ZmanimItem> {
		@Override
		public int compare(ZmanimItem lhs, ZmanimItem rhs) {
			return lhs.compareTo(rhs);
		}
	}

	/**
	 * Creates a new adapter.
	 * 
	 * @param context
	 *            the context.
	 * @param settings
	 *            the application settings.
	 * @param cal
	 *            the calendar.
	 * @param inIsrael
	 *            is in Israel?
	 */
	public ZmanimAdapter(Context context, ZmanimSettings settings, ComplexZmanimCalendar cal, boolean inIsrael) {
		super(context, R.layout.times_item, 0);
		mInflater = LayoutInflater.from(context);
		mSettings = settings;
		mSummaries = settings.isSummaries();
		mElapsed = settings.isPast();
		mCandlesOffset = settings.getCandleLightingOffset();
		mCalendar = cal;
		mInIsrael = inIsrael;

		if (settings.isSeconds()) {
			boolean time24 = android.text.format.DateFormat.is24HourFormat(context);
			String pattern = context.getString(time24 ? R.string.twenty_four_hour_time_format : R.string.twelve_hour_time_format);
			mTimeFormat = new SimpleDateFormat(pattern, Locale.getDefault());
		} else {
			mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
		}
	}

	/**
	 * Get the calendar.
	 * 
	 * @return the calendar.
	 */
	public ComplexZmanimCalendar getCalendar() {
		return mCalendar;
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
	protected View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
		ZmanimItem item = getItem(position);
		boolean enabled = !item.elapsed;

		View view = convertView;
		ViewHolder holder;
		TextView title;
		TextView summary;
		TextView time;

		if (view == null) {
			view = mInflater.inflate(resource, parent, false);

			title = (TextView) view.findViewById(android.R.id.title);
			summary = (TextView) view.findViewById(android.R.id.summary);
			time = (TextView) view.findViewById(R.id.time);

			holder = new ViewHolder(title, summary, time);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
			title = holder.title;
			summary = holder.summary;
			time = holder.time;
		}
		view.setEnabled(enabled);
		view.setTag(R.id.time, item);

		title.setText(item.titleId);
		title.setEnabled(enabled);

		if (summary != null) {
			summary.setText(item.summary);
			summary.setEnabled(enabled);
			if (item.summary == null)
				summary.setVisibility(View.GONE);
		}

		time.setText(item.timeLabel);
		time.setEnabled(enabled);

		return view;
	}

	/**
	 * Adds the item to the array for a valid time.
	 * 
	 * @param titleId
	 *            the title label id.
	 * @param summaryId
	 *            the summary label id.
	 * @param time
	 *            the time.
	 */
	public void add(int titleId, int summaryId, Date time) {
		add(titleId, mSummaries && (summaryId != 0) ? getContext().getText(summaryId) : (CharSequence) null, time);
	}

	/**
	 * Adds the item to the array for a valid time.
	 * 
	 * @param titleId
	 *            the title label id.
	 * @param summary
	 *            the summary label.
	 * @param time
	 *            the time in milliseconds.
	 */
	public void add(int titleId, CharSequence summary, Date time) {
		add(titleId, summary, titleId, time, false);
	}

	/**
	 * Adds the item to the array for a valid date.
	 * 
	 * @param titleId
	 *            the row layout id.
	 * @param time
	 *            the time.
	 * @param timeId
	 *            the time text id to set for remote views.
	 */
	public void add(int titleId, Date time, int timeId) {
		add(titleId, null, timeId, time, true);
	}

	/**
	 * Adds the item to the array for a valid date.
	 * 
	 * @param titleId
	 *            the row layout id.
	 * @param summary
	 *            the summary label.
	 * @param timeId
	 *            the time text id to set for remote views.
	 * @param time
	 *            the time.
	 * @param set
	 *            set for remote views?
	 */
	private void add(int titleId, CharSequence summary, int timeId, Date time, boolean set) {
		if (time == null)
			return;
		long t = time.getTime();

		ZmanimItem item = new ZmanimItem();
		item.titleId = titleId;
		item.summary = mSummaries ? summary : null;
		item.timeId = timeId;
		item.time = time;
		item.timeLabel = mTimeFormat.format(time);
		if (set)
			item.elapsed = (t < mNow);
		else
			item.elapsed = mElapsed ? false : (t < mNow);

		add(item);
	}

	/**
	 * Populate the list of times.
	 * 
	 * @param remote
	 *            is for remote views?
	 */
	public void populate(boolean remote) {
		ComplexZmanimCalendar cal = mCalendar;
		cal.setCandleLightingOffset(mCandlesOffset);
		Calendar gcal = cal.getCalendar();
		JewishCalendar jcal = new JewishCalendar(gcal);
		jcal.setInIsrael(mInIsrael);
		int candles = getCandles(jcal);
		int candlesCount = candles & CANDLES_MASK;
		boolean hasCandles = candlesCount > 0;
		int candlesHow = candles & MOTZE_MASK;
		int holidayTomorrow = (candles >> 4) & HOLIDAY_MASK;
		int holidayToday = (candles >> 12) & HOLIDAY_MASK;

		Date date;
		int summary;
		String opinion;
		final Resources res = getContext().getResources();

		opinion = mSettings.getDawn();
		if (OPINION_19_8.equals(opinion)) {
			date = cal.getAlos19Point8Degrees();
			summary = R.string.dawn_19;
		} else if (OPINION_120.equals(opinion)) {
			date = cal.getAlos120();
			summary = R.string.dawn_120;
		} else if (OPINION_120_ZMANIS.equals(opinion)) {
			date = cal.getAlos120Zmanis();
			summary = R.string.dawn_120_zmanis;
		} else if (OPINION_18.equals(opinion)) {
			date = cal.getAlos18Degrees();
			summary = R.string.dawn_18;
		} else if (OPINION_26.equals(opinion)) {
			date = cal.getAlos26Degrees();
			summary = R.string.dawn_26;
		} else if (OPINION_16_1.equals(opinion)) {
			date = cal.getAlos16Point1Degrees();
			summary = R.string.dawn_16;
		} else if (OPINION_96.equals(opinion)) {
			date = cal.getAlos96();
			summary = R.string.dawn_96;
		} else if (OPINION_96_ZMANIS.equals(opinion)) {
			date = cal.getAlos90Zmanis();
			summary = R.string.dawn_96_zmanis;
		} else if (OPINION_90.equals(opinion)) {
			date = cal.getAlos90();
			summary = R.string.dawn_90;
		} else if (OPINION_90_ZMANIS.equals(opinion)) {
			date = cal.getAlos90Zmanis();
			summary = R.string.dawn_90_zmanis;
		} else if (OPINION_72.equals(opinion)) {
			date = cal.getAlos72();
			summary = R.string.dawn_72;
		} else if (OPINION_72_ZMANIS.equals(opinion)) {
			date = cal.getAlos72Zmanis();
			summary = R.string.dawn_72_zmanis;
		} else if (OPINION_60.equals(opinion)) {
			date = cal.getAlos60();
			summary = R.string.dawn_60;
		} else {
			date = cal.getAlosHashachar();
			summary = R.string.dawn_16;
		}
		if (remote)
			add(R.id.dawn_row, date, R.id.dawn_time);
		else
			add(R.string.dawn, summary, date);

		opinion = mSettings.getTallis();
		if (OPINION_10_2.equals(opinion)) {
			date = cal.getMisheyakir10Point2Degrees();
			summary = R.string.tallis_10;
		} else if (OPINION_11.equals(opinion)) {
			date = cal.getMisheyakir11Degrees();
			summary = R.string.tallis_11;
		} else {
			date = cal.getMisheyakir11Point5Degrees();
			summary = R.string.tallis_summary;
		}
		if (remote)
			add(R.id.tallis_row, date, R.id.tallis_time);
		else
			add(R.string.tallis, summary, date);

		opinion = mSettings.getSunrise();
		if (OPINION_SEA.equals(opinion)) {
			date = cal.getSeaLevelSunrise();
			summary = R.string.sunrise_sea;
		} else {
			date = cal.getSunrise();
			summary = R.string.sunrise_summary;
		}
		if (remote)
			add(R.id.sunrise_row, date, R.id.sunrise_time);
		else
			add(R.string.sunrise, summary, date);

		opinion = mSettings.getLastShema();
		if (OPINION_16_1_SUNSET.equals(opinion)) {
			date = cal.getSofZmanShmaAlos16Point1ToSunset();
			summary = R.string.shema_16_sunset;
		} else if (OPINION_7_083.equals(opinion)) {
			date = cal.getSofZmanShmaAlos16Point1ToTzaisGeonim7Point083Degrees();
			summary = R.string.shema_7;
		} else if (OPINION_19_8.equals(opinion)) {
			date = cal.getSofZmanShmaMGA19Point8Degrees();
			summary = R.string.shema_19;
		} else if (OPINION_120.equals(opinion)) {
			date = cal.getSofZmanShmaMGA120Minutes();
			summary = R.string.shema_120;
		} else if (OPINION_18.equals(opinion)) {
			date = cal.getSofZmanShmaMGA18Degrees();
			summary = R.string.shema_18;
		} else if (OPINION_96.equals(opinion)) {
			date = cal.getSofZmanShmaMGA96Minutes();
			summary = R.string.shema_96;
		} else if (OPINION_96_ZMANIS.equals(opinion)) {
			date = cal.getSofZmanShmaMGA96MinutesZmanis();
			summary = R.string.shema_96_zmanis;
		} else if (OPINION_16_1.equals(opinion)) {
			date = cal.getSofZmanShmaMGA16Point1Degrees();
			summary = R.string.shema_16;
		} else if (OPINION_90.equals(opinion)) {
			date = cal.getSofZmanShmaMGA90Minutes();
			summary = R.string.shema_90;
		} else if (OPINION_90_ZMANIS.equals(opinion)) {
			date = cal.getSofZmanShmaMGA90MinutesZmanis();
			summary = R.string.shema_90_zmanis;
		} else if (OPINION_72.equals(opinion)) {
			date = cal.getSofZmanShmaMGA72Minutes();
			summary = R.string.shema_72;
		} else if (OPINION_72_ZMANIS.equals(opinion)) {
			date = cal.getSofZmanShmaMGA72MinutesZmanis();
			summary = R.string.shema_72_zmanis;
		} else if (OPINION_MGA.equals(opinion)) {
			date = cal.getSofZmanShmaMGA();
			summary = R.string.shema_mga;
		} else if (OPINION_ATERET.equals(opinion)) {
			date = cal.getSofZmanShmaAteretTorah();
			summary = R.string.shema_ateret;
		} else if (OPINION_3.equals(opinion)) {
			date = cal.getSofZmanShma3HoursBeforeChatzos();
			summary = R.string.shema_3;
		} else if (OPINION_FIXED.equals(opinion)) {
			date = cal.getSofZmanShmaFixedLocal();
			summary = R.string.shema_fixed;
		} else if (OPINION_GRA.equals(opinion)) {
			date = cal.getSofZmanShmaGRA();
			summary = R.string.shema_gra;
		} else {
			date = cal.getSofZmanShmaMGA();
			summary = R.string.shema_mga;
		}
		if (remote)
			add(R.id.shema_row, date, R.id.shema_time);
		else
			add(R.string.shema, summary, date);

		opinion = mSettings.getLastTfila();
		if (OPINION_120.equals(opinion)) {
			date = cal.getSofZmanTfilaMGA120Minutes();
			summary = R.string.prayers_120;
		} else if (OPINION_96.equals(opinion)) {
			date = cal.getSofZmanTfilaMGA96Minutes();
			summary = R.string.prayers_96;
		} else if (OPINION_96_ZMANIS.equals(opinion)) {
			date = cal.getSofZmanTfilaMGA96MinutesZmanis();
			summary = R.string.prayers_96_zmanis;
		} else if (OPINION_19_8.equals(opinion)) {
			date = cal.getSofZmanTfilaMGA19Point8Degrees();
			summary = R.string.prayers_19;
		} else if (OPINION_90.equals(opinion)) {
			date = cal.getSofZmanTfilaMGA90Minutes();
			summary = R.string.prayers_90;
		} else if (OPINION_90_ZMANIS.equals(opinion)) {
			date = cal.getSofZmanTfilaMGA90MinutesZmanis();
			summary = R.string.prayers_90_zmanis;
		} else if (OPINION_ATERET.equals(opinion)) {
			date = cal.getSofZmanTfilahAteretTorah();
			summary = R.string.prayers_ateret;
		} else if (OPINION_18.equals(opinion)) {
			date = cal.getSofZmanTfilaMGA18Degrees();
			summary = R.string.prayers_18;
		} else if (OPINION_FIXED.equals(opinion)) {
			date = cal.getSofZmanTfilaFixedLocal();
			summary = R.string.prayers_fixed;
		} else if (OPINION_16_1.equals(opinion)) {
			date = cal.getSofZmanTfilaMGA16Point1Degrees();
			summary = R.string.prayers_16;
		} else if (OPINION_72.equals(opinion)) {
			date = cal.getSofZmanTfilaMGA72Minutes();
			summary = R.string.prayers_72;
		} else if (OPINION_72_ZMANIS.equals(opinion)) {
			date = cal.getSofZmanTfilaMGA72MinutesZmanis();
			summary = R.string.prayers_72_zmanis;
		} else if (OPINION_2.equals(opinion)) {
			date = cal.getSofZmanTfila2HoursBeforeChatzos();
			summary = R.string.prayers_2;
		} else if (OPINION_GRA.equals(opinion)) {
			date = cal.getSofZmanTfilaGRA();
			summary = R.string.prayers_gra;
		} else {
			date = cal.getSofZmanTfilaMGA();
			summary = R.string.prayers_mga;
		}
		if (remote)
			add(R.id.prayers_row, date, R.id.prayers_time);
		else {
			add(R.string.prayers, summary, date);
			if (holidayToday == JewishCalendar.EREV_PESACH)
				add(R.string.eat_chametz, summary, date);
		}

		if (!remote && (holidayToday == JewishCalendar.EREV_PESACH)) {
			opinion = mSettings.getBurnChametz();
			if (OPINION_16_1.equals(opinion)) {
				date = cal.getSofZmanBiurChametzMGA16Point1Degrees();
				summary = R.string.burn_chametz_16;
			} else if (OPINION_72.equals(opinion)) {
				date = cal.getSofZmanBiurChametzMGA72Minutes();
				summary = R.string.burn_chametz_72;
			} else {
				date = cal.getSofZmanBiurChametzGRA();
				summary = R.string.burn_chametz_gra;
			}
			add(R.string.burn_chametz, summary, date);
		}

		opinion = mSettings.getMidday();
		if (OPINION_FIXED.equals(opinion)) {
			date = cal.getFixedLocalChatzos();
			summary = R.string.midday_fixed;
		} else {
			date = cal.getChatzos();
			summary = R.string.midday_summary;
		}
		if (remote)
			add(R.id.midday_row, date, R.id.midday_time);
		else
			add(R.string.midday, summary, date);
		Date midday = date;

		opinion = mSettings.getEarliestMincha();
		if (OPINION_16_1.equals(opinion)) {
			date = cal.getMinchaGedola16Point1Degrees();
			summary = R.string.earliest_mincha_16;
		} else if (OPINION_30.equals(opinion)) {
			date = cal.getMinchaGedola30Minutes();
			summary = R.string.earliest_mincha_30;
		} else if (OPINION_ATERET.equals(opinion)) {
			date = cal.getMinchaGedolaAteretTorah();
			summary = R.string.earliest_mincha_ateret;
		} else if (OPINION_72.equals(opinion)) {
			date = cal.getMinchaGedola72Minutes();
			summary = R.string.earliest_mincha_72;
		} else {
			date = cal.getMinchaGedola();
			summary = R.string.earliest_mincha_summary;
		}
		if (remote)
			add(R.id.earliest_mincha_row, date, R.id.earliest_mincha_time);
		else
			add(R.string.earliest_mincha, summary, date);

		opinion = mSettings.getMincha();
		if (OPINION_16_1.equals(opinion)) {
			date = cal.getMinchaKetana16Point1Degrees();
			summary = R.string.mincha_16;
		} else if (OPINION_72.equals(opinion)) {
			date = cal.getMinchaKetana72Minutes();
			summary = R.string.mincha_72;
		} else if (OPINION_ATERET.equals(opinion)) {
			date = cal.getMinchaKetanaAteretTorah();
			summary = R.string.mincha_ateret;
		} else {
			date = cal.getMinchaKetana();
			summary = R.string.mincha_summary;
		}
		if (remote)
			add(R.id.mincha_row, date, R.id.mincha_time);
		else
			add(R.string.mincha, summary, date);

		opinion = mSettings.getPlugHamincha();
		if (OPINION_16_1_SUNSET.equals(opinion)) {
			date = cal.getPlagAlosToSunset();
			summary = R.string.plug_hamincha_16_sunset;
		} else if (OPINION_16_1_ALOS.equals(opinion)) {
			date = cal.getPlagAlos16Point1ToTzaisGeonim7Point083Degrees();
			summary = R.string.plug_hamincha_16_alos;
		} else if (OPINION_ATERET.equals(opinion)) {
			date = cal.getPlagHaminchaAteretTorah();
			summary = R.string.plug_hamincha_ateret;
		} else if (OPINION_60.equals(opinion)) {
			date = cal.getPlagHamincha60Minutes();
			summary = R.string.plug_hamincha_60;
		} else if (OPINION_72.equals(opinion)) {
			date = cal.getPlagHamincha72Minutes();
			summary = R.string.plug_hamincha_72;
		} else if (OPINION_72_ZMANIS.equals(opinion)) {
			date = cal.getPlagHamincha72MinutesZmanis();
			summary = R.string.plug_hamincha_72_zmanis;
		} else if (OPINION_16_1.equals(opinion)) {
			date = cal.getPlagHamincha16Point1Degrees();
			summary = R.string.plug_hamincha_16;
		} else if (OPINION_18.equals(opinion)) {
			date = cal.getPlagHamincha18Degrees();
			summary = R.string.plug_hamincha_18;
		} else if (OPINION_90.equals(opinion)) {
			date = cal.getPlagHamincha90Minutes();
			summary = R.string.plug_hamincha_90;
		} else if (OPINION_90_ZMANIS.equals(opinion)) {
			date = cal.getPlagHamincha90MinutesZmanis();
			summary = R.string.plug_hamincha_90_zmanis;
		} else if (OPINION_19_8.equals(opinion)) {
			date = cal.getPlagHamincha19Point8Degrees();
			summary = R.string.plug_hamincha_19;
		} else if (OPINION_96.equals(opinion)) {
			date = cal.getPlagHamincha96Minutes();
			summary = R.string.plug_hamincha_96;
		} else if (OPINION_96_ZMANIS.equals(opinion)) {
			date = cal.getPlagHamincha96MinutesZmanis();
			summary = R.string.plug_hamincha_96_zmanis;
		} else if (OPINION_120.equals(opinion)) {
			date = cal.getPlagHamincha120Minutes();
			summary = R.string.plug_hamincha_120;
		} else if (OPINION_120_ZMANIS.equals(opinion)) {
			date = cal.getPlagHamincha120MinutesZmanis();
			summary = R.string.plug_hamincha_120_zmanis;
		} else if (OPINION_26.equals(opinion)) {
			date = cal.getPlagHamincha26Degrees();
			summary = R.string.plug_hamincha_26;
		} else {
			date = cal.getPlagHamincha();
			summary = R.string.plug_hamincha_gra;
		}
		if (remote)
			add(R.id.plug_hamincha_row, date, R.id.plug_hamincha_time);
		else
			add(R.string.plug_hamincha, summary, date);

		date = cal.getCandleLighting();
		if (hasCandles && (candlesHow == BEFORE_SUNSET)) {
			if (remote) {
				add(R.id.candles_row, date, R.id.candles_time);
			} else {
				String summaryText;
				if (holidayTomorrow == JewishCalendar.CHANUKAH) {
					summaryText = res.getQuantityString(R.plurals.candles_chanukka, candlesCount, candlesCount);
				} else {
					summaryText = getContext().getString(R.string.candles_summary, mCandlesOffset);
				}
				add(R.string.candles, summaryText, date);
			}
		} else if (remote) {
			add(R.id.candles_row, null, R.id.candles_time);
		}

		opinion = mSettings.getSunset();
		if (OPINION_SEA.equals(opinion)) {
			date = cal.getSeaLevelSunset();
			summary = R.string.sunset_sea;
		} else {
			date = cal.getSunset();
			summary = R.string.sunset_summary;
		}
		if (hasCandles && (candlesHow == AT_SUNSET)) {
			if (remote) {
				add(R.id.candles_row, date, R.id.candles_time);
			} else if (holidayTomorrow == JewishCalendar.CHANUKAH) {
				String summaryText = res.getQuantityString(R.plurals.candles_chanukka, candlesCount, candlesCount);
				add(R.string.candles, summaryText, date);
			} else {
				add(R.string.candles, summary, date);
			}
		} else if (remote) {
			add(R.id.candles_row, null, R.id.candles_time);
		}
		if (remote)
			add(R.id.sunset_row, date, R.id.sunset_time);
		else
			add(R.string.sunset, summary, date);

		opinion = mSettings.getTwilight();
		if (OPINION_7_083.equals(opinion)) {
			date = cal.getBainHasmashosRT13Point5MinutesBefore7Point083Degrees();
			summary = R.string.twilight_7_083;
		} else if (OPINION_58.equals(opinion)) {
			date = cal.getBainHasmashosRT58Point5Minutes();
			summary = R.string.twilight_58;
		} else if (OPINION_13.equals(opinion)) {
			date = cal.getBainHasmashosRT13Point24Degrees();
			summary = R.string.twilight_13;
		} else {
			date = cal.getBainHasmashosRT2Stars();
			summary = R.string.twilight_2stars;
		}
		if (remote)
			add(R.id.twilight_row, date, R.id.twilight_time);
		else
			add(R.string.twilight, summary, date);

		opinion = mSettings.getNightfall();
		if (OPINION_120.equals(opinion)) {
			date = cal.getTzais120();
			summary = R.string.nightfall_120;
		} else if (OPINION_120_ZMANIS.equals(opinion)) {
			date = cal.getTzais120Zmanis();
			summary = R.string.nightfall_120_zmanis;
		} else if (OPINION_16_1.equals(opinion)) {
			date = cal.getTzais16Point1Degrees();
			summary = R.string.nightfall_16;
		} else if (OPINION_18.equals(opinion)) {
			date = cal.getTzais18Degrees();
			summary = R.string.nightfall_18;
		} else if (OPINION_19_8.equals(opinion)) {
			date = cal.getTzais19Point8Degrees();
			summary = R.string.nightfall_19;
		} else if (OPINION_26.equals(opinion)) {
			date = cal.getTzais26Degrees();
			summary = R.string.nightfall_26;
		} else if (OPINION_60.equals(opinion)) {
			date = cal.getTzais60();
			summary = R.string.nightfall_60;
		} else if (OPINION_72.equals(opinion)) {
			date = cal.getTzais72();
			summary = R.string.nightfall_72;
		} else if (OPINION_72_ZMANIS.equals(opinion)) {
			date = cal.getTzais72Zmanis();
			summary = R.string.nightfall_72_zmanis;
		} else if (OPINION_90.equals(opinion)) {
			date = cal.getTzais90();
			summary = R.string.nightfall_90;
		} else if (OPINION_90_ZMANIS.equals(opinion)) {
			date = cal.getTzais90Zmanis();
			summary = R.string.nightfall_90_zmanis;
		} else if (OPINION_96.equals(opinion)) {
			date = cal.getTzais96();
			summary = R.string.nightfall_96;
		} else if (OPINION_96_ZMANIS.equals(opinion)) {
			date = cal.getTzais96Zmanis();
			summary = R.string.nightfall_96_zmanis;
		} else if (OPINION_ATERET.equals(opinion)) {
			date = cal.getTzaisAteretTorah();
			summary = R.string.nightfall_ateret;
		} else if (OPINION_3_65.equals(opinion)) {
			date = cal.getTzaisGeonim3Point65Degrees();
			summary = R.string.nightfall_3_65;
		} else if (OPINION_3_676.equals(opinion)) {
			date = cal.getTzaisGeonim3Point676Degrees();
			summary = R.string.nightfall_3_676;
		} else if (OPINION_4_37.equals(opinion)) {
			date = cal.getTzaisGeonim4Point37Degrees();
			summary = R.string.nightfall_4_37;
		} else if (OPINION_4_61.equals(opinion)) {
			date = cal.getTzaisGeonim4Point61Degrees();
			summary = R.string.nightfall_4_61;
		} else if (OPINION_4_8.equals(opinion)) {
			date = cal.getTzaisGeonim4Point8Degrees();
			summary = R.string.nightfall_4_8;
		} else if (OPINION_5_88.equals(opinion)) {
			date = cal.getTzaisGeonim5Point88Degrees();
			summary = R.string.nightfall_5_88;
		} else if (OPINION_5_95.equals(opinion)) {
			date = cal.getTzaisGeonim5Point95Degrees();
			summary = R.string.nightfall_5_95;
		} else if (OPINION_7_083.equals(opinion)) {
			date = cal.getTzaisGeonim7Point083Degrees();
			summary = R.string.nightfall_7;
		} else if (OPINION_8_5.equals(opinion)) {
			date = cal.getTzaisGeonim8Point5Degrees();
			summary = R.string.nightfall_8;
		} else {
			date = cal.getTzais();
			summary = R.string.nightfall_3stars;
		}
		if (remote)
			add(R.id.nightfall_row, date, R.id.nightfall_time);
		else
			add(R.string.nightfall, summary, date);
		if (hasCandles && (candlesHow == MOTZE_SHABBATH)) {
			if (remote) {
				add(R.id.candles_nightfall_row, date, R.id.candles_nightfall_time);
			} else if (holidayTomorrow == JewishCalendar.CHANUKAH) {
				String summaryText = res.getQuantityString(R.plurals.candles_chanukka, candlesCount, candlesCount);
				add(R.string.candles, summaryText, date);
			} else {
				add(R.string.candles, summary, date);
			}
		} else if (remote) {
			add(R.id.candles_nightfall_row, null, R.id.candles_nightfall_time);
		}

		opinion = mSettings.getMidnight();
		if (OPINION_12.equals(opinion)) {
			date = midday;
			if (date != null)
				date.setTime(date.getTime() + TWELVE_HOURS);
			summary = R.string.midnight_12;
		} else {
			date = cal.getSolarMidnight();
			summary = R.string.midnight_summary;
		}
		if (remote)
			add(R.id.midnight_row, date, R.id.midnight_time);
		else
			add(R.string.midnight, summary, date);

		if (!remote) {
			final int jDayOfMonth = jcal.getJewishDayOfMonth();
			// Molad.
			if ((jDayOfMonth <= 2) || (jDayOfMonth >= 25)) {
				int y = gcal.get(Calendar.YEAR);
				int m = gcal.get(Calendar.MONTH);
				int d = gcal.get(Calendar.DAY_OF_MONTH);
				jcal.forward();// Molad is always of the previous month.
				JewishDate molad = jcal.getMolad();
				int moladYear = molad.getGregorianYear();
				int moladMonth = molad.getGregorianMonth();
				int moladDay = molad.getGregorianDayOfMonth();
				if ((moladYear == y) && (moladMonth == m) && (moladDay == d)) {
					double moladSeconds = (molad.getMoladChalakim() * 10.0) / 3.0;
					double moladSecondsFloor = Math.floor(moladSeconds);
					Calendar calMolad = (Calendar) gcal.clone();
					calMolad.set(moladYear, moladMonth, moladDay, molad.getMoladHours(), molad.getMoladMinutes(), (int) moladSecondsFloor);
					calMolad.set(Calendar.MILLISECOND, (int) (DateUtils.SECOND_IN_MILLIS * (moladSeconds - moladSecondsFloor)));
					summary = R.string.molad_summary;
					add(R.string.molad, summary, calMolad.getTime());
				}
			}
			// Last Kiddush Levana.
			else if ((jDayOfMonth > 10) && (jDayOfMonth < 20)) {
				opinion = mSettings.getKiddushLevana();
				if (OPINION_15.equals(opinion)) {
					date = cal.getSofZmanKidushLevana15Days();
					if (date == null) {
						date = jcal.getSofZmanKidushLevana15Days();
					}
					summary = R.string.levana_15;
				} else {
					date = cal.getSofZmanKidushLevanaBetweenMoldos();
					if (date == null) {
						date = jcal.getSofZmanKidushLevanaBetweenMoldos();
					}
					summary = R.string.levana_summary;
				}
				add(R.string.levana, summary, date);
			}
		}

		sort();
	}

	/**
	 * Get the number of candles to light.
	 * 
	 * @param cal
	 *            the Gregorian date.
	 * @param inIsrael
	 *            is in Israel?
	 * @return the number of candles to light, the holiday, and when to light.
	 */
	protected int getCandles(JewishCalendar jcal) {
		final int dayOfWeek = jcal.getDayOfWeek();

		// Check if the following day is special, because we can't check
		// EREV_CHANUKAH.
		int holidayToday = jcal.getYomTovIndex();
		jcal.forward();
		int holidayTomorrow = jcal.getYomTovIndex();
		int count = CANDLES_NONE;
		int flags = BEFORE_SUNSET;

		// Forbidden to light candles during Shabbath.
		if (dayOfWeek == Calendar.SATURDAY) {
			flags = MOTZE_SHABBATH;
		} else if (dayOfWeek == Calendar.FRIDAY) {
			// Probably never happens that Yom Kippurim falls on a Friday.
			// Prohibited to light candles on Yom Kippurim.
			if (holidayToday == JewishCalendar.YOM_KIPPUR) {
				return 0;
			}
		} else {
			// During a holiday, we can light for the next day from an existing
			// flame.
			switch (holidayToday) {
			case JewishCalendar.ROSH_HASHANA:
			case JewishCalendar.SUCCOS:
			case JewishCalendar.SHEMINI_ATZERES:
			case JewishCalendar.SIMCHAS_TORAH:
			case JewishCalendar.PESACH:
			case JewishCalendar.SHAVUOS:
				flags = AT_SUNSET;
				break;
			}
		}

		switch (holidayTomorrow) {
		case JewishCalendar.ROSH_HASHANA:
		case JewishCalendar.SUCCOS:
		case JewishCalendar.SHEMINI_ATZERES:
		case JewishCalendar.SIMCHAS_TORAH:
		case JewishCalendar.PESACH:
		case JewishCalendar.SHAVUOS:
			count = CANDLES_FESTIVAL;
			break;
		case JewishCalendar.YOM_KIPPUR:
			count = CANDLES_YOM_KIPPUR;
			break;
		case JewishCalendar.CHANUKAH:
			count = jcal.getDayOfChanukah();
			if ((dayOfWeek != Calendar.FRIDAY) && (dayOfWeek != Calendar.SATURDAY))
				flags = AT_SUNSET;
			break;
		default:
			if (dayOfWeek == Calendar.FRIDAY) {
				holidayTomorrow = SHABBATH;
				count = CANDLES_SHABBATH;
			} else if (dayOfWeek == Calendar.SUNDAY) {
				holidayToday = SHABBATH;
			}
			break;
		}

		return flags | ((holidayToday & HOLIDAY_MASK) << 12) | ((holidayTomorrow & HOLIDAY_MASK) << 4) | (count & CANDLES_MASK);
	}

	/**
	 * Sort.
	 */
	protected void sort() {
		if (mComparator == null) {
			mComparator = new ZmanimComparator();
		}
		sort(mComparator);
	}

	/**
	 * View holder for zman row item.
	 * 
	 * @author Moshe W
	 */
	private static class ViewHolder {

		public final TextView title;
		public final TextView summary;
		public final TextView time;

		public ViewHolder(TextView title, TextView summary, TextView time) {
			this.title = title;
			this.summary = summary;
			this.time = time;
		}
	}
}
