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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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

	private final BlockingQueue<Location> mLocations = new ArrayBlockingQueue<Location>(10);
	private final List<OnFindAddressListener> mListeners = new ArrayList<OnFindAddressListener>();
	private final AddressProvider mAddressProvider;
	private boolean mRunning;

	/**
	 * Creates a new finder.
	 * 
	 * @param context
	 *            the context.
	 */
	public FindAddress(Context context) {
		super();
		mAddressProvider = new AddressProvider(context);
	}

	/**
	 * Creates a new finder.
	 * 
	 * @param context
	 *            the context.
	 * @param provider
	 *            the addresses provider.
	 */
	public FindAddress(Context context, AddressProvider provider) {
		super();
		mAddressProvider = provider;
	}

	/**
	 * Register an address listener.
	 * 
	 * @param listener
	 *            the listener to add.
	 */
	public void addListener(OnFindAddressListener listener) {
		if (!mListeners.contains(listener))
			mListeners.add(listener);
	}

	/**
	 * Remove an address listener.
	 * 
	 * @param listener
	 *            the listener to remove.
	 */
	public void removeListener(OnFindAddressListener listener) {
		mListeners.remove(listener);
	}

	/**
	 * Find the address for the location.
	 * 
	 * @param location
	 *            the location.
	 */
	public void find(Location location) {
		mLocations.add(location);
	}

	/**
	 * Find the address for the location.
	 * 
	 * @param location
	 *            the location.
	 * @param listener
	 *            the listener.
	 */
	public void find(Location location, OnFindAddressListener listener) {
		addListener(listener);
		mLocations.add(location);
	}

	@Override
	public void run() {
		mRunning = true;
		Location location;
		AddressProvider provider = mAddressProvider;
		Address nearest;
		try {
			while (mRunning) {
				location = mLocations.take();
				nearest = provider.findNearestAddress(location, this);
				onFindAddress(provider, location, nearest);
			}
		} catch (NoSuchElementException nsee) {
		} catch (InterruptedException ie) {
		} finally {
			cancel();
		}
	}

	@Override
	public void onFindAddress(AddressProvider provider, Location location, Address address) {
		if ((address == null) || !mRunning)
			return;
		ZmanimAddress addr = (address instanceof ZmanimAddress) ? ((ZmanimAddress) address) : new ZmanimAddress(address);

		// Make copy of listeners to avoid ConcurrentModificationException.
		Collection<OnFindAddressListener> listenersCopy = new ArrayList<OnFindAddressListener>(mListeners);
		for (OnFindAddressListener listener : listenersCopy)
			listener.onFindAddress(provider, location, addr);
	}

	/** Cancel finding and close. */
	public void cancel() {
		mRunning = false;
		mListeners.clear();
		mLocations.clear();
		mAddressProvider.close();
	}
}
