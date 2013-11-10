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

import net.sf.times.location.AddressProvider.OnFindAddressListener;
import net.sf.times.location.FindAddress;
import android.app.Application;
import android.location.Location;

/**
 * Zmanim application.
 * 
 * @author Moshe Waisberg
 */
public class ZmanimApplication extends Application {

	/** The finder instance. */
	private FindAddress mFinder;

	/**
	 * Constructs a new application.
	 */
	public ZmanimApplication() {
		super();
	}

	/**
	 * Find an address.
	 * 
	 * @param location
	 *            the location.
	 * @param listener
	 *            the callback listener.
	 * @return the address finder.
	 */
	public void findAddress(Location location, OnFindAddressListener listener) {
		if (mFinder == null) {
			mFinder = new FindAddress(this);
			mFinder.start();
		}
		mFinder.find(location, listener);
	}

	@Override
	public void onTerminate() {
		if (mFinder != null) {
			mFinder.cancel();
			mFinder = null;
		}
		super.onTerminate();
	}
}
