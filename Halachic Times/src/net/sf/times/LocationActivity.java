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
import java.util.List;

import net.sf.times.location.AddressProvider;
import net.sf.times.location.CountriesGeocoder;
import net.sf.times.location.ZmanimAddress;
import android.app.SearchManager;
import android.app.TabActivity;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * Pick a city from the list.
 * 
 * @author Moshe Waisberg
 */
@SuppressWarnings("deprecation")
public class LocationActivity extends TabActivity implements TextWatcher, OnClickListener, OnEditorActionListener, OnItemClickListener {

	private static final String TAG_ALL = "all";
	private static final String TAG_HISTORY = "history";

	private EditText mSearchText;
	private CountriesGeocoder mCountries;
	private LocationAdapter mAdapterAll;
	private LocationAdapter mAdapterHistory;

	/**
	 * Constructs a new activity.
	 */
	public LocationActivity() {
		super();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locations);

		EditText searchText = (EditText) findViewById(R.id.search_src_text);
		searchText.addTextChangedListener(this);
		searchText.setOnEditorActionListener(this);
		View searchTextParent = (View) searchText.getParent();
		searchTextParent.setBackgroundDrawable(searchText.getBackground());
		searchText.setBackgroundDrawable(null);
		mSearchText = searchText;

		ImageView searchClear = (ImageView) findViewById(R.id.search_close_btn);
		searchClear.setOnClickListener(this);

		ImageView myLocation = (ImageView) findViewById(R.id.my_location);
		myLocation.setOnClickListener(this);

		TabHost tabs = getTabHost();

		TabSpec tabAll = tabs.newTabSpec(TAG_ALL);
		tabAll.setIndicator(getText(R.string.all));
		tabAll.setContent(android.R.id.list);
		tabs.addTab(tabAll);

		TabSpec tabHistory = tabs.newTabSpec(TAG_HISTORY);
		tabHistory.setIndicator(getText(R.string.history));
		tabHistory.setContent(R.id.listHistory);
		tabs.addTab(tabHistory);

		mCountries = new CountriesGeocoder(this);

		Intent intent = getIntent();
		String query = intent.getStringExtra(SearchManager.QUERY);
		Location loc = intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
		if (loc == null) {
			Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
			if (appData != null) {
				loc = appData.getParcelable(LocationManager.KEY_LOCATION_CHANGED);
			}
		}

		search(query, loc);
	}

	/**
	 * Do the search.
	 * 
	 * @param query
	 *            the query.
	 * @param loc
	 *            the location.
	 */
	protected void search(String query, Location loc) {
		populateLists();

		EditText searchText = mSearchText;
		searchText.requestFocus();
		searchText.setText(query);
		if (!TextUtils.isEmpty(query))
			searchText.setSelection(query.length());
	}

	@Override
	public void onClick(View view) {
		final int id = view.getId();

		if (id == R.id.search_close_btn) {
			mSearchText.setText(null);
		} else if (id == R.id.my_location) {
			Intent data = new Intent();
			data.putExtra(LocationManager.KEY_LOCATION_CHANGED, (Location) null);
			setResult(RESULT_OK, data);
			finish();
		}
	}

	/**
	 * Populate the lists with cities.
	 */
	protected void populateLists() {
		List<ZmanimAddress> cities = mCountries.getCities();

		ZmanimApplication app = (ZmanimApplication) getApplication();
		AddressProvider addressProvider = app.getAddresses();
		List<ZmanimAddress> history = new ArrayList<ZmanimAddress>(addressProvider.query());
		cities.addAll(history);

		LocationAdapter adapter = new LocationAdapter(this, cities);
		adapter.sort();
		mAdapterAll = adapter;
		ListView list = (ListView) findViewById(android.R.id.list);
		list.setOnItemClickListener(this);
		list.setAdapter(adapter);

		adapter = new LocationAdapter(this, history);
		adapter.sort();
		mAdapterHistory = adapter;
		list = (ListView) findViewById(R.id.listHistory);
		list.setOnItemClickListener(this);
		list.setAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> l, View view, int position, long id) {
		LocationAdapter adapter = mAdapterAll;
		if (l.getId() == R.id.listHistory)
			adapter = mAdapterHistory;
		Address addr = adapter.getItem(position);
		Location loc = new Location(CountriesGeocoder.USER_PROVIDER);
		loc.setLatitude(addr.getLatitude());
		loc.setLongitude(addr.getLongitude());
		loc.setTime(System.currentTimeMillis());
		setAddress(loc);
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (mAdapterAll == null)
			return;
		Filter filter = mAdapterAll.getFilter();
		filter.filter(s);

		if (mAdapterHistory == null)
			return;
		filter = mAdapterHistory.getFilter();
		filter.filter(s);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		// Maybe user typed "latitude,longitude"?
		boolean submit = false;
		switch (actionId) {
		case 5:
		case 6:
			submit = true;
		case 0:
			if (event != null) {
				switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_ENTER:
					submit = true;
				}
			}
		}
		if (submit) {
			CharSequence text = v.getText();
			Location loc = null;
			if (!TextUtils.isEmpty(text)) {
				String textStr = text.toString();
				String[] tokens = textStr.split("[,;]");
				if (tokens.length >= 2) {
					try {
						double latitude = Location.convert(tokens[0]);
						double longitude = Location.convert(tokens[1]);

						loc = new Location(CountriesGeocoder.USER_PROVIDER);
						loc.setLatitude(latitude);
						loc.setLongitude(longitude);
						loc.setTime(System.currentTimeMillis());
					} catch (IllegalArgumentException e) {
						// Not valid coordinate.
					}
				}
			}
			setAddress(loc);
		}
		return submit;
	}

	/**
	 * Set the result location and close the activity.
	 * 
	 * @param location
	 *            the location.
	 */
	protected void setAddress(Location location) {
		Intent data = new Intent();
		data.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);

		Intent intent = getIntent();
		String action = intent.getAction();
		if (Intent.ACTION_SEARCH.equals(action)) {
			data.setClass(this, ZmanimActivity.class);
			data.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(data);
		} else {
			setResult(RESULT_OK, data);
		}

		finish();
	}

}
