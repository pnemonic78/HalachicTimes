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

import java.lang.reflect.Field;
import java.util.List;

import net.sf.times.LocationAdapter.OnFavoriteClickListener;
import net.sf.times.location.AddressProvider;
import net.sf.times.location.CountriesGeocoder;
import net.sf.times.location.ZmanimAddress;
import android.app.SearchManager;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.CompoundButton;
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
public class LocationActivity extends TabActivity implements TextWatcher, OnClickListener, OnEditorActionListener, OnItemClickListener, OnFavoriteClickListener {

	private static final String TAG_ALL = "all";
	private static final String TAG_FAVORITES = "favorites";
	private static final String TAG_HISTORY = "history";

	private static int ic_menu_star;

	private EditText mSearchText;
	private CountriesGeocoder mCountries;
	private LocationAdapter mAdapterAll;
	private LocationAdapter mAdapterFavorites;
	private LocationAdapter mAdapterHistory;

	static {
		try {
			Class<?> clazz = Class.forName("com.android.internal.R$drawable");
			Field field = clazz.getDeclaredField("ic_menu_star");
			ic_menu_star = field.getInt(null);
		} catch (Exception e) {
			ic_menu_star = android.R.drawable.btn_star_big_off;
		}
	}

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
		Resources res = getResources();

		TabSpec tabFavorites = tabs.newTabSpec(TAG_FAVORITES);
		tabFavorites.setIndicator(null, res.getDrawable(ic_menu_star));
		tabFavorites.setContent(R.id.listFavorites);
		tabs.addTab(tabFavorites);

		TabSpec tabAll = tabs.newTabSpec(TAG_ALL);
		tabAll.setIndicator(null, res.getDrawable(android.R.drawable.ic_menu_mapmode));
		tabAll.setContent(android.R.id.list);
		tabs.addTab(tabAll);

		TabSpec tabHistory = tabs.newTabSpec(TAG_HISTORY);
		tabHistory.setIndicator(null, res.getDrawable(android.R.drawable.ic_menu_recent_history));
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
		ZmanimApplication app = (ZmanimApplication) getApplication();
		AddressProvider addressProvider = app.getAddresses();
		List<ZmanimAddress> cities = addressProvider.query();

		// "History" locations take precedence over "built-in" locations.
		cities.addAll(mCountries.getCities());

		LocationAdapter adapter = new LocationAdapter(this, cities);
		adapter.setOnFavoriteClickListener(this);
		adapter.sort();
		mAdapterAll = adapter;
		ListView list = (ListView) findViewById(android.R.id.list);
		list.setOnItemClickListener(this);
		list.setAdapter(adapter);

		adapter = new HistoryLocationAdapter(this, cities);
		adapter.setOnFavoriteClickListener(this);
		adapter.sort();
		mAdapterHistory = adapter;
		list = (ListView) findViewById(R.id.listHistory);
		list.setOnItemClickListener(this);
		list.setAdapter(adapter);

		adapter = new FavoritesLocationAdapter(this, cities);
		adapter.setOnFavoriteClickListener(this);
		adapter.sort();
		mAdapterFavorites = adapter;
		list = (ListView) findViewById(R.id.listFavorites);
		list.setOnItemClickListener(this);
		list.setAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> l, View view, int position, long id) {
		LocationAdapter adapter = mAdapterAll;
		switch (l.getId()) {
		case R.id.listFavorites:
			adapter = mAdapterFavorites;
			break;
		case R.id.listHistory:
			adapter = mAdapterHistory;
			break;
		}
		Address addr = adapter.getItem(position);
		Location loc = new Location(CountriesGeocoder.USER_PROVIDER);
		loc.setLatitude(addr.getLatitude());
		loc.setLongitude(addr.getLongitude());
		loc.setTime(System.currentTimeMillis());
		setAddress(loc);
	}

	@Override
	public void afterTextChanged(Editable s) {
		Filter filter;

		if (mAdapterAll != null) {
			filter = mAdapterAll.getFilter();
			filter.filter(s);
		}

		if (mAdapterFavorites != null) {
			filter = mAdapterFavorites.getFilter();
			filter.filter(s);
		}

		if (mAdapterHistory != null) {
			filter = mAdapterHistory.getFilter();
			filter.filter(s);
		}
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

	@Override
	public void onFavoriteClick(LocationAdapter adapter, CompoundButton button, ZmanimAddress address) {
		address.setFavorite(button.isChecked());
		// FIXME modify the db in non-UI thread.
		long idBefore = address.getId();
		AddressProvider provider = ((ZmanimApplication) getApplication()).getAddresses();
		if (idBefore < 0L) {
			address.setId(0L);
		}
		provider.insertOrUpdate(null, address);

		mAdapterAll.notifyDataSetChanged();
		mAdapterFavorites.notifyDataSetChanged();
		mAdapterHistory.notifyDataSetChanged();
	}

}
