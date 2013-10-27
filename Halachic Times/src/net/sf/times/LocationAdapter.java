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

import java.util.List;

import android.content.Context;
import android.location.Address;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @author Moshe Waisberg
 */
public class LocationAdapter extends ArrayAdapter<Address> {

	public LocationAdapter(Context context, List<Address> addresses) {
		super(context, R.layout.times_item, android.R.id.title, addresses);
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
}
