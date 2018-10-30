package si.virag.promet.gcm;

import android.location.Location;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import io.realm.Realm;
import io.realm.RealmResults;
import si.virag.promet.api.model.EventGroup;
import si.virag.promet.map.LocationModule;
import si.virag.promet.utils.DataUtils;
import si.virag.promet.utils.PrometSettings;

@Singleton
public class NotificationStorageModule {
    private static final String LOG_TAG = "Promet.GCM.Storage";

    private static final int HIGHWAY_NOTIFICATION_DISTANCE = 200000;
    private static final int REGIONAL_NOTIFICATION_DISTANCE = 30000;
    private static final int LOCAL_NOTIFICATION_DISTANCE = 25000;

    @Inject LocationModule location;
    @Inject PrometSettings settings;

    @Inject
    public NotificationStorageModule() {
    }

    void storeIncomingEvents(@NonNull final Realm realm, @NonNull final String events) {
        try {
            JSONArray eventArray = new JSONArray(events);

            realm.beginTransaction();
            for (int i = 0; i < eventArray.length(); i++) {
                JSONObject event = eventArray.getJSONObject(i);
                long id = event.getLong("id");

                // Check for existing notification
                // TODO TODO TODO TODO
                /* PushNotification notifs = realm.where(PushNotification.class).equalTo("id", id).findFirst();
                if (notifs != null) continue; */

                PushNotification notification = new PushNotification(id,
                                                                    event.getString("cause"),
                                                                    event.getString("causeEn"),
                                                                    event.getString("road"),
                                                                    event.getString("roadEn"),
                                                                    event.optString("description", ""),
                                                                    event.optString("descriptionEn", ""),
                                                                    event.getInt("roadPriority"),
                                                                    event.getBoolean("isBorderCrossing"),
                                                                    event.getLong("created"),
                                                                    event.getLong("validUntil"),
                                                                    event.getDouble("y_wgs"),
                                                                    event.getDouble("x_wgs"));
                realm.copyToRealm(notification);
            }

            realm.commitTransaction();
        } catch (JSONException e) {
            Crashlytics.logException(e);
        }
    }

    /**
     * Filters stored notifications according to valid time and distance
     */
    public void filterNotifications(@NonNull final Realm realm) {
        final long currentTime = Calendar.getInstance().getTimeInMillis();

        realm.beginTransaction();
        RealmResults<PushNotification> notifications = realm.where(PushNotification.class).findAll();
        for (int i = 0; i < notifications.size(); i++) {
            PushNotification notification = notifications.get(i);
            if (notification.getValidUntil() < currentTime ||
                !settings.shouldShowNotification(DataUtils.roadPriorityToRoadType(notification.getRoadPriority(), notification.isCrossing())) ||
                "Roadworks".equalsIgnoreCase(notification.getCauseEn())) {
                Log.d(LOG_TAG, "Clearing expired/disabled notification " + notification);
                notification.deleteFromRealm();
            }
        }

        if (realm.where(PushNotification.class).count() > 0) {
            Location currentLocation = location.getLocationWithTimeout(1000);
            if (currentLocation != null) {
                filterNotificationsByDistance(realm, currentLocation);
            }
        }

        realm.commitTransaction();
    }

    private static void filterNotificationsByDistance(@NonNull final Realm realm, @NonNull final Location location) {
        final float[] result = new float[1];

        RealmResults<PushNotification> notifications = realm.where(PushNotification.class).findAll();
        for (int i = 0; i < notifications.size(); i++) {
            PushNotification notification = notifications.get(i);
            Location.distanceBetween(location.getLatitude(), location.getLongitude(), notification.getLat(), notification.getLng(), result);
            Log.d(LOG_TAG, notification.getId() + " - Distance from current location: " + result[0]);

            EventGroup type = DataUtils.roadPriorityToRoadType(notification.getRoadPriority(), notification.isCrossing());
            int maxDistance = Integer.MAX_VALUE;
            switch (type) {
                case MEJNI_PREHOD:
                case AVTOCESTA:
                case HITRA_CESTA:
                    maxDistance = HIGHWAY_NOTIFICATION_DISTANCE;
                    break;
                case REGIONALNA_CESTA:
                    maxDistance = REGIONAL_NOTIFICATION_DISTANCE;
                    break;
                case LOKALNA_CESTA:
                    maxDistance = LOCAL_NOTIFICATION_DISTANCE;
                    break;
            }

            if (result[0] > maxDistance) {
                Log.d(LOG_TAG, "Filtering too far event " + result[0] + "/" + maxDistance + "[" + notification + "]");
                notification.deleteFromRealm();
            }
        }
    }

}