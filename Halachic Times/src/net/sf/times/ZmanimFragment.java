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
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.util.GeoLocation;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Shows a list of halachic times (<em>zmanim</em>) for prayers.
 * 
 * @author Moshe Waisberg
 */
public class ZmanimFragment extends FrameLayout {

	protected final Context mContext;
	protected LayoutInflater mInflater;
	private OnClickListener mOnClickListener;
	/** The list. */
	protected ViewGroup mList;
	/** Provider for locations. */
	protected ZmanimLocations mLocations;
	/** The settings and preferences. */
	protected ZmanimSettings mSettings;
	/** The gradient background. */
	private Drawable mBackground;
	/** The master item selected row. */
	private View mHighlightRow;
	/** The master item background that is not selected. */
	private Drawable mUnhighlightBackground;
	private int mUnhighlightPaddingLeft;
	private int mUnhighlightPaddingTop;
	private int mUnhighlightPaddingRight;
	private int mUnhighlightPaddingBottom;
	/** The master item background that is selected. */
	private Drawable mHighlightBackground;
	private ZmanimAdapter mAdapter;

	/**
	 * Constructs a new list.
	 * 
	 * @param context
	 *            the context.
	 * @param attrs
	 *            the XMl attributes.
	 * @param defStyle
	 *            the default style.
	 */
	public ZmanimFragment(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}

	/**
	 * Constructs a new list.
	 * 
	 * @param context
	 *            the context.
	 * @param attrs
	 *            the XML attributes.
	 */
	public ZmanimFragment(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	/**
	 * Constructs a new list.
	 * 
	 * @param context
	 *            the context.
	 */
	public ZmanimFragment(Context context) {
		super(context);
		mContext = context;
		init();
	}

	/** Initialise. */
	private void init() {
		mSettings = new ZmanimSettings(mContext);
		mLocations = ZmanimLocations.getInstance(mContext);

		mInflater = LayoutInflater.from(mContext);
		ViewGroup view = (ViewGroup) mInflater.inflate(R.layout.times_list, this);
		mList = (ViewGroup) view.findViewById(android.R.id.list);
	}

	protected ZmanimAdapter createAdapter(Calendar date, ZmanimLocations locations) {
		GeoLocation gloc = locations.getGeoLocation();
		// Have we been destroyed?
		if (gloc == null)
			return null;
		ComplexZmanimCalendar cal = new ComplexZmanimCalendar(gloc);
		cal.setCalendar(date);
		boolean inIsrael = locations.inIsrael();
		return new ZmanimAdapter(mContext, mSettings, cal, inIsrael);
	}

	/**
	 * Populate the list with times.
	 * 
	 * @param date
	 *            the date.
	 */
	public void populateTimes(Calendar date) {
		// Called before attached to activity?
		if (mLocations == null)
			return;
		ZmanimAdapter adapter = createAdapter(date, mLocations);
		// Have we been destroyed?
		if (adapter == null)
			return;
		adapter.populate(false);
		mAdapter = adapter;

		ViewGroup list = mList;
		if (list == null)
			return;
		if (isBackgroundDrawable()) {
			if (mSettings.isBackgroundGradient()) {
				if (mBackground == null)
					mBackground = getResources().getDrawable(R.drawable.list_gradient);
				list.setBackgroundDrawable(mBackground);
			} else
				list.setBackgroundDrawable(null);
		}
		bindViews(list, adapter);
	}

	/**
	 * Is the list background painted?
	 * 
	 * @return {@code true} for non-transparent background.
	 */
	protected boolean isBackgroundDrawable() {
		return true;
	}

	/**
	 * Bind the times to a list.
	 * 
	 * @param list
	 *            the list.
	 * @param adapter
	 *            the list adapter.
	 */
	protected void bindViews(ViewGroup list, ZmanimAdapter adapter) {
		if (list == null)
			return;
		final int count = adapter.getCount();
		list.removeAllViews();

		ZmanimItem item;
		View row;

		for (int position = 0; position < count; position++) {
			item = adapter.getItem(position);
			row = adapter.getView(position, null, list);
			bindView(list, position, row, item);
		}
	}

	/**
	 * Bind the time to a list.
	 * 
	 * @param list
	 *            the list.
	 * @param position
	 *            the position index.
	 * @param view
	 *            the row view.
	 * @param item
	 *            the item.
	 */
	protected void bindView(ViewGroup list, int position, View row, ZmanimItem item) {
		setOnClickListener(row, item);
		if (position > 0)
			mInflater.inflate(R.layout.divider, list);
		list.addView(row);
	}

	protected void setOnClickListener(View view, ZmanimItem item) {
		final int id = item.titleId;
		boolean clickable = view.isEnabled() && (id != R.string.candles) && (id != R.string.molad);
		view.setOnClickListener(clickable ? mOnClickListener : null);
		view.setClickable(clickable);
	}

	@Override
	public void setOnClickListener(OnClickListener listener) {
		mOnClickListener = listener;
	}

	/**
	 * Get the background for the selected item.
	 * 
	 * @return the background.
	 */
	private Drawable getSelectedBackground() {
		if (mHighlightBackground == null) {
			ColorDrawable drawable = new ColorDrawable(0x40ffffff);
			mHighlightBackground = drawable;
		}
		return mHighlightBackground;
	}

	/**
	 * Mark the selected row as unselected.
	 */
	public void unhighlight() {
		unhighlight(mHighlightRow);
	}

	/**
	 * Mark the row as unselected.
	 * 
	 * @param view
	 *            the row view.
	 */
	private void unhighlight(View view) {
		Drawable bg = mUnhighlightBackground;
		if ((view == null) || (bg == null))
			return;

		// Workaround for Samsung ICS bug where the highlight lingers.
		if (bg instanceof StateListDrawable)
			bg = bg.getConstantState().newDrawable();
		view.setBackgroundDrawable(bg);
		view.setPadding(mUnhighlightPaddingLeft, mUnhighlightPaddingTop, mUnhighlightPaddingRight, mUnhighlightPaddingBottom);
		mUnhighlightBackground = null;
	}

	/**
	 * Mark the row as selected.
	 * 
	 * @param itemId
	 *            the row id.
	 */
	public void highlight(int itemId) {
		// Find the view that matches the item id (the view that was clicked).
		final ZmanimAdapter adapter = mAdapter;
		if (adapter == null)
			return;
		final ViewGroup list = mList;
		if (list == null)
			return;
		View view = null;
		View child;
		ZmanimItem item;
		final int count = list.getChildCount();
		for (int i = 0; i < count; i++) {
			child = list.getChildAt(i);
			item = (ZmanimItem) child.getTag();
			// Maybe row divider?
			if (item == null)
				continue;
			if (item.titleId == itemId) {
				view = child;
				break;
			}
		}
		if (view == null)
			return;

		mUnhighlightBackground = view.getBackground();
		mUnhighlightPaddingLeft = view.getPaddingLeft();
		mUnhighlightPaddingTop = view.getPaddingTop();
		mUnhighlightPaddingRight = view.getPaddingRight();
		mUnhighlightPaddingBottom = view.getPaddingBottom();
		view.setBackgroundDrawable(getSelectedBackground());
		mHighlightRow = view;
	}

	public boolean isVisible() {
		return getVisibility() == VISIBLE;
	}
}