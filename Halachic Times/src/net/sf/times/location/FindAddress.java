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

import net.sf.times.location.AddressProvider.OnFindAddressListener;
import android.content.Context;
import android.location.Address;
import android.location.Location;

/**
 * Find an address.
 * 
 * @author Moshe Waisberg
 */
public class FindAddress extends Thread implements OnFindAddressListener {

	/** The instance. */
	private static FindAddress mInstance;

	private final Location mLocation;
	private final OnFindAddressListener mListener;
	private final AddressProvider mAddressProvider;
	private boolean mCancelled;

	/** Creates a new finder. */
	private FindAddress(Context context, Location location, OnFindAddressListener callback) {
		super();
		mAddressProvider = new AddressProvider(context);
		mLocation = location;
		mListener = callback;
	}

	public static void find(Context context, Location location, OnFindAddressListener callback) {
		if (mInstance != null) {
			mInstance.cancel();
		}
		FindAddress instance = new FindAddress(context, location, callback);
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
			close();
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
		mListener.onFindAddress(provider, location, addr);
	}

	/** Close. */
	protected void close() {
		cancel();
		mAddressProvider.close();
		mInstance = null;
	}
}
