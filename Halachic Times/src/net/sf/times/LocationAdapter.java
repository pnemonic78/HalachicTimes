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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import net.sf.times.location.ZmanimAddress;
import net.sf.times.location.ZmanimLocations;
import android.content.Context;
import android.location.Address;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

/**
 * Location (city) adapter.
 * 
 * @author Moshe Waisberg
 */
public class LocationAdapter extends ArrayAdapter<ZmanimAddress> {

	private List<ZmanimAddress> mObjects;
	private List<ZmanimAddress> mOriginalValues;
	private Comparator<Address> mComparator;
	private LocationsFilter mFilter;
	/** Provider for locations. */
	private ZmanimLocations mLocations;

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
		mObjects = addresses;
		mLocations = ZmanimLocations.getInstance(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		ZmanimAddress addr = getItem(position);

		TextView cityName = (TextView) view.findViewById(android.R.id.title);
		cityName.setText(String.format("%s (%s)", addr.getLocality(), addr.getCountryName()));
		TextView coordinates = (TextView) view.findViewById(android.R.id.summary);
		coordinates.setText(mLocations.formatCoordinates(addr));

		return view;
	}

	@Override
	public void add(ZmanimAddress object) {
		if (mOriginalValues != null) {
			mOriginalValues.add(object);
		} else {
			mObjects.add(object);
		}
		super.add(object);
	}

	@Override
	public void insert(ZmanimAddress object, int index) {
		if (mOriginalValues != null) {
			mOriginalValues.add(index, object);
		} else {
			mObjects.add(index, object);
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
		sort(mComparator);
	}

	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new LocationsFilter();
		}
		return mFilter;
	}

	/**
	 * Compare two cities by their names, then their countries, but not by their
	 * locations.
	 * 
	 * @author Moshe Waisberg
	 */
	protected static class LocationComparator implements Comparator<Address> {
		@Override
		public int compare(Address lhs, Address rhs) {
			int c = lhs.getLocality().compareTo(rhs.getLocality());
			if (c != 0)
				return c;
			return lhs.getCountryName().compareTo(rhs.getCountryName());
		}
	}

	/**
	 * Filter the list of locations to match cities' names that contain the
	 * contraint.
	 * 
	 * @author Moshe Waisberg
	 */
	protected class LocationsFilter extends Filter {

		private Collator mCollator;
		private final Locale mLocale = Locale.getDefault();

		/**
		 * Constructs a new filter.
		 */
		public LocationsFilter() {
			super();
			mCollator = Collator.getInstance();
			mCollator.setStrength(Collator.PRIMARY);
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();

			if (mOriginalValues == null) {
				mOriginalValues = new ArrayList<ZmanimAddress>(mObjects);
			}

			final List<Address> values = new ArrayList<Address>(mOriginalValues);
			final int count = values.size();

			if (TextUtils.isEmpty(constraint)) {
				results.values = values;
				results.count = values.size();
			} else {
				final Locale locale = mLocale;
				final String constraintString = constraint.toString().toLowerCase(locale);

				final List<Address> newValues = new ArrayList<Address>();
				Address value;
				String valueText;

				for (int i = 0; i < count; i++) {
					value = values.get(i);
					valueText = value.getLocality().toLowerCase(locale);

					if (contains(valueText, constraintString)) {
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
			List<ZmanimAddress> list = (List<ZmanimAddress>) results.values;
			LocationAdapter.super.clear();
			if (results.count > 0) {
				for (ZmanimAddress address : list) {
					LocationAdapter.super.add(address);
				}
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
}
