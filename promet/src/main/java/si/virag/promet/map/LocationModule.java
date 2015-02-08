package si.virag.promet.map;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;

@Module
@Singleton
public class LocationModule implements GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final String LOG_TAG = "Promet.Location";
    private static final int CURRENT_LOCATION_MAX_AGE_MS = 10 * 60 * 1000;

    @NonNull
    private final GoogleApiClient googleApiClient;

    @Nullable
    private Location currentLocation;

    private ConditionVariable waitForTimeout = new ConditionVariable(false);

    public LocationModule(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                              .addConnectionCallbacks(this)
                              .addApi(LocationServices.API)
                              .build();
    }

    public synchronized Location getLocationWithTimeout(int timeoutMs) {
        if (!googleApiClient.isConnected())
            googleApiClient.blockingConnect(timeoutMs, TimeUnit.MILLISECONDS);

        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentLocation != null && timeDifference(currentLocation.getTime(), currentTime) < CURRENT_LOCATION_MAX_AGE_MS) {
            return currentLocation;
        }

        waitForTimeout.close();
        LocationRequest lr = LocationRequest.create();
        lr.setNumUpdates(1);
        lr.setExpirationDuration(1000);
        lr.setPriority(LocationRequest.PRIORITY_NO_POWER);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, lr, this);
        waitForTimeout.block(timeoutMs);

        if (currentLocation == null || timeDifference(currentLocation.getTime(), currentTime) > CURRENT_LOCATION_MAX_AGE_MS) {
            return null;
        }

        return currentLocation;
    }

    @Override
    public void onConnected(Bundle bundle) {
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        Log.d(LOG_TAG, "Current location: " + currentLocation);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private static long timeDifference(long previous, long now) {
        return now - previous;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "Location update received: " + location);
        currentLocation = location;
        waitForTimeout.open();
    }
}
