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
package net.sf.times.location;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

/**
 * Location adapter for specific type of locations.
 * 
 * @author Moshe Waisberg
 */
public abstract class SpecificLocationAdapter extends LocationAdapter {

	private final List<LocationItem> mSpecific = new ArrayList<LocationItem>();

	public SpecificLocationAdapter(Context context, List<ZmanimAddress> addresses) {
		super(context, addresses);
		populateSpecific();
	}

	private void populateSpecific() {
		mSpecific.clear();

		ZmanimAddress address;
		for (LocationItem item : mObjects) {
			address = item.getAddress();
			if (isSpecific(address))
				mSpecific.add(item);
		}
	}

	/**
	 * Is the address specific to this adapter?
	 * 
	 * @param address
	 *            the address.
	 * @return {@code true} to include the address.
	 */
	protected abstract boolean isSpecific(ZmanimAddress address);

	@Override
	public int getCount() {
		return mSpecific.size();
	}

	@Override
	protected LocationItem getLocationItem(int position) {
		return mSpecific.get(position);
	}

	@Override
	public int getPosition(ZmanimAddress address) {
		final int size = mSpecific.size();
		LocationItem item;
		for (int i = 0; i < size; i++) {
			item = mSpecific.get(i);
			if (item.getAddress().equals(address))
				return i;
		}
		return super.getPosition(address);
	}

	@Override
	public void notifyDataSetChanged() {
		populateSpecific();
		super.notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetInvalidated() {
		populateSpecific();
		super.notifyDataSetInvalidated();
	}

}
