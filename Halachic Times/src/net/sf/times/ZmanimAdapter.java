/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/MPL-1.1.html
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

import java.util.Date;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Adapter for halachic times list.
 * 
 * @author Moshe
 */
public class ZmanimAdapter extends ArrayAdapter<ZmanimItem> {

	private final Context mContext;
	private final LayoutInflater mInflater;
	/** The settings and preferences. */
	private ZmanimSettings mSettings;

	/**
	 * Time row item.
	 */
	public static class ZmanimItem {

		/** The title. */
		public CharSequence title;
		/** The summary. */
		public CharSequence summary;
		/** The time. */
		public CharSequence time;
		/** Is the time past? */
		public boolean past;

		public ZmanimItem() {
			super();
		}
	}

	/**
	 * Creates a new adapter.
	 */
	public ZmanimAdapter(Context context) {
		super(context, R.layout.times_item, 0);
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mSettings = new ZmanimSettings(context);
	}

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
		title.setText(item.title);
		TextView summary = (TextView) view.findViewById(R.id.summary);
		summary.setText(item.summary);
		if (item.summary == null)
			summary.setVisibility(View.GONE);
		TextView time = (TextView) view.findViewById(R.id.time);
		time.setText(item.time);
		boolean enabled = !item.past;
		title.setEnabled(enabled);
		summary.setEnabled(enabled);
		time.setEnabled(enabled);

		return view;
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
		if (time == 0)
			return;

		final boolean summaryHidden = mSettings.isSummariesHidden();
		final boolean pastEnabled = mSettings.isPast();

		ZmanimItem item = new ZmanimItem();
		item.title = mContext.getText(labelId);
		item.summary = summaryHidden ? null : mContext.getText(summaryId);
		item.time = DateUtils.formatDateTime(getContext(), time, DateUtils.FORMAT_SHOW_TIME);
		item.past = pastEnabled ? false : (time < System.currentTimeMillis());

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
		add(labelId, summaryId, date.getTime());
	}
}
