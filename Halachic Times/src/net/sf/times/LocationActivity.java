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

import net.sf.times.location.CountriesGeocoder;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * Pick a city from the list.
 * 
 * @author Moshe Waisberg
 */
public class LocationActivity extends ListActivity implements OnClickListener {

	private EditText mSearchText;
	private Location mLocation;
	private CountriesGeocoder mCountries;

	/**
	 * Constructs a new activity.
	 */
	public LocationActivity() {
		super();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locations);
		EditText searchText = (EditText) findViewById(R.id.search_src_text);
		mSearchText = searchText;
		ImageView searchClear = (ImageView) findViewById(R.id.search_close_btn);
		searchClear.setOnClickListener(this);
		ImageView myLocation = (ImageView) findViewById(R.id.my_location);
		myLocation.setOnClickListener(this);

		mCountries = new CountriesGeocoder(this);

		// Get the intent, verify the action and get the query
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			Location loc = null;
			Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
			if (appData != null) {
				loc = appData.getParcelable(LocationManager.KEY_LOCATION_CHANGED);
			}

			doSearch(query, loc);
		}
	}

	/**
	 * Do the search.
	 * 
	 * @param query
	 *            the query.
	 * @param loc
	 *            the location.
	 */
	protected void doSearch(String query, Location loc) {
		mSearchText.setText(query);
		mLocation = loc;
		populateList();
	}

	@Override
	public void onClick(View view) {
		final int id = view.getId();

		if (id == R.id.search_close_btn) {
			mSearchText.setText(null);
		} else if (id == R.id.my_location) {
			mSearchText.setText(null);

			Intent data = new Intent();
			data.putExtra(LocationManager.KEY_LOCATION_CHANGED, (Location) null);
			setResult(RESULT_OK, data);
			finish();
		}
	}

	/**
	 * Populate the list with cities.
	 */
	protected void populateList() {
		List<Address> cities = mCountries.getCities();
		LocationAdapter adapter = new LocationAdapter(this, cities);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		LocationAdapter adapter = (LocationAdapter) getListAdapter();
		Address addr = adapter.getItem(position);
		Location loc = new Location(CountriesGeocoder.USER_PROVIDER);
		loc.setLatitude(addr.getLatitude());
		loc.setLongitude(addr.getLongitude());
		loc.setTime(System.currentTimeMillis());

		Intent data = new Intent();
		data.putExtra(LocationManager.KEY_LOCATION_CHANGED, loc);
		setResult(RESULT_OK, data);
		finish();
	}
}
