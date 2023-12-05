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
package com.github.times.location

import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.TabHost
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.app.SimpleThemeCallbacks
import com.github.app.ThemeCallbacks
import com.github.os.getParcelableCompat
import com.github.preference.ThemePreferences
import com.github.times.location.LocationAdapter.LocationItem
import com.github.times.location.LocationAdapter.LocationItemListener
import com.github.times.location.databinding.LocationsBinding
import com.github.times.location.impl.FavoritesLocationAdapter
import com.github.times.location.impl.HistoryLocationAdapter
import java.lang.ref.WeakReference
import kotlin.math.abs
import timber.log.Timber

/**
 * Pick a city from the list.
 *
 * @author Moshe Waisberg
 */
@SuppressLint("DiscouragedApi", "PrivateApi")
abstract class LocationTabActivity<P : ThemePreferences> : AppCompatActivity(),
    ThemeCallbacks<P>,
    LocationItemListener,
    SearchView.OnQueryTextListener,
    ZmanimLocationListener,
    LocationAdapter.FilterListener {

    private val themeCallbacks: ThemeCallbacks<P> by lazy { createThemeCallbacks(this) }

    override val themePreferences: P
        get() = themeCallbacks.themePreferences

    private lateinit var binding: LocationsBinding
    private var searchText: SearchView? = null
    private var adapterAll: LocationAdapter? = null
    private var adapterFavorites: LocationAdapter? = null
    private var adapterHistory: LocationAdapter? = null
    private val handler: Handler = ActivityHandler(this)
    private var tabHost: TabHost? = null

    private val locations: LocationsProvider by lazy {
        (application as LocationApplication<*, *, *>).locations
    }
    private val addressProvider: AddressProvider by lazy { AddressProvider(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        onPreCreate()
        super.onCreate(savedInstanceState)

        val binding = LocationsBinding.inflate(layoutInflater)
        this.binding = binding
        setContentView(binding.root)

        val searchText = binding.searchBar.searchLocation
        searchText.setOnQueryTextListener(this)
        this.searchText = searchText

        val res = resources
        val tabs: TabHost = binding.tabhost
        tabs.setup()
        tabHost = tabs

        val tabFavorites = tabs.newTabSpec(TAG_FAVORITES)
        tabFavorites.setIndicator(null, ResourcesCompat.getDrawable(res, ic_menu_star, null))
        tabFavorites.setContent(R.id.list_favorites)
        tabs.addTab(tabFavorites)

        val tabAll = tabs.newTabSpec(TAG_ALL)
        tabAll.setIndicator(
            null,
            ResourcesCompat.getDrawable(res, android.R.drawable.ic_menu_mapmode, null)
        )
        tabAll.setContent(android.R.id.list)
        tabs.addTab(tabAll)

        val tabHistory = tabs.newTabSpec(TAG_HISTORY)
        tabHistory.setIndicator(
            null,
            ResourcesCompat.getDrawable(res, android.R.drawable.ic_menu_recent_history, null)
        )
        tabHistory.setContent(R.id.list_history)
        tabs.addTab(tabHistory)

        populateLists(binding)
    }

    override fun onPreCreate() {
        themeCallbacks.onPreCreate()
    }

    protected open fun createThemeCallbacks(context: Context): ThemeCallbacks<P> {
        return SimpleThemeCallbacks(context)
    }

    override fun onStart() {
        super.onStart()
        locations.start(this)
        onNewIntent(intent)
    }

    override fun onStop() {
        super.onStop()
        locations.stop(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val query = intent.getStringExtra(SearchManager.QUERY)
        search(query)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menuInflater.inflate(R.menu.locations, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_location_add -> {
                addLocation()
                return true
            }

            R.id.menu_location_here -> {
                gotoHere()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Do the search.
     *
     * @param query the query.
     */
    protected fun search(query: CharSequence?) {
        val searchText = searchText ?: return
        if (!query.isNullOrEmpty()) {
            searchText.requestFocus()
            searchText.isIconified = false
        }
        searchText.setQuery(query, false)
    }

    /**
     * Populate the lists with cities.
     */
    private fun populateLists(binding: LocationsBinding) {
        val context: Context = binding.root.context
        val provider = addressProvider
        val formatter: LocationFormatter = locations
        val addresses = provider.queryAddresses(null)
        val cities = provider.cities

        // Prepare the common list of items for all adapters.
        // Also to save time formatting the same addresses in each adapter by themselves.
        // "History" locations take precedence over "built-in" locations.
        val items = mutableListOf<LocationItem>()
        for (city in cities) {
            items.add(LocationItem(city, formatter))
        }
        for (address in addresses) {
            items.add(LocationItem(address, formatter))
        }

        val itemListener: LocationItemListener = this
        val filterListener: LocationAdapter.FilterListener = this
        val swipeHandler = LocationSwipeHandler(this)

        var adapter = LocationAdapter(context, items, itemListener)
        adapterAll = adapter
        var list: RecyclerView = binding.list
        list.adapter = adapter
        var itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(list)

        adapter = HistoryLocationAdapter(context, items, itemListener, filterListener)
        adapterHistory = adapter
        list = binding.listHistory
        list.adapter = adapter
        itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(list)

        adapter = FavoritesLocationAdapter(context, items, itemListener, filterListener)
        adapterFavorites = adapter
        list = binding.listFavorites
        list.adapter = adapter
        itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(list)
    }

    override fun onItemClick(item: LocationItem) {
        val address = item.address
        val location = Location(GeocoderBase.USER_PROVIDER)
        location.time = System.currentTimeMillis()
        location.latitude = address.latitude
        location.longitude = address.longitude
        if (address.hasElevation()) {
            location.altitude = address.elevation
        }
        setAddress(location)
    }

    override fun onFavoriteClick(item: LocationItem, checked: Boolean) {
        val address = item.address
        address.isFavorite = checked
        handler.obtainMessage(WHAT_FAVORITE, address).sendToTarget()
    }

    override fun onItemSwipe(item: LocationItem) {
        val address = item.address
        handler.obtainMessage(WHAT_DELETE, address).sendToTarget()
    }

    override fun onQueryTextChange(newText: String): Boolean {
        adapterAll?.filter?.filter(newText)
        adapterFavorites?.filter?.filter(newText)
        adapterHistory?.filter?.filter(newText)
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    /**
     * Set the result location and close the activity.
     *
     * @param location the location.
     */
    private fun setAddress(location: Location?) {
        val intent = intent
            .putExtra(LocationManager.KEY_LOCATION_CHANGED, location)
        setResult(RESULT_OK, intent)
        finish()
    }

    /**
     * Set the location to "here".
     */
    private fun gotoHere() {
        setAddress(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeMessages(WHAT_ADDED)
        handler.removeMessages(WHAT_FAVORITE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD && resultCode == RESULT_OK) {
            val location =
                data?.getParcelableCompat(AddLocationActivity.EXTRA_LOCATION, Location::class.java)
            if (location != null) {
                addLocation(location)
            }
        }
    }

    /**
     * Show the form to add a custom location.
     */
    private fun addLocation() {
        // Don't pass the whole location because we are not editing it, but only using as an example.
        val location = locations.getLocation()
        val intent = Intent(this, addLocationActivityClass)
            .setAction(Intent.ACTION_INSERT)
        if (location != null) {
            intent.putExtra(AddLocationActivity.EXTRA_LATITUDE, location.latitude)
                .putExtra(AddLocationActivity.EXTRA_LONGITUDE, location.longitude)
        }
        startActivityForResult(intent, REQUEST_ADD)
    }

    protected open val addLocationActivityClass: Class<out Activity>
        get() = AddLocationActivity::class.java

    /**
     * Add a custom location.
     *
     * @param location the new location.
     */
    private fun addLocation(location: Location?) {
        if (location == null) {
            Timber.w("add empty location")
            return
        }
        fetchAddress(location)
        val formatter: LocationFormatter = locations
        val query: CharSequence = formatter.formatLongitude(abs(location.longitude))
        search(query)
    }

    private fun fetchAddress(location: Location) {
        val locations = locations
        locations.findAddress(location)
    }

    override fun onAddressChanged(location: Location, address: ZmanimAddress) {
        handler.obtainMessage(WHAT_ADDED, address).sendToTarget()
    }

    override fun onElevationChanged(location: Location) = Unit
    override fun onLocationChanged(location: Location) = Unit

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) = Unit
    override fun onProviderEnabled(provider: String) = Unit
    override fun onProviderDisabled(provider: String) = Unit
    override fun onFilterComplete(adapter: LocationAdapter?, count: Int) {
        // Switch to the first non-empty tab.
        val tabHost = tabHost ?: return
        if (count == 0 && adapter === adapterFavorites && tabHost.currentTab == TAB_FAVORITES) {
            tabHost.currentTab = TAB_ALL
        }
    }

    private class ActivityHandler(activity: LocationTabActivity<*>) :
        Handler(Looper.getMainLooper()) {

        private val activityWeakReference = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            val activity = activityWeakReference.get() ?: return
            val address: ZmanimAddress
            when (msg.what) {
                WHAT_FAVORITE -> {
                    address = msg.obj as? ZmanimAddress ?: return
                    activity.markFavorite(address)
                }

                WHAT_ADDED -> {
                    address = msg.obj as? ZmanimAddress ?: return
                    activity.addAddress(address)
                }

                WHAT_DELETE -> {
                    address = msg.obj as? ZmanimAddress ?: return
                    activity.deleteAddress(address)
                }
            }
        }
    }

    private fun markFavorite(address: ZmanimAddress) {
        saveAddress(address)
        adapterAll?.notifyItemChanged(address)
        adapterFavorites?.notifyItemChanged(address)
        adapterHistory?.notifyItemChanged(address)
    }

    private fun addAddress(address: ZmanimAddress) {
        saveAddress(address)

        // Refresh the lists with the new location's address.
        populateLists(binding)
        tabHost?.currentTab = TAB_HISTORY
        val formatter: LocationFormatter = locations
        val query: CharSequence = formatter.formatLongitude(abs(address.longitude))
        search(query)
    }

    private fun saveAddress(address: ZmanimAddress) {
        val addressProvider = addressProvider
        addressProvider.insertOrUpdateAddress(null, address)
    }

    private fun deleteAddress(address: ZmanimAddress) {
        val addressProvider = addressProvider
        if (addressProvider.deleteAddress(address)) {
            adapterAll?.delete(address)
            adapterFavorites?.delete(address)
            adapterHistory?.delete(address)
        }
    }

    companion object {
        private const val TAG_ALL = "all"
        private const val TAG_FAVORITES = "favorites"
        private const val TAG_HISTORY = "history"

        private const val WHAT_FAVORITE = 1
        private const val WHAT_ADDED = 2
        private const val WHAT_DELETE = 3

        private const val TAB_FAVORITES = 0
        private const val TAB_ALL = 1
        private const val TAB_HISTORY = 2

        private const val REQUEST_ADD = 0xADD

        private var ic_menu_star = 0

        init {
            try {
                val res = Resources.getSystem()
                ic_menu_star = res.getIdentifier("ic_menu_star", "drawable", "android")
                if (ic_menu_star == 0) {
                    val clazz = Class.forName("com.android.internal.R\$drawable")
                    val field = clazz.getDeclaredField("ic_menu_star")
                    ic_menu_star = field.getInt(null)
                }
            } catch (e: Exception) {
                ic_menu_star = android.R.drawable.btn_star_big_off
            }
        }
    }
}