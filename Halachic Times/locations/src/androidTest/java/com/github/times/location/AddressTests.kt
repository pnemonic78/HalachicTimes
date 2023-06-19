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
package com.github.times.location

import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.text.format.DateUtils
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.concurrent.TimeUnit

class AddressTests {
    /**
     * Test job service.
     * [com.android.server.job.JobSchedulerService.JobSchedulerStub.enforceValidJobRequest]
     */
    @Test
    fun testEnforceValidJobRequest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertNotNull(context)

        val cls: Class<*> = AddressService::class.java
        val service = ComponentName(context, cls)
        val pm = context.packageManager
        assertNotNull(pm)
        val si = pm.getServiceInfo(
            service, PackageManager.MATCH_DIRECT_BOOT_AWARE or PackageManager.MATCH_DIRECT_BOOT_UNAWARE
        )
        assertNotNull(si)
        assertEquals(JobService.PERMISSION_BIND, si.permission)
    }

    /**
     * Test address service.
     */
    @Test
    fun testLocationChanged() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertNotNull(context)

        val location: Location = ZmanimLocation(LocationManager.GPS_PROVIDER)
        location.latitude = 36.9
        location.longitude = 120.7
        location.accuracy = 14f
        location.altitude = 24.9
        location.speed = 0.87f
        location.bearing = 296.7f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            location.speedAccuracyMetersPerSecond = 1f
            location.bearingAccuracyDegrees = 61f
        }
        val ms = 4 * DateUtils.DAY_IN_MILLIS + 11 * DateUtils.MINUTE_IN_MILLIS + 57 * DateUtils.SECOND_IN_MILLIS + 6 //+4d0h11m57s6ms
        location.elapsedRealtimeNanos = TimeUnit.MILLISECONDS.toNanos(ms)

        val findAddress = Intent(ZmanimLocationListener.ACTION_ADDRESS)
        findAddress.putExtra(ZmanimLocationListener.EXTRA_LOCATION, location)
        findAddress.putExtra(ZmanimLocationListener.EXTRA_PERSIST, false)
        AddressService.enqueueWork(context, findAddress)
    }
}