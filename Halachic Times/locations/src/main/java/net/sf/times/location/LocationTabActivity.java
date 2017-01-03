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

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import net.sf.app.ThemedActivity;
import net.sf.times.location.LocationAdapter.LocationItem;
import net.sf.times.location.LocationAdapter.OnFavoriteClickListener;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Pick a city from the list.
 *
 * @author Moshe Waisberg
 */
public abstract class LocationTabActivity extends ThemedActivity implements
        OnItemClickListener,
        OnFavoriteClickListener,
        SearchView.OnQueryTextListener,
        ZmanimLocationListener {

    private static final String TAG = "LocationTabActivity";

    private static final String TAG_ALL = "all";
    private static final String TAG_FAVORITES = "favorites";
    private static final String TAG_HISTORY = "history";

    private static final int REQUEST_ADD = 0xADD;

    private static int ic_menu_star;

    static {
        try {
            Resources res = Resources.getSystem();
            ic_menu_star = res.getIdentifier("ic_menu_star", "drawable", "android");
            if (ic_menu_star == 0) {
                Class<?> clazz = Class.forName("com.android.internal.R$drawable");
                Field field = clazz.getDeclaredField("ic_menu_star");
                ic_menu_star = field.getInt(null);
            }
        } catch (Exception e) {
            ic_menu_star = android.R.drawable.btn_star_big_off;
        }
    }

    private static final int WHAT_FAVORITE = 1;
    private static final int WHAT_ADDED = 2;

    private SearchView searchText;
    private LocationAdapter adapterAll;
    private LocationAdapter adapterFavorites;
    private LocationAdapter adapterHistory;
    private final Handler handler;
    /** Provider for locations. */
    private LocationsProvider locations;
    private Location locationForAddress;

    /**
     * Constructs a new activity.
     */
    public LocationTabActivity() {
        this.handler = new ActivityHandler(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res = getResources();

        LocationApplication app = (LocationApplication) getApplication();
        locations = app.getLocations();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.locations);

        SearchView searchText = (SearchView) findViewById(R.id.search_location);
        searchText.setOnQueryTextListener(this);
        this.searchText = searchText;

        TabHost tabs = (TabHost) findViewById(android.R.id.tabhost);
        tabs.setup();

        TabSpec tabFavorites = tabs.newTabSpec(TAG_FAVORITES);
        tabFavorites.setIndicator(null, res.getDrawable(ic_menu_star));
        tabFavorites.setContent(R.id.list_favorites);
        tabs.addTab(tabFavorites);

        TabSpec tabAll = tabs.newTabSpec(TAG_ALL);
        tabAll.setIndicator(null, res.getDrawable(android.R.drawable.ic_menu_mapmode));
        tabAll.setContent(android.R.id.list);
        tabs.addTab(tabAll);

        TabSpec tabHistory = tabs.newTabSpec(TAG_HISTORY);
        tabHistory.setIndicator(null, res.getDrawable(android.R.drawable.ic_menu_recent_history));
        tabHistory.setContent(R.id.list_history);
        tabs.addTab(tabHistory);

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

        // Switch to the first non-empty tab.
        if (adapterFavorites.getCount() == 0) {
            tabs.setCurrentTab(1);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        locations.start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        locations.stop(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.locations, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        if (id == android.R.id.home) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                finish();
                return true;
            }
        }
        // Cannot use 'switch' here because library ids are not final.
        if (id == R.id.menu_location_add) {
            addLocation();
            return true;
        }
        if (id == R.id.menu_location_here) {
            gotoHere();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Do the search.
     *
     * @param query
     *         the query.
     * @param loc
     *         the location.
     */
    protected void search(CharSequence query, Location loc) {
        populateLists();

        SearchView searchText = this.searchText;
        if (!TextUtils.isEmpty(query)) {
            searchText.requestFocus();
            searchText.setIconified(false);
        }
        searchText.setQuery(query, false);
    }

    /**
     * Populate the lists with cities.
     */
    protected void populateLists() {
        LocationApplication app = (LocationApplication) getApplication();
        AddressProvider provider = app.getAddresses();
        LocationFormatter formatter = app.getLocations();
        List<ZmanimAddress> addresses = provider.query(null);
        List<City> cities = provider.getCities();

        // "History" locations take precedence over "built-in" locations.
        addresses.addAll(cities);

        // Prepare the common list of items for all adapters.
        // Also to save time formatting the same addresses in each adapter by
        // themselves.
        List<LocationItem> items = new ArrayList<LocationItem>(addresses.size());
        for (ZmanimAddress addr : addresses) {
            items.add(new LocationItem(addr, formatter));
        }

        LocationAdapter adapter = new LocationAdapter(this, items);
        adapter.setOnFavoriteClickListener(this);
        adapterAll = adapter;
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setOnItemClickListener(this);
        list.setAdapter(adapter);

        adapter = new HistoryLocationAdapter(this, items);
        adapter.setOnFavoriteClickListener(this);
        adapterHistory = adapter;
        list = (ListView) findViewById(R.id.list_history);
        list.setOnItemClickListener(this);
        list.setAdapter(adapter);

        adapter = new FavoritesLocationAdapter(this, items);
        adapter.setOnFavoriteClickListener(this);
        adapterFavorites = adapter;
        list = (ListView) findViewById(R.id.list_favorites);
        list.setOnItemClickListener(this);
        list.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> l, View view, int position, long id) {
        LocationAdapter adapter = adapterAll;
        int i = l.getId();
        if (i == R.id.list_favorites) {
            adapter = adapterFavorites;
        } else if (i == R.id.list_history) {
            adapter = adapterHistory;
        }
        LocationItem item = adapter.getItem(position);
        ZmanimAddress addr = item.getAddress();
        Location loc = new Location(GeocoderBase.USER_PROVIDER);
        loc.setTime(System.currentTimeMillis());
        loc.setLatitude(addr.getLatitude());
        loc.setLongitude(addr.getLongitude());
        if (addr.hasElevation())
            loc.setAltitude(addr.getElevation());
        setAddress(loc);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (adapterAll != null) {
            adapterAll.getFilter().filter(newText);
        }
        if (adapterFavorites != null) {
            adapterFavorites.getFilter().filter(newText);
        }
        if (adapterHistory != null) {
            adapterHistory.getFilter().filter(newText);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (!TextUtils.isEmpty(query)) {
            Location loc = null;
            String[] tokens = query.split("[,;]");
            if (tokens.length >= 2) {
                try {
                    double latitude = Location.convert(tokens[0].trim());
                    double longitude = Location.convert(tokens[1].trim());

                    loc = new Location(GeocoderBase.USER_PROVIDER);
                    loc.setLatitude(latitude);
                    loc.setLongitude(longitude);
                    loc.setTime(System.currentTimeMillis());

                    if (tokens.length >= 3) {
                        double elevation = Double.parseDouble(tokens[2]);

                        loc.setAltitude(elevation);
                    }
                } catch (IllegalArgumentException e) {
                    // Not valid coordinate.
                }
            }
            setAddress(loc);
            return true;
        }

        return false;
    }

    /**
     * Set the result location and close the activity.
     *
     * @param location
     *         the location.
     */
    protected void setAddress(Location location) {
        LocationApplication app = (LocationApplication) getApplication();
        LocationsProvider locations = app.getLocations();
        locations.setLocation(location);

        Intent intent = getIntent();
        intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        setResult(RESULT_OK, intent);

        finish();
    }

    @Override
    public void onFavoriteClick(LocationAdapter adapter, CompoundButton button, ZmanimAddress address) {
        address.setFavorite(button.isChecked());
        handler.obtainMessage(WHAT_FAVORITE, address).sendToTarget();
    }

    /**
     * Set the location to "here".
     */
    private void gotoHere() {
        setAddress(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeMessages(WHAT_ADDED);
        handler.removeMessages(WHAT_FAVORITE);
    }

    /**
     * Get the locations provider.
     *
     * @return hte provider.
     */
    public LocationsProvider getLocations() {
        return locations;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_ADD) && (resultCode == RESULT_OK)) {
            Location location = data.getParcelableExtra(AddLocationActivity.EXTRA_LOCATION);
            addLocation(location);
        }
    }

    /**
     * Show the form to add a custom location.
     */
    private void addLocation() {
        Intent intent = new Intent(this, getAddLocationActivityClass());
        intent.setAction(Intent.ACTION_INSERT);
        startActivityForResult(intent, REQUEST_ADD);
    }

    protected Class<? extends Activity> getAddLocationActivityClass() {
        return AddLocationActivity.class;
    }

    /**
     * Add a custom location.
     *
     * @param location
     *         the new location.
     */
    private void addLocation(Location location) {
        if (location == null) {
            Log.w(TAG, "location empty");
            return;
        }

        String query = location.getLatitude() + ", " + location.getLongitude();
        SearchView searchText = this.searchText;
        searchText.setIconified(false);
        searchText.requestFocus();
        searchText.setQuery(query, false);

        fetchAddress(location);
    }

    private void fetchAddress(Location location) {
        this.locationForAddress = location;
        LocationsProvider locations = getLocations();
        locations.findAddress(location);
    }

    @Override
    public void onAddressChanged(Location location, final ZmanimAddress address) {
        if ((location == null) || (address == null)) {
            return;
        }
        if ((locationForAddress == null) || (location.getLatitude() != locationForAddress.getLatitude()) || (location.getLongitude() != locationForAddress.getLongitude())) {
            return;
        }
        handler.obtainMessage(WHAT_ADDED, address).sendToTarget();
    }

    @Override
    public void onElevationChanged(Location location) {
    }

    @Override
    public boolean isPassive() {
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    private static class ActivityHandler extends Handler {

        private final WeakReference<LocationTabActivity> activityWeakReference;

        public ActivityHandler(LocationTabActivity activity) {
            this.activityWeakReference = new WeakReference<LocationTabActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LocationTabActivity activity = activityWeakReference.get();
            if (activity == null) {
                return;
            }
            ZmanimAddress address;

            switch (msg.what) {
                case WHAT_FAVORITE:
                    address = (ZmanimAddress) msg.obj;
                    LocationApplication app = (LocationApplication) activity.getApplication();
                    AddressProvider provider = app.getAddresses();
                    provider.insertOrUpdateAddress(null, address);

                    activity.adapterAll.notifyDataSetChanged();
                    activity.adapterFavorites.notifyDataSetChanged();
                    activity.adapterHistory.notifyDataSetChanged();
                    break;
                case WHAT_ADDED:
                    // Refresh the lists with the new location's address.
                    address = (ZmanimAddress) msg.obj;
                    activity.populateLists();

                    String query = address.getFormatted();
                    SearchView searchText = activity.searchText;
                    searchText.setIconified(false);
                    searchText.requestFocus();
                    searchText.setQuery(query, false);
                    break;
            }
        }
    }
}
