package com.github.times.location

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Location
import android.os.Bundle
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.times.location.AddressProvider.OnFindAddressListener
import com.github.times.location.ZmanimLocationListener.Companion.ACTION_ADDRESS
import com.github.times.location.ZmanimLocationListener.Companion.ACTION_ELEVATION
import com.github.times.location.ZmanimLocationListener.Companion.EXTRA_ADDRESS
import com.github.times.location.ZmanimLocationListener.Companion.EXTRA_FORCE
import com.github.times.location.ZmanimLocationListener.Companion.EXTRA_LOCATION
import com.github.times.location.ZmanimLocationListener.Companion.EXTRA_PERSIST
import timber.log.Timber

/**
 * Background worker for addresses.
 *
 * @param context The context.
 * @param params  Parameters to setup the internal state of this worker.
 * @author Moshe Waisberg
 */
class AddressWorker(context: Context, params: WorkerParameters) : Worker(context, params),
    OnFindAddressListener {
    private val addressProvider: AddressProvider = AddressProvider(context)

    override fun doWork(): Result {
        val data = inputData
        val action = data.getString(DATA_ACTION)
        val location = data.getLocation(DATA_LOCATION) ?: return Result.failure()
        val provider = addressProvider

        when (action) {
            ACTION_ADDRESS -> {
                val force = data.getBoolean(DATA_FORCE, FORCE_DEFAULT)
                val persist = data.getBoolean(DATA_PERSIST, PERSIST_DEFAULT)
                val extras = location.extras ?: Bundle()
                extras.putBoolean(EXTRA_FORCE, force)
                extras.putBoolean(EXTRA_PERSIST, persist)
                location.extras = extras
                provider.findNearestAddress(location, this)
            }

            ACTION_ELEVATION -> {
                provider.findElevation(location, this)
            }
        }
        return Result.success()
    }

    override fun onFindAddress(
        provider: AddressProvider,
        location: Location,
        address: Address
    ) {
        val addr: ZmanimAddress?
        if (address is ZmanimAddress) {
            addr = address
        } else {
            addr = ZmanimAddress(address)
            if (location.hasAltitude()) {
                addr.elevation = location.altitude
            }
        }
        val extras = location.extras
        if (extras?.getBoolean(EXTRA_PERSIST, PERSIST_DEFAULT) ?: PERSIST_DEFAULT) {
            provider.insertOrUpdateAddress(location, addr)
        }
        Timber.i("find address: %s %s", location, addr)
        val context = applicationContext
        val result = Intent(ACTION_ADDRESS)
            .setPackage(context.packageName)
            .putExtra(EXTRA_LOCATION, location)
            .putExtra(EXTRA_ADDRESS, addr)
        context.sendBroadcast(result)
    }

    override fun onFindElevation(
        provider: AddressProvider,
        location: Location,
        elevated: Location
    ) {
        if (elevated is ZmanimLocation) {
            provider.insertOrUpdateElevation(elevated)
        }
        Timber.i("find elevation: %s %s", location, elevated)
        val context = applicationContext
        val result = Intent(ACTION_ELEVATION)
            .setPackage(context.packageName)
            .putExtra(EXTRA_LOCATION, elevated)
        context.sendBroadcast(result)
    }

    companion object {
        private const val DATA_ACTION = "android.intent.action"
        private const val DATA_LOCATION = EXTRA_LOCATION
        private const val DATA_FORCE = EXTRA_FORCE
        private const val DATA_PERSIST = EXTRA_PERSIST

        private const val FORCE_DEFAULT = false
        private const val PERSIST_DEFAULT = true

        fun enqueueAddress(
            context: Context,
            location: Location,
            persist: Boolean = true,
            force: Boolean = false
        ) {
            val data = Data.Builder()
                .putString(DATA_ACTION, ACTION_ADDRESS)
                .putBoolean(DATA_PERSIST, persist)
                .putBoolean(DATA_FORCE, force)
                .putLocation(DATA_LOCATION, location)

            val workRequest = OneTimeWorkRequest.Builder(AddressWorker::class.java)
                .setInputData(data.build())
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }

        fun enqueueElevation(context: Context, location: Location) {
            val data = Data.Builder()
                .putString(DATA_ACTION, ACTION_ELEVATION)
                .putLocation(DATA_LOCATION, location)

            val workRequest = OneTimeWorkRequest.Builder(AddressWorker::class.java)
                .setInputData(data.build())
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
