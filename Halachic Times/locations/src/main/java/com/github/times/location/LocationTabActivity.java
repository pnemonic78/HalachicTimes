/*
 * Copyright 2012, Moshe Waisberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.times.location;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.github.app.SimpleThemeCallbacks;
import com.github.app.ThemeCallbacks;
import com.github.preference.ThemePreferences;
import com.github.times.location.LocationAdapter.LocationItem;
import com.github.times.location.impl.FavoritesLocationAdapter;
import com.github.times.location.impl.HistoryLocationAdapter;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static com.github.times.location.GeocoderBase.USER_PROVIDER;

/**
 * Pick a city from the list.
 *
 * @author Moshe Waisberg
 */
public abstract class LocationTabActivity<P extends ThemePreferences> extends Activity implements
    ThemeCallbacks<P>,
    LocationAdapter.LocationItemListener,
    SearchView.OnQueryTextListener,
    ZmanimLocationListener,
    LocationAdapter.FilterListener {

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
                @SuppressLint("PrivateApi") Class<?> clazz = Class.forName("com.android.internal.R$drawable");
                Field field = clazz.getDeclaredField("ic_menu_star");
                ic_menu_star = field.getInt(null);
            }
        } catch (Exception e) {
            ic_menu_star = android.R.drawable.btn_star_big_off;
        }
    }

    private static final int WHAT_FAVORITE = 1;
    private static final int WHAT_ADDED = 2;
    private static final int WHAT_DELETE = 3;

    private ThemeCallbacks<P> themeCallbacks;
    private SearchView searchText;
    private LocationAdapter adapterAll;
    private LocationAdapter adapterFavorites;
    private LocationAdapter adapterHistory;
    private final Handler handler;
    /**
     * Provider for locations.
     */
    private LocationsProvider locations;
    private Location locationForAddress;
    private TabHost tabHost;
    private AddressProvider addressProvider;

    /**
     * Constructs a new activity.
     */
    public LocationTabActivity() {
        this.handler = new ActivityHandler(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreate();
        final Resources res = getResources();

        final LocationApplication app = (LocationApplication) getApplication();
        locations = app.getLocations();

        if (VERSION.SDK_INT < JELLY_BEAN) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        setContentView(R.layout.locations);

        SearchView searchText = findViewById(R.id.search_location);
        searchText.setOnQueryTextListener(this);
        this.searchText = searchText;

        TabHost tabs = findViewById(android.R.id.tabhost);
        tabs.setup();
        this.tabHost = tabs;

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

        populateLists();
        search(query);
    }

    @Override
    public void onCreate() {
        if (themeCallbacks == null) {
            themeCallbacks = createThemeCallbacks();
        }
        themeCallbacks.onCreate();
    }

    @Override
    public P getThemePreferences() {
        return themeCallbacks.getThemePreferences();
    }

    protected ThemeCallbacks<P> createThemeCallbacks() {
        return new SimpleThemeCallbacks<>(this);
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
            if (VERSION.SDK_INT < JELLY_BEAN) {
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
     * @param query the query.
     */
    protected void search(CharSequence query) {
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
        final Context context = this;
        AddressProvider provider = getAddressProvider();
        LocationFormatter formatter = getLocations();
        List<ZmanimAddress> addresses = provider.queryAddresses(null);
        List<City> cities = provider.getCities();

        // "History" locations take precedence over "built-in" locations.
        addresses.addAll(cities);

        // Prepare the common list of items for all adapters.
        // Also to save time formatting the same addresses in each adapter by themselves.
        List<LocationItem> items = new ArrayList<>(addresses.size());
        for (ZmanimAddress address : addresses) {
            items.add(new LocationItem(address, formatter));
        }

        final LocationAdapter.LocationItemListener itemListener = this;
        final LocationAdapter.FilterListener filterListener = this;

        LocationAdapter adapter = new LocationAdapter(context, items, itemListener);
        adapterAll = adapter;
        RecyclerView list = findViewById(android.R.id.list);
        list.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        list.setAdapter(adapter);
        LocationSwipeHandler swipeHandler = new LocationSwipeHandler(this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHandler);
        itemTouchHelper.attachToRecyclerView(list);

        adapter = new HistoryLocationAdapter(context, items, itemListener, filterListener);
        adapterHistory = adapter;
        list = findViewById(R.id.list_history);
        list.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        list.setAdapter(adapter);
        swipeHandler = new LocationSwipeHandler(this);
        itemTouchHelper = new ItemTouchHelper(swipeHandler);
        itemTouchHelper.attachToRecyclerView(list);

        adapter = new FavoritesLocationAdapter(context, items, itemListener, filterListener);
        adapterFavorites = adapter;
        list = findViewById(R.id.list_favorites);
        list.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        list.setAdapter(adapter);
        swipeHandler = new LocationSwipeHandler(this);
        itemTouchHelper = new ItemTouchHelper(swipeHandler);
        itemTouchHelper.attachToRecyclerView(list);
    }

    @Override
    public void onItemClick(LocationItem item) {
        ZmanimAddress address = item.getAddress();
        Location loc = new Location(USER_PROVIDER);
        loc.setTime(System.currentTimeMillis());
        loc.setLatitude(address.getLatitude());
        loc.setLongitude(address.getLongitude());
        if (address.hasElevation()) {
            loc.setAltitude(address.getElevation());
        }
        setAddress(loc);
    }

    @Override
    public void onFavoriteClick(LocationItem item, boolean checked) {
        ZmanimAddress address = item.getAddress();
        address.setFavorite(checked);
        handler.obtainMessage(WHAT_FAVORITE, address).sendToTarget();
    }

    @Override
    public void onItemSwipe(LocationItem item) {
        ZmanimAddress address = item.getAddress();
        handler.obtainMessage(WHAT_DELETE, address).sendToTarget();
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
            String[] tokens = query.split("[,;]");
            if (tokens.length >= 2) {
                final String token0 = tokens[0].trim();
                final String token1 = tokens[1].trim();
                if (!TextUtils.isEmpty(token0) && !TextUtils.isEmpty(token1)) {
                    try {
                        double latitude = Location.convert(token0);
                        double longitude = Location.convert(token1);

                        Location loc = new Location(USER_PROVIDER);
                        loc.setLatitude(latitude);
                        loc.setLongitude(longitude);
                        loc.setTime(System.currentTimeMillis());

                        setAddress(loc);
                        return true;
                    } catch (Exception ignore) {
                        // Not a valid coordinate.
                    }
                }
            }
        }

        return false;
    }

    /**
     * Set the result location and close the activity.
     *
     * @param location the location.
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
        // Don't pass the whole location because we are not editing it, but only using as an example.
        Location location = getLocations().getLocation();
        intent.putExtra(AddLocationActivity.EXTRA_LATITUDE, location.getLatitude());
        intent.putExtra(AddLocationActivity.EXTRA_LONGITUDE, location.getLongitude());
        startActivityForResult(intent, REQUEST_ADD);
    }

    protected Class<? extends Activity> getAddLocationActivityClass() {
        return AddLocationActivity.class;
    }

    /**
     * Add a custom location.
     *
     * @param location the new location.
     */
    private void addLocation(Location location) {
        if (location == null) {
            Timber.w("add empty location");
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

    @Override
    public void onFilterComplete(LocationAdapter adapter, int count) {
        // Switch to the first non-empty tab.
        if ((count == 0) && (adapter == adapterFavorites) && (tabHost.getCurrentTab() == 0)) {
            tabHost.setCurrentTab(1);
        }
    }

    protected AddressProvider getAddressProvider() {
        if (addressProvider == null) {
            addressProvider = new AddressProvider(this);
        }
        return addressProvider;
    }

    private static class ActivityHandler extends Handler {

        private final WeakReference<LocationTabActivity> activityWeakReference;

        ActivityHandler(LocationTabActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final LocationTabActivity activity = activityWeakReference.get();
            if (activity == null) {
                return;
            }
            ZmanimAddress address;
            AddressProvider addressProvider;

            switch (msg.what) {
                case WHAT_FAVORITE:
                    address = (ZmanimAddress) msg.obj;
                    addressProvider = activity.getAddressProvider();
                    addressProvider.insertOrUpdateAddress(null, address);

                    activity.adapterAll.notifyItemChanged(address);
                    activity.adapterFavorites.notifyItemChanged(address);
                    activity.adapterHistory.notifyItemChanged(address);
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
                case WHAT_DELETE:
                    address = (ZmanimAddress) msg.obj;
                    addressProvider = activity.getAddressProvider();
                    if (addressProvider.deleteAddress(address)) {
                        activity.adapterAll.delete(address);
                        activity.adapterFavorites.delete(address);
                        activity.adapterHistory.delete(address);
                    }
                    break;
            }
        }
    }
}
