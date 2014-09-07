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
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;
import net.sourceforge.zmanim.util.GeoLocation;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Shows a list of halachic times (<em>zmanim</em>) for prayers.
 * 
 * @author Moshe Waisberg
 */
public class ZmanimFragment extends FrameLayout {

	protected final Context mContext;
	protected LayoutInflater mInflater;
	private OnClickListener mOnClickListener;
	/** The main list view. */
	protected ViewGroup mView;
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
	/** The master item background that is selected. */
	private Drawable mHighlightBackground;
	/** The master item background that is not selected. */
	private Drawable mUnhighlightBackground;
	private int mPaddingLeft;
	private int mPaddingTop;
	private int mPaddingRight;
	private int mPaddingBottom;
	private ZmanimAdapter mAdapter;
	/** The gesture detector. */
	private GestureDetector mGestureDetector;

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
		init(context);
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
		init(context);
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
		init(context);
	}

	/** Initialise. */
	private void init(Context context) {
		if (!isInEditMode()) {
			mSettings = new ZmanimSettings(context);
			ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
			mLocations = app.getLocations();
		}

		mInflater = LayoutInflater.from(context);
		mView = (ViewGroup) mInflater.inflate(R.layout.times_list, null);
		addView(mView);
		mList = (ViewGroup) mView.findViewById(android.R.id.list);
	}

	/**
	 * Create a new times adapter.
	 * 
	 * @param date
	 *            the date.
	 * @param locations
	 *            the locations provider.
	 * @return the adapter.
	 */
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
	@SuppressWarnings("deprecation")
	public ZmanimAdapter populateTimes(Calendar date) {
		// Called before attached to activity?
		if (mLocations == null)
			return null;
		ZmanimAdapter adapter = createAdapter(date, mLocations);
		// Have we been destroyed?
		if (adapter == null)
			return null;
		adapter.populate(false);
		mAdapter = adapter;

		ViewGroup list = mList;
		if (list == null)
			return adapter;
		list.setBackgroundDrawable(getListBackground());
		bindViews(list, adapter, date);
		return adapter;
	}

	/**
	 * Get the list background.
	 * 
	 * @return the background - {@code null} otherwise.
	 */
	protected Drawable getListBackground() {
		if (mSettings.isBackgroundGradient()) {
			if (mBackground == null)
				mBackground = getResources().getDrawable(R.drawable.list_gradient);
			return mBackground;
		}
		return null;
	}

	/**
	 * Bind the times to a list.
	 * 
	 * @param list
	 *            the list.
	 * @param adapter
	 *            the list adapter.
	 * @param date
	 *            the date.
	 */
	protected void bindViews(ViewGroup list, ZmanimAdapter adapter, Calendar date) {
		if (list == null)
			return;
		final int count = adapter.getCount();
		list.removeAllViews();

		ZmanimItem item;
		View row;

		JewishDate jewishDate = new JewishDate(date);
		CharSequence dateHebrew = adapter.formatDate(jewishDate);

		bindViewGrouping(list, -1, dateHebrew);

		for (int position = 0; position < count; position++) {
			item = adapter.getItem(position);
			row = adapter.getView(position, null, list);
			bindView(list, position, row, item);

			// New Hebrew day.
			if (item.titleId == R.string.sunset) {
				jewishDate.forward();
				dateHebrew = adapter.formatDate(jewishDate);
				bindViewGrouping(list, position, dateHebrew);
			}
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
		mInflater.inflate(R.layout.divider, list);
		list.addView(row);
	}

	/**
	 * Bind the date group header to a list.
	 * 
	 * @param list
	 *            the list.
	 * @param position
	 *            the position index.
	 * @param label
	 *            the formatted Hebrew date label.
	 */
	protected void bindViewGrouping(ViewGroup list, int position, CharSequence label) {
		if (position > 0)
			mInflater.inflate(R.layout.divider, list);
		ViewGroup row = (ViewGroup) mInflater.inflate(R.layout.date_group, null);
		TextView text = (TextView) row.findViewById(R.id.date_hebrew);
		text.setText(label);
		list.addView(row);
	}

	protected void setOnClickListener(View view, ZmanimItem item) {
		final int id = item.titleId;
		boolean clickable = view.isEnabled() && (id != R.string.molad);
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
			mHighlightBackground = getResources().getDrawable(R.drawable.list_selected);
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
	@SuppressWarnings("deprecation")
	private void unhighlight(View view) {
		Drawable bg = mUnhighlightBackground;
		if ((view == null) || (bg == null))
			return;

		// Workaround for Samsung ICS bug where the highlight lingers.
		if (bg instanceof StateListDrawable)
			bg = bg.getConstantState().newDrawable();
		view.setBackgroundDrawable(bg);
		view.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom);
		mUnhighlightBackground = null;
	}

	/**
	 * Mark the row as selected.
	 * 
	 * @param itemId
	 *            the row id.
	 */
	@SuppressWarnings("deprecation")
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
			item = (ZmanimItem) child.getTag(R.id.time);
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
		mPaddingLeft = view.getPaddingLeft();
		mPaddingTop = view.getPaddingTop();
		mPaddingRight = view.getPaddingRight();
		mPaddingBottom = view.getPaddingBottom();
		view.setBackgroundDrawable(getSelectedBackground());
		view.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom);
		mHighlightRow = view;
	}

	public boolean isVisible() {
		return getVisibility() == VISIBLE;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if ((mGestureDetector != null) && mGestureDetector.onTouchEvent(event))
			return true;
		return super.onInterceptTouchEvent(event);
	}

	/**
	 * Set the gesture detector.
	 * 
	 * @param gestureDetector
	 *            the gesture detector.
	 */
	public void setGestureDetector(GestureDetector gestureDetector) {
		mGestureDetector = gestureDetector;
	}

}
