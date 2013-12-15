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

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sf.times.location.ZmanimLocations;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.util.GeoLocation;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Shows a list of all opinions for a halachic time (<em>zman</em>).
 * 
 * @author Moshe Waisberg
 */
public class ZmanimDetailsFragment extends ZmanimFragment {

	private static final int[] CHANNUKA_CANDLES = { R.id.candle_1, R.id.candle_2, R.id.candle_3, R.id.candle_4, R.id.candle_5, R.id.candle_6, R.id.candle_7, R.id.candle_8 };

	/** The master id. */
	private int mMasterId;
	/** The candles view for Shabbat. */
	private View mCandlesShabbat;
	/** The candles view for Channuka. */
	private View mCandlesChannuka;
	/** The candles view for Yom Kippurim. */
	private View mCandlesKippurim;

	/**
	 * Constructs a new details list.
	 * 
	 * @param context
	 *            the context.
	 * @param attrs
	 *            the XMl attributes.
	 * @param defStyle
	 *            the default style.
	 */
	public ZmanimDetailsFragment(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Constructs a new details list.
	 * 
	 * @param context
	 *            the context.
	 * @param attrs
	 *            the XML attributes.
	 */
	public ZmanimDetailsFragment(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Constructs a new details list.
	 * 
	 * @param context
	 *            the context.
	 */
	public ZmanimDetailsFragment(Context context) {
		super(context);
	}

	@Override
	protected ZmanimAdapter createAdapter(Calendar date, ZmanimLocations locations) {
		if (mMasterId == 0)
			return null;

		GeoLocation gloc = locations.getGeoLocation();
		// Have we been destroyed?
		if (gloc == null)
			return null;
		ComplexZmanimCalendar cal = new ComplexZmanimCalendar(gloc);
		cal.setCalendar(date);
		boolean inIsrael = locations.inIsrael();
		return new ZmanimDetailsAdapter(mContext, mSettings, cal, inIsrael, mMasterId);
	}

	@Override
	public ZmanimAdapter populateTimes(Calendar date) {
		return populateTimes(date, mMasterId);
	}

	/**
	 * Populate the list with detailed times.
	 * 
	 * @param date
	 *            the date.
	 * @param id
	 *            the time id.
	 */
	@SuppressWarnings("deprecation")
	public ZmanimAdapter populateTimes(Calendar date, int id) {
		mMasterId = id;
		this.setVisibility(View.VISIBLE);

		if (mSettings.isBackgroundGradient()) {
			Resources res = getResources();

			if (id == R.string.dawn) {
				setBackgroundColor(res.getColor(R.color.dawn));
			} else if (id == R.string.tallis) {
				setBackgroundColor(res.getColor(R.color.tallis));
			} else if (id == R.string.sunrise) {
				setBackgroundColor(res.getColor(R.color.sunrise));
			} else if (id == R.string.shema) {
				setBackgroundColor(res.getColor(R.color.shema));
			} else if (id == R.string.prayers) {
				setBackgroundColor(res.getColor(R.color.prayers));
			} else if (id == R.string.midday) {
				setBackgroundColor(res.getColor(R.color.midday));
			} else if (id == R.string.earliest_mincha) {
				setBackgroundColor(res.getColor(R.color.earliest_mincha));
			} else if (id == R.string.mincha) {
				setBackgroundColor(res.getColor(R.color.mincha));
			} else if (id == R.string.plug_hamincha) {
				setBackgroundColor(res.getColor(R.color.plug_hamincha));
			} else if (id == R.string.sunset) {
				setBackgroundColor(res.getColor(R.color.sunset));
			} else if (id == R.string.twilight) {
				setBackgroundColor(res.getColor(R.color.twilight));
			} else if (id == R.string.nightfall) {
				setBackgroundColor(res.getColor(R.color.nightfall));
			} else if (id == R.string.midnight) {
				setBackgroundColor(res.getColor(R.color.midnight));
			} else {
				setBackgroundDrawable(null);
			}
		} else {
			setBackgroundDrawable(null);
		}

		ZmanimAdapter adapter = super.populateTimes(date);

		if ((adapter != null) && (id == R.string.candles)) {
			int holiday = adapter.getCandlesHoliday();
			int candlesCount = adapter.getCandlesCount();

			switch (holiday) {
			case JewishCalendar.YOM_KIPPUR:
				if (mCandlesKippurim == null) {
					mCandlesKippurim = mInflater.inflate(R.layout.candles_kippurim, null);
					addView(mCandlesKippurim);
				}
				mView.setVisibility(View.GONE);
				if (mCandlesShabbat != null)
					mCandlesShabbat.setVisibility(View.GONE);
				if (mCandlesChannuka != null)
					mCandlesChannuka.setVisibility(View.GONE);
				mCandlesKippurim.setVisibility(View.VISIBLE);
				break;
			case JewishCalendar.CHANUKAH:
				if (mCandlesChannuka == null) {
					mCandlesChannuka = mInflater.inflate(R.layout.candles_channuka, null);
					addView(mCandlesChannuka);
				}
				// Only show relevant candles.
				for (int i = 0; i < candlesCount; i++) {
					mCandlesChannuka.findViewById(CHANNUKA_CANDLES[i]).setVisibility(View.VISIBLE);
				}
				for (int i = candlesCount; i < CHANNUKA_CANDLES.length; i++) {
					mCandlesChannuka.findViewById(CHANNUKA_CANDLES[i]).setVisibility(View.INVISIBLE);
				}
				mView.setVisibility(View.GONE);
				if (mCandlesShabbat != null)
					mCandlesShabbat.setVisibility(View.GONE);
				if (mCandlesKippurim != null)
					mCandlesKippurim.setVisibility(View.GONE);
				mCandlesChannuka.setVisibility(View.VISIBLE);
				break;
			default:
				if (candlesCount == 0) {
					// TODO ZmanimActivity.toggleDetail() - remove the detail
					// fragment, and show only the master fragment.
					if (mCandlesShabbat != null)
						mCandlesShabbat.setVisibility(View.GONE);
					if (mCandlesKippurim != null)
						mCandlesKippurim.setVisibility(View.GONE);
					if (mCandlesChannuka != null)
						mCandlesChannuka.setVisibility(View.GONE);
					this.setVisibility(View.GONE);
				} else {
					if (mCandlesShabbat == null) {
						mCandlesShabbat = mInflater.inflate(R.layout.candles_shabbat, null);
						addView(mCandlesShabbat);
					}
					mView.setVisibility(View.GONE);
					if (mCandlesKippurim != null)
						mCandlesKippurim.setVisibility(View.GONE);
					if (mCandlesChannuka != null)
						mCandlesChannuka.setVisibility(View.GONE);
					mCandlesShabbat.setVisibility(View.VISIBLE);
				}
				break;
			}
		} else {
			if (mCandlesShabbat != null)
				mCandlesShabbat.setVisibility(View.GONE);
			if (mCandlesKippurim != null)
				mCandlesKippurim.setVisibility(View.GONE);
			if (mCandlesChannuka != null)
				mCandlesChannuka.setVisibility(View.GONE);
			mView.setVisibility(View.VISIBLE);
		}

		return adapter;
	}

	@Override
	protected Drawable getListBackground() {
		return null;
	}

	@Override
	protected void setOnClickListener(View view, ZmanimItem item) {
		// No clicking allowed.
	}
}
