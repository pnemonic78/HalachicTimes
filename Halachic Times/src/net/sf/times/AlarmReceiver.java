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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Receive alarm events.
 * 
 * @author Moshe Waisberg
 */
public class AlarmReceiver extends BroadcastReceiver {

	public AlarmReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean isAlarm = false;

		Bundle extras = intent.getExtras();
		if (extras != null) {
			isAlarm = (extras.getInt(Intent.EXTRA_ALARM_COUNT, 0) > 0);
		}

		if (isAlarm) {
			ZmanimSettings settings = new ZmanimSettings(context);
			ZmanimLocations locations = ZmanimLocations.getInstance(context);
			ZmanimReminder reminder = new ZmanimReminder(context);
			reminder.remind(settings, locations);
		}
	}
}
