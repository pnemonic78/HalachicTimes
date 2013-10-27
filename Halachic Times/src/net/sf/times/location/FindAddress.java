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

import android.location.Address;
import android.location.Location;

/**
 * Find an address.
 * 
 * @author Moshe Waisberg
 */
public class FindAddress extends Thread implements AddressProvider.OnFindAddressListener {

	public static interface OnFindAddressListener {

		/**
		 * Called when an address is found.
		 * 
		 * @param location
		 *            the requested location.
		 * @param address
		 *            the found address. Never {@code null}.
		 */
		public void onAddressFound(Location location, ZmanimAddress address);

	}

	/** The instance. */
	private static FindAddress mInstance;

	private final Location mLocation;
	private final OnFindAddressListener mListener;
	private final AddressProvider mAddressProvider;
	private boolean mCancelled;

	/** Creates a new finder. */
	private FindAddress(AddressProvider provider, Location location, OnFindAddressListener callback) {
		super();
		mAddressProvider = provider;
		mLocation = location;
		mListener = callback;
	}

	public static void find(AddressProvider provider, Location location, OnFindAddressListener callback) {
		if (mInstance != null) {
			mInstance.cancel();
		}
		FindAddress instance = new FindAddress(provider, location, callback);
		mInstance = instance;
		instance.start();
	}

	@Override
	public void run() {
		if (mCancelled)
			return;
		try {
			AddressProvider provider = mAddressProvider;
			Address nearest = provider.findNearestAddress(mLocation, this);
			if (mCancelled)
				return;
			onFindAddress(provider, mLocation, nearest);
		} finally {
			mInstance = null;
		}
	}

	/**
	 * Cancel finding.
	 */
	public void cancel() {
		mCancelled = true;
	}

	@Override
	public void onFindAddress(AddressProvider provider, Location location, Address address) {
		if (address == null)
			return;
		if (mCancelled)
			return;
		ZmanimAddress addr = (address instanceof ZmanimAddress) ? ((ZmanimAddress) address) : new ZmanimAddress(address);
		mListener.onAddressFound(location, addr);
	}

}
