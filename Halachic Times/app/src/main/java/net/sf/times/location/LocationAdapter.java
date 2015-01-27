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
package net.sf.times.location;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import net.sf.times.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.TextView;

/**
 * Location adapter.
 * 
 * @author Moshe Waisberg
 */
public class LocationAdapter extends ArrayAdapter<LocationAdapter.LocationItem> implements OnClickListener {

	protected List<LocationItem> mObjects;
	private List<LocationItem> mOriginalValues;
	private LocationComparator mComparator;
	private LocationsFilter mFilter;
	private Collator mCollator;
	private final Locale mLocale = Locale.getDefault();
	private OnFavoriteClickListener mOnFavoriteClickListener;

	/**
	 * Constructs a new adapter.
	 * 
	 * @param context
	 *            the context.
	 * @param items
	 *            the list of addresses' items.
	 */
	public LocationAdapter(Context context, List<LocationItem> items) {
		super(context, R.layout.location, android.R.id.title, items);
		mObjects = new ArrayList<LocationItem>(items);
		mCollator = Collator.getInstance();
		mCollator.setStrength(Collator.PRIMARY);
		sortNoNotify();
	}

	@Override
	public int getCount() {
		return mObjects.size();
	}

	@Override
	public LocationItem getItem(int position) {
		return getLocationItem(position);
	}

	/**
	 * Get the location item.
	 * 
	 * @param position
	 *            the position index.
	 * @return the item.
	 */
	protected LocationItem getLocationItem(int position) {
		return mObjects.get(position);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getAddress().getId();
	}

	@Override
	public int getPosition(LocationItem object) {
		final int size = mObjects.size();
		LocationItem item;
		for (int i = 0; i < size; i++) {
			item = mObjects.get(i);
			if (item.equals(object))
				return i;
		}
		return super.getPosition(object);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		LocationItem item = getLocationItem(position);
		TextView cityName;
		TextView coordinates;
		CheckBox checkbox;

		ViewHolder holder = (ViewHolder) view.getTag();
		if (holder == null) {
			cityName = (TextView) view.findViewById(android.R.id.title);
			coordinates = (TextView) view.findViewById(R.id.coordinates);
			checkbox = (CheckBox) view.findViewById(android.R.id.checkbox);
			checkbox.setOnClickListener(this);

			holder = new ViewHolder(cityName, coordinates, checkbox);
			view.setTag(holder);
		} else {
			cityName = holder.cityName;
			coordinates = holder.coordinates;
			checkbox = holder.checkbox;
		}

		cityName.setText(item.getLabel());
		coordinates.setText(item.getCoordinates());
		checkbox.setChecked(item.isFavorite());
		checkbox.setTag(item.getAddress());

		return view;
	}

	@Override
	public void add(LocationItem object) {
		if (mOriginalValues != null) {
			mOriginalValues.add(object);
		} else {
			mObjects.add(object);
		}
		super.add(object);
	}

	@Override
	public void insert(LocationItem object, int index) {
		if (mOriginalValues != null) {
			mOriginalValues.add(index, object);
		} else {
			mObjects.add(index, object);
		}
		super.insert(object, index);
	}

	@Override
	public void remove(LocationItem object) {
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
	 * Sort without notification.
	 */
	protected void sortNoNotify() {
		if (mComparator == null) {
			mComparator = new LocationComparator();
		}
		sortNoNotify(mComparator);
	}

	/**
	 * Sort without notification.
	 * 
	 * @param the
	 *            comparator used to sort the objects contained in this adapter.
	 */
	protected void sortNoNotify(Comparator<? super LocationItem> comparator) {
		// Remove duplicate locations.
		Set<LocationItem> items = new TreeSet<LocationItem>(comparator);
		if (mOriginalValues != null) {
			items.addAll(mOriginalValues);
			mOriginalValues.clear();
			mOriginalValues.addAll(items);
		} else {
			items.addAll(mObjects);
			mObjects.clear();
			mObjects.addAll(items);
		}
	}

	/**
	 * Sort.
	 */
	public void sort() {
		sortNoNotify();
		notifyDataSetChanged();
	}

	@Override
	public void sort(Comparator<? super LocationItem> comparator) {
		sortNoNotify(comparator);
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
	 * View holder for location row item.
	 * 
	 * @author Moshe W
	 */
	private static class ViewHolder {

		public final TextView cityName;
		public final TextView coordinates;
		public final CheckBox checkbox;

		public ViewHolder(TextView cityName, TextView coordinates, CheckBox checkbox) {
			this.cityName = cityName;
			this.coordinates = coordinates;
			this.checkbox = checkbox;
		}
	}

	/**
	 * Filter the list of locations to match cities' names that contain the
	 * constraint.
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
	protected static class LocationItem {

		private final ZmanimAddress mAddress;
		private final String mLabel;
		private final String mLabelLower;
		private final String mLatitude;
		private final String mLongitude;
		private final String mCoordinates;

		/**
		 * Constructs a new item.
		 * 
		 * @param address
		 *            the address.
		 */
		public LocationItem(ZmanimAddress address, ZmanimLocations locations) {
			super();
			this.mAddress = address;
			this.mLabel = address.getFormatted();
			this.mLabelLower = mLabel.toLowerCase(address.getLocale());
			this.mLatitude = locations.formatCoordinate(address.getLatitude());
			this.mLongitude = locations.formatCoordinate(address.getLongitude());
			this.mCoordinates = locations.formatCoordinates(getAddress());
		}

		/**
		 * Get the source address.
		 * 
		 * @return the address.
		 */
		public ZmanimAddress getAddress() {
			return mAddress;
		}

		/**
		 * Get the label.
		 * 
		 * @return the label.
		 */
		public String getLabel() {
			return mLabel;
		}

		/**
		 * Get the label in lower casing.
		 * 
		 * @return the label.
		 */
		public String getLabelLower() {
			return mLabelLower;
		}

		/**
		 * Get the formatted latitude.
		 * 
		 * @return the latitude.
		 */
		public String getFormatLatitude() {
			return mLatitude;
		}

		/**
		 * Get the formatted longitude.
		 * 
		 * @return the longitude.
		 */
		public String getFormatLongitude() {
			return mLongitude;
		}

		/**
		 * Get the formatted coordinates.
		 * 
		 * @return the coordinates.
		 */
		public String getCoordinates() {
			return mCoordinates;
		}

		/**
		 * Is location a favourite?
		 * 
		 * @return {@code true} if a favourite.
		 */
		public boolean isFavorite() {
			return getAddress().isFavorite();
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
	 * Compare two locations by their locations, then by their names, then their
	 * ids.
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
			ZmanimAddress addr1 = item1.getAddress();
			ZmanimAddress addr2 = item2.getAddress();

			// Sort first by name.
			String format1 = item1.getLabelLower();
			String format2 = item2.getLabelLower();
			int c = mCollator.compare(format1, format2);
			if (c != 0)
				return c;

			// Is same location?
			double lat1 = addr1.getLatitude();
			double lat2 = addr2.getLatitude();
			double latD = lat1 - lat2;
			double lng1 = addr1.getLongitude();
			double lng2 = addr2.getLongitude();
			double lngD = lng1 - lng2;
			if (latD >= EPSILON)
				return 1;
			if (latD <= -EPSILON)
				return -1;
			if (lngD >= EPSILON)
				return 1;
			if (lngD < -EPSILON)
				return -1;

			// Then sort by id. Positive id is more important.
			long id1 = addr1.getId();
			long id2 = addr2.getId();
			return (id1 == id2 ? 0 : (id1 < id2 ? -1 : 1));
		}
	}

	/**
	 * Interface definition for a callback to be invoked when a "favorite"
	 * checkbox in this list has been clicked.
	 * 
	 * @author Moshe W
	 */
	public interface OnFavoriteClickListener {

		void onFavoriteClick(LocationAdapter adapter, CompoundButton button, ZmanimAddress address);

	}

	/**
	 * Get the listener for "favorite" clicked callbacks.
	 * 
	 * @return the listener.
	 */
	public OnFavoriteClickListener getOnFavoriteClickListener() {
		return mOnFavoriteClickListener;
	}

	/**
	 * Set the listener for "favorite" clicked callbacks.
	 * 
	 * @param listener
	 *            the listener.
	 */
	public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
		this.mOnFavoriteClickListener = listener;
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();

		if (id == android.R.id.checkbox) {
			CompoundButton buttonView = (CompoundButton) v;
			ZmanimAddress address = (ZmanimAddress) buttonView.getTag();

			if ((address != null) && (mOnFavoriteClickListener != null)) {
				mOnFavoriteClickListener.onFavoriteClick(LocationAdapter.this, buttonView, address);
			}
		}
	}

}
