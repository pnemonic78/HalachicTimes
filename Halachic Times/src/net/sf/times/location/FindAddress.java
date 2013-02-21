package net.sf.times.location;

import android.location.Address;
import android.location.Location;

/**
 * Find an address.
 * 
 * @author Moshe
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
