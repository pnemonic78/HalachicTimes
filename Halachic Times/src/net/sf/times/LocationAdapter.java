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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
public class LocationAdapter extends ArrayAdapter<Address> {

	private List<Address> mObjects;
	private List<Address> mOriginalValues;
	private Comparator<Address> mComparator;
	private LocationsFilter mFilter;

	/**
	 * Constructs a new adapter.
	 * 
	 * @param context
	 *            the context.
	 * @param addresses
	 *            the list of cities.
	 */
	public LocationAdapter(Context context, List<Address> addresses) {
		super(context, R.layout.times_item, android.R.id.title, addresses);
		mObjects = addresses;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);

		Address addr = getItem(position);
		TextView text1 = (TextView) view.findViewById(android.R.id.title);
		text1.setText(addr.getLocality());
		TextView text2 = (TextView) view.findViewById(android.R.id.summary);
		text2.setText(addr.getCountryName());

		return view;
	}

	@Override
	public void add(Address object) {
		if (mOriginalValues != null) {
			mOriginalValues.add(object);
		} else {
			mObjects.add(object);
		}
		super.add(object);
	}

	@Override
	public void insert(Address object, int index) {
		if (mOriginalValues != null) {
			mOriginalValues.add(index, object);
		} else {
			mObjects.add(index, object);
		}
		super.insert(object, index);
	}

	@Override
	public void remove(Address object) {
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
				mOriginalValues = new ArrayList<Address>(mObjects);
			}

			final List<Address> values = new ArrayList<Address>(mOriginalValues);
			final int count = values.size();

			if (TextUtils.isEmpty(constraint)) {
				results.values = values;
				results.count = values.size();
			} else {
				final Locale locale = Locale.getDefault();
				final String constraintString = constraint.toString().toLowerCase(locale);

				final List<Address> newValues = new ArrayList<Address>();
				Address value;
				String valueText;

				for (int i = 0; i < count; i++) {
					value = values.get(i);
					valueText = value.getLocality().toLowerCase(locale);

					if (valueText.contains(constraintString)) {
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
			List<Address> list = (List<Address>) results.values;
			LocationAdapter.super.clear();
			if (results.count > 0) {
				for (Address address : list) {
					LocationAdapter.super.add(address);
				}
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}
