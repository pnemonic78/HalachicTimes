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
public class FindAddress extends Thread {

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
	private static boolean mFindingAddress;

	private final Location mLocation;
	private final OnFindAddressListener mListener;
	private final AddressProvider mAddressProvider;

	/** Creates a new finder. */
	private FindAddress(AddressProvider provider, Location location, OnFindAddressListener callback) {
		super();
		mAddressProvider = provider;
		mLocation = location;
		mListener = callback;
	}

	public static void find(AddressProvider provider, Location location, OnFindAddressListener callback) {
		if (mInstance == null) {
			if (!mFindingAddress) {
				mFindingAddress = true;
				mInstance = new FindAddress(provider, location, callback);
				mInstance.start();
			}
		}
	}

	@Override
	public void run() {
		try {
			AddressProvider provider = mAddressProvider;
			Address nearest = provider.findNearestAddress(mLocation);
			if (nearest != null) {
				ZmanimAddress addr = (nearest instanceof ZmanimAddress) ? ((ZmanimAddress) nearest) : new ZmanimAddress(nearest);
				if (addr != null) {
					mListener.onAddressFound(mLocation, addr);
				}
			}
		} finally {
			mInstance = null;
			mFindingAddress = false;
		}
	}
}
