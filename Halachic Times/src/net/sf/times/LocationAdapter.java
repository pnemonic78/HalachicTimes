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
 *   Moshe Waisberggdcfvrfdfg v 
 * 
 */
package net.sf.times;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import net.sf.times.location.ZmanimAddress;
import net.sf.times.location.ZmanimLocations;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

/**
 * Location adapter.
 * 
 * @author Moshe Waisberg
 */
public class LocationAdapter extends ArrayAdapter<ZmanimAddress> {

	private List<LocationItem> mObjects = new ArrayList<LocationItem>();
	private List<LocationItem> mOriginalValues;
	private Comparator<LocationItem> mComparator;
	private LocationsFilter mFilter;
	/** Provider for locations. */
	private ZmanimLocations mLocations;
	private Collator mCollator;
	private final Locale mLocale = Locale.getDefault();

	/**
	 * Constructs a new adapter.
	 * 
	 * @param context
	 *            the context.
	 * @param addresses
	 *            the list of cities.
	 */
	public LocationAdapter(Context context, List<ZmanimAddress> addresses) {
		super(context, R.layout.times_item, android.R.id.title, addresses);
		for (ZmanimAddress addr : addresses) {
			mObjects.add(new LocationItem(addr));
		}
		ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
		mLocations = app.getLocations();
		mCollator = Collator.getInstance();
		mCollator.setStrength(Collator.PRIMARY);
	}

	@Override
	public int getCount() {
		return mObjects.size();
	}

	@Override
	public ZmanimAddress getItem(int position) {
		return mObjects.get(position).getAddress();
	}

	@Override
	public int getPosition(ZmanimAddress item) {
		return mObjects.indexOf(item);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		LocationItem item = mObjects.get(position);

		TextView cityName = (TextView) view.findViewById(android.R.id.title);
		cityName.setText(item.getLabel());
		TextView coordinates = (TextView) view.findViewById(android.R.id.summary);
		coordinates.setText(item.getCoordinates());

		return view;
	}

	@Override
	public void add(ZmanimAddress object) {
		add(new LocationItem(object));
	}

	public void add(LocationItem object) {
		if (mOriginalValues != null) {
			mOriginalValues.add(object);
		} else {
			mObjects.add(object);
		}
		super.add(object.getAddress());
	}

	@Override
	public void insert(ZmanimAddress object, int index) {
		if (mOriginalValues != null) {
			mOriginalValues.add(index, new LocationItem(object));
		} else {
			mObjects.add(index, new LocationItem(object));
		}
		super.insert(object, index);
	}

	@Override
	public void remove(ZmanimAddress object) {
		if (mOriginalValues != null) {
			mOriginalValues.remove(object);
		} else {
			mObjects.remove(object);
		}
		super.remove(object);
	}

	@Override
	public void clear() {
		if (mOriginalValues != null) {
			mOriginalValues.clear();
		} else {
			mObjects.clear();
		}
		super.clear();
	}

	/**
	 * Sort.
	 */
	public void sort() {
		if (mComparator == null) {
			mComparator = new LocationComparator();
		}
		if (mOriginalValues != null) {
			Collections.sort(mOriginalValues, mComparator);
		} else {
			Collections.sort(mObjects, mComparator);
		}
		notifyDataSetChanged();
	}

	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new LocationsFilter();
		}
		return mFilter;
	}

	/**
	 * Filter the list of locations to match cities' names that contain the
	 * contraint.
	 * 
	 * @author Moshe Waisberg
	 */
	protected class LocationsFilter extends Filter {

		/**
		 * Constructs a new filter.
		 */
		public LocationsFilter() {
			super();
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();

			if (mOriginalValues == null) {
				mOriginalValues = new ArrayList<LocationItem>(mObjects);
			}

			final List<LocationItem> values = new ArrayList<LocationItem>(mOriginalValues);
			final int count = values.size();

			if (TextUtils.isEmpty(constraint)) {
				results.values = values;
				results.count = values.size();
			} else {
				final Locale locale = mLocale;
				final String constraintString = constraint.toString().toLowerCase(locale);
				String latitude;
				String longitude;

				final List<LocationItem> newValues = new ArrayList<LocationItem>();
				LocationItem value;
				String valueText;

				for (int i = 0; i < count; i++) {
					value = values.get(i);
					valueText = value.getLabelLower();
					latitude = value.getFormatLatitude();
					longitude = value.getFormatLongitude();

					if (contains(valueText, constraintString) || latitude.contains(constraintString) || longitude.contains(constraintString)) {
						newValues.add(value);
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			mObjects = (List<LocationItem>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}

		/**
		 * Does the first string contain the other string?
		 * 
		 * @param s
		 *            the source string.
		 * @param search
		 *            the character sequence to search for.
		 * @return {@code true} if {@code s} contains {@code search}.
		 */
		private boolean contains(String s, String search) {
			final int len1 = s.length();
			final int len2 = search.length();

			if (len1 < len2)
				return false;

			final Collator collator = mCollator;

			if (len1 == len2) {
				if (s.equals(search) || collator.equals(s, search))
					return true;
				return false;
			}

			if (s.contains(search))
				return true;

			// Let's do a "Collator.contains"
			String lhs;
			String rhs;
			int dLen = len1 - len2;
			String concat;
			for (int i = 0; i < dLen; i++) {
				lhs = s.substring(0, i);
				rhs = s.substring(len2 + i);
				concat = lhs + search + rhs;
				if (collator.equals(s, concat))
					return true;
			}

			return false;
		}
	}

	/**
	 * Location item.
	 * 
	 * @author Moshe Waisberg
	 */
	protected class LocationItem {

		private final ZmanimAddress address;
		private final String label;
		private final String labelLower;
		private String latitude;
		private String longitude;
		private String coordinates;

		/**
		 * Constructs a new item.
		 * 
		 * @param address
		 *            the address.
		 */
		public LocationItem(ZmanimAddress address) {
			super();
			this.address = address;
			this.label = address.getFormatted();
			this.labelLower = label.toLowerCase(mLocale);
		}

		/**
		 * Get the source address.
		 * 
		 * @return the address.
		 */
		public ZmanimAddress getAddress() {
			return address;
		}

		/**
		 * Get the label.
		 * 
		 * @return the label.
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * Get the label in lower casing.
		 * 
		 * @return the label.
		 */
		public String getLabelLower() {
			return labelLower;
		}

		/**
		 * Get the formatted latitude.
		 * 
		 * @return the latitude.
		 */
		public String getFormatLatitude() {
			if (mLocations == null)
				return null;
			if (latitude == null)
				this.latitude = mLocations.formatCoordinate(address.getLatitude());
			return latitude;
		}

		/**
		 * Get the formatted longitude.
		 * 
		 * @return the longitude.
		 */
		public String getFormatLongitude() {
			if (mLocations == null)
				return null;
			if (longitude == null)
				this.longitude = mLocations.formatCoordinate(address.getLongitude());
			return longitude;
		}

		/**
		 * Get the formatted coordinates.
		 * 
		 * @return the coordinates.
		 */
		public String getCoordinates() {
			if (mLocations == null)
				return null;
			if (coordinates == null)
				this.coordinates = mLocations.formatCoordinates(getAddress());
			return coordinates;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null)
				return false;
			if (o instanceof LocationItem)
				return getAddress().equals(((LocationItem) o).getAddress());
			if (o instanceof ZmanimAddress)
				return getAddress().equals(o);
			return super.equals(o);
		}
	}

	/**
	 * Compare two cities by their names, then their countries, but not by their
	 * locations.
	 * 
	 * @author Moshe Waisberg
	 */
	protected class LocationComparator implements Comparator<LocationItem> {

		/** Double subtraction error. */
		private static final double EPSILON = 1e-6;

		private Collator mCollator;

		/**
		 * Constructs a new comparator.
		 */
		public LocationComparator() {
			super();
			mCollator = Collator.getInstance();
			mCollator.setStrength(Collator.PRIMARY);
		}

		@SuppressLint("DefaultLocale")
		@Override
		public int compare(LocationItem item1, LocationItem item2) {
			String format1 = item1.getLabelLower();
			String format2 = item2.getLabelLower();
			int c = mCollator.compare(format1, format2);
			if (c != 0)
				return c;
			ZmanimAddress addr1 = item1.getAddress();
			ZmanimAddress addr2 = item2.getAddress();
			double lat1 = addr1.getLatitude();
			double lat2 = addr2.getLatitude();
			double latD = lat1 - lat2;
			if (latD >= EPSILON)
				return 1;
			if (latD <= -EPSILON)
				return -1;
			double lng1 = addr1.getLongitude();
			double lng2 = addr2.getLongitude();
			double lngD = lng1 - lng2;
			if (lngD >= EPSILON)
				return 1;
			if (lngD < -EPSILON)
				return -1;
			return 0;
		}
	}
}
