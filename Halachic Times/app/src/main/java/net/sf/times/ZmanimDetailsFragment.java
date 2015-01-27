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
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Shows a list of all opinions for a halachic time (<em>zman</em>).
 * 
 * @author Moshe Waisberg
 */
public class ZmanimDetailsFragment<A extends ZmanimDetailsAdapter> extends ZmanimFragment<A> {

	/** The master id. */
	private int mMasterId;

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

	/**
	 * Get the master id for populating the details.
	 * 
	 * @return the master id.
	 */
	public int getMasterId() {
		return mMasterId;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected A createAdapter() {
		if (mMasterId == 0)
			return null;

		return (A) new ZmanimDetailsAdapter(mContext, mSettings, mMasterId);
	}

	@Override
	public A populateTimes(Calendar date) {
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
	public A populateTimes(Calendar date, int id) {
		mMasterId = id;

		A adapter = getAdapter();
		if (adapter != null) {
			adapter.setItemId(id);
			super.populateTimes(date);
		}

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

	@Override
	protected void bindViewGrouping(ViewGroup list, int position, CharSequence label) {
		if (position >= 0)
			return;
		super.bindViewGrouping(list, position, getResources().getText(mMasterId));
	}

}
