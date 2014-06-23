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

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Location that is partially stored in the local database.
 * 
 * @author Moshe Waisberg
 */
public class ZmanimLocation extends Location {

	private long mId;

	/**
	 * Constructs a new location.
	 * 
	 * @param provider
	 *            the name of the provider that generated this location.
	 */
	public ZmanimLocation(String provider) {
		super(provider);
	}

	/**
	 * Construct a new location that is copied from an existing one.
	 * 
	 * @param location
	 *            the source location.
	 */
	public ZmanimLocation(Location location) {
		super(location);
	}

	/**
	 * Get the id.
	 * 
	 * @return the id
	 */
	public long getId() {
		return mId;
	}

	/**
	 * Set the id.
	 * 
	 * @param id
	 *            the id.
	 */
	public void setId(long id) {
		this.mId = id;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		super.writeToParcel(parcel, flags);
		parcel.writeLong(mId);
	}

	public static final Parcelable.Creator<ZmanimLocation> CREATOR = new Parcelable.Creator<ZmanimLocation>() {
		@Override
		public ZmanimLocation createFromParcel(Parcel source) {
			Location l = Location.CREATOR.createFromParcel(source);
			ZmanimLocation zl = new ZmanimLocation(l);
			zl.mId = source.readLong();
			return zl;
		}

		@Override
		public ZmanimLocation[] newArray(int size) {
			return new ZmanimLocation[size];
		}
	};

}
