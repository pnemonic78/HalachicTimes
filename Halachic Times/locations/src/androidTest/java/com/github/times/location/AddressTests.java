/*
 * Copyright 2019, Moshe Waisberg
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
package com.github.times.location;

import android.app.job.JobInfo;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationManager;
import android.text.format.DateUtils;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AddressTests {

    /**
     * Test job service.
     * {@link com.android.server.job.JobSchedulerService$JobSchedulerStub#enforceValidJobRequest(int, JobInfo)}
     */
    @Test
    public void testEnforceValidJobRequest() throws Exception {
        final Context context = getApplicationContext();
        assertNotNull(context);

        Class cls = AddressService.class;
        ComponentName service = new ComponentName(context, cls);
        PackageManager pm = context.getPackageManager();
        assertNotNull(pm);
        ServiceInfo si = pm.getServiceInfo(service,
                PackageManager.MATCH_DIRECT_BOOT_AWARE
                        | PackageManager.MATCH_DIRECT_BOOT_UNAWARE);
        assertNotNull(si);
        assertEquals(JobService.PERMISSION_BIND, si.permission);
    }

    /**
     * Test address service.
     */
    @Test
    public void testLocationChanged() {
        final Context context = getApplicationContext();
        assertNotNull(context);

        Location location = new ZmanimLocation(LocationManager.GPS_PROVIDER);
        location.setLatitude(36.9);
        location.setLongitude(120.7);
        location.setAccuracy(14);
        location.setAltitude(24.9);
        location.setSpeed(0.87f);
        location.setBearing(296.7f);
        location.setSpeedAccuracyMetersPerSecond(1);
        location.setBearingAccuracyDegrees(61);
        long ms = (4 * DateUtils.DAY_IN_MILLIS) + (11 * DateUtils.MINUTE_IN_MILLIS) + (57 * DateUtils.SECOND_IN_MILLIS) + 6;//+4d0h11m57s6ms
        location.setElapsedRealtimeNanos(TimeUnit.MILLISECONDS.toNanos(ms));

        Intent findAddress = new Intent(ZmanimLocationListener.ACTION_ADDRESS);
        findAddress.putExtra(ZmanimLocationListener.EXTRA_LOCATION, location);
        findAddress.putExtra(ZmanimLocationListener.EXTRA_PERSIST, false);
        AddressService.enqueueWork(context, findAddress);
    }
}
