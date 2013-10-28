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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * Pick a city from the list.
 * 
 * @author Moshe Waisberg
 */
public class LocationActivity extends ListActivity implements TextWatcher, OnClickListener, OnLongClickListener {

	private EditText mSearchText;
	private CountriesGeocoder mCountries;
	private LocationAdapter mAdapter;

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
		searchText.addTextChangedListener(this);
		mSearchText = searchText;

		ImageView searchClear = (ImageView) findViewById(R.id.search_close_btn);
		searchClear.setOnClickListener(this);
		searchClear.setOnLongClickListener(this);

		ImageView myLocation = (ImageView) findViewById(R.id.my_location);
		myLocation.setOnClickListener(this);

		mCountries = new CountriesGeocoder(this);

		// Get the intent, verify the action and get the query
		// if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
		// }
		Intent intent = getIntent();
		String query = intent.getStringExtra(SearchManager.QUERY);
		Location loc = null;
		Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
		if (appData != null) {
			loc = appData.getParcelable(LocationManager.KEY_LOCATION_CHANGED);
		}

		doSearch(query, loc);
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
		populateList();
		mSearchText.setText(query);
	}

	@Override
	public void onClick(View view) {
		final int id = view.getId();

		if (id == R.id.search_close_btn) {
			EditText edit = mSearchText;
			if (edit != null) {
				edit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
			}
		} else if (id == R.id.my_location) {
			Intent data = new Intent();
			data.putExtra(LocationManager.KEY_LOCATION_CHANGED, (Location) null);
			setResult(RESULT_OK, data);
			finish();
		}
	}

	@Override
	public boolean onLongClick(View view) {
		final int id = view.getId();

		if (id == R.id.search_close_btn) {
			mSearchText.setText(null);
			return true;
		}
		return false;
	}

	/**
	 * Populate the list with cities.
	 */
	protected void populateList() {
		List<Address> cities = mCountries.getCities();
		LocationAdapter adapter = new LocationAdapter(this, cities);
		adapter.sort();
		mAdapter = adapter;
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

	@Override
	public void afterTextChanged(Editable s) {
		if (mAdapter == null)
			return;
		Filter filter = mAdapter.getFilter();
		filter.filter(s);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
}
