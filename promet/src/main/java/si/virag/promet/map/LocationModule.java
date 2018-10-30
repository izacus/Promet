package si.virag.promet.map;

import android.content.Context;
import android.location.Location;
import android.os.ConditionVariable;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.Nullable;

@Singleton
public class LocationModule {

    private static final String LOG_TAG = "Promet.Location";
    private static final int CURRENT_LOCATION_MAX_AGE_MS = 10 * 60 * 1000;

    private final Context context;

    @Nullable
    private Location currentLocation;

    private final ConditionVariable waitForTimeout = new ConditionVariable(false);

    @Inject
    LocationModule(Context context) {
        this.context = context;

        try {
            LocationServices.getFusedLocationProviderClient(context).getLastLocation().addOnSuccessListener(location -> LocationModule.this.currentLocation = location);
        } catch (SecurityException e) {
            // User didn't allow location access.
        }
    }

    @Nullable
    public synchronized Location getLocationWithTimeout(int timeoutMs) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentLocation != null && timeDifference(currentLocation.getTime(), currentTime) < CURRENT_LOCATION_MAX_AGE_MS) {
            return currentLocation;
        }

        waitForTimeout.close();
        LocationRequest lr = LocationRequest.create();
        lr.setNumUpdates(1);
        lr.setExpirationDuration(timeoutMs);
        lr.setPriority(LocationRequest.PRIORITY_NO_POWER);

        try {
            FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(context);
            locationClient.requestLocationUpdates(lr, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Log.d(LOG_TAG, "Location update received: " + locationResult.getLastLocation());
                    currentLocation = locationResult.getLastLocation();
                    waitForTimeout.open();
                }
            }, Looper.getMainLooper());
            waitForTimeout.block(timeoutMs);

            if (currentLocation == null || timeDifference(currentLocation.getTime(), currentTime) > CURRENT_LOCATION_MAX_AGE_MS) {
                return null;
            }

            return currentLocation;
        } catch (SecurityException e) {
            return null;
        }
    }

    private static long timeDifference(long previous, long now) {
        return now - previous;
    }

}
