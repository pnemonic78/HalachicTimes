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

import net.sf.times.ZmanimApplication;
import net.sf.times.location.AddressProvider.OnFindAddressListener;
import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;

/**
 * Service to find an address.
 * 
 * @author Moshe Waisberg
 */
public class AddressService extends IntentService implements OnFindAddressListener {

	/** The location parameter. */
	public static final String PARAMETER_LOCATION = LocationManager.KEY_LOCATION_CHANGED;
	/** The address parameter. */
	public static final String PARAMETER_ADDRESS = "address";
	/** The intent action for an address that was found. */
	public static final String ADDRESS_ACTION = "net.sf.times.location.ADDRESS";

	private static final String NAME = "AddressService";

	private AddressProvider mAddressProvider;

	/**
	 * Constructs a new service.
	 * 
	 * @param name
	 *            the worker thread name.
	 */
	public AddressService(String name) {
		super(name);
	}

	/**
	 * Constructs a new service.
	 */
	public AddressService() {
		this(NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Location location = intent.getParcelableExtra(PARAMETER_LOCATION);
		if (location == null)
			return;

		final AddressProvider provider = mAddressProvider;
		if (provider == null)
			return;
		provider.findNearestAddress(location, this);
	}

	@Override
	public void onFindAddress(AddressProvider provider, Location location, Address address) {
		ZmanimAddress addr = null;
		if (address != null) {
			addr = (address instanceof ZmanimAddress) ? ((ZmanimAddress) address) : new ZmanimAddress(address);
			provider.insertAddress(location, addr);
		}

		Intent result = new Intent(ADDRESS_ACTION);
		result.putExtra(PARAMETER_LOCATION, location);
		result.putExtra(PARAMETER_ADDRESS, addr);
		sendBroadcast(result);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ZmanimApplication app = (ZmanimApplication) getApplication();
		mAddressProvider = app.getAddresses();
	}
}
