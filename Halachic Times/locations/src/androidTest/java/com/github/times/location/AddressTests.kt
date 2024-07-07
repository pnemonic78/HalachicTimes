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
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.text.format.DateUtils
import com.github.BaseTests
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AddressTests : BaseTests() {
    /**
     * Test job service.
     * [com.android.server.job.JobSchedulerService.JobSchedulerStub.enforceValidJobRequest]
     */
    @Test
    fun testEnforceValidJobRequest() {
        assertNotNull(context)
        val clazz: Class<*> = AddressService::class.java
        val service = ComponentName(context, clazz)
        val pm = context.packageManager
        assertNotNull(pm)
        val si = pm.getServiceInfo(
            service, PackageManager.MATCH_DIRECT_BOOT_AWARE
                or PackageManager.MATCH_DIRECT_BOOT_UNAWARE
        )
        assertNotNull(si)
        assertEquals(JobService.PERMISSION_BIND, si.permission)
    }

    /**
     * Test address service.
     */
    @Test
    fun testLocationChanged() {
        assertNotNull(context)
        val ms = 4 * DateUtils.DAY_IN_MILLIS +
            11 * DateUtils.MINUTE_IN_MILLIS +
            57 * DateUtils.SECOND_IN_MILLIS +
            6 //+4d0h11m57s6ms
        val location: Location = ZmanimLocation(LocationManager.GPS_PROVIDER).apply {
            latitude = 36.9
            longitude = 120.7
            accuracy = 14f
            altitude = 24.9
            speed = 0.87f
            bearing = 296.7f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                speedAccuracyMetersPerSecond = 1f
                bearingAccuracyDegrees = 61f
            }
            elapsedRealtimeNanos = TimeUnit.MILLISECONDS.toNanos(ms)
        }
        val findAddress = Intent(ZmanimLocationListener.ACTION_ADDRESS)
            .putExtra(ZmanimLocationListener.EXTRA_LOCATION, location)
            .putExtra(ZmanimLocationListener.EXTRA_PERSIST, false)
        AddressService.enqueueWork(context, findAddress)
    }
}