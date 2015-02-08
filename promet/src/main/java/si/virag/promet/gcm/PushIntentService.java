package si.virag.promet.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;
import si.virag.promet.MainActivity;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;
import si.virag.promet.api.model.RoadType;
import si.virag.promet.utils.DataUtils;

public class PushIntentService extends IntentService {
    private static final String LOG_TAG = "Promet.GCM.Receive";

    @Inject NotificationStorageModule storage;

    @Nullable
    private Location currentLocation;

    public PushIntentService() {
        super("Push receiver");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PrometApplication app = (PrometApplication) getApplication();
        app.component().inject(this);
    }

    /**
     * [{"id":177103,"cause":"Delo na cesti","created":1422692083237,"validUntil":1422916200000,"y_wgs":45.92806862270888,"road":"R2-444, Selo - Nova Gorica","x_wgs":13.749834141365874,"causeEn":"Roadworks","roadEn":""},
     *  {"id":177051,"cause":"Zaprta cesta","created":1422619592843,"validUntil":1425073200000,"y_wgs":46.07211621775074,"road":"R3-608, Čepovan - Dolenja Trebuša","x_wgs":13.824534056893757,"causeEn":"Road closure","roadEn":""},
     *  {"id":177032,"cause":"Zaprta cesta","created":1422610908360,"validUntil":1426433400000,"y_wgs":46.03015657207544,"road":"R3-608, Lokve - Čepovan","x_wgs":13.78874899090795,"causeEn":"Road closure","roadEn":""},
     *  {"id":177010,"cause":"Prepoved za tovornjake","created":1422605849870,"validUntil":1422868560000,"y_wgs":46.26182211449827,"road":"R1-225, Stahovica - Črnivec","x_wgs":14.64311920428614,"causeEn":"No freight traffic ","roadEn":""},
     *  {"id":176991,"cause":"Prepoved za tovornjake","created":1422601043593,"validUntil":1422863760000,"y_wgs":45.46412074273359,"road":"G2-106, Fara - Petrina","x_wgs":14.85287071375841,"causeEn":"No freight traffic ","roadEn":""},
     *  {"id":176977,"cause":"Sneg","created":1422597997813,"validUntil":1423263000000,"y_wgs":46.4181384275814,"road":"R1-210, Zg. Jezersko - Sp. Jezersko","x_wgs":14.526675081035254,"causeEn":"Snow","roadEn":""}]
     */

    @Override
    protected void onHandleIntent(Intent intent) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        Bundle extras = intent.getExtras();
        if (!GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType) ||
            extras == null ||
            !extras.containsKey("events")) {
            PushBroadcastReceiver.completeWakefulIntent(intent);
            return;
        }

        storage.storeIncomingEvents(this, extras.getString("events"));
        storage.filterNotifications(this);
        showWaitingNotifications();

        PushBroadcastReceiver.completeWakefulIntent(intent);
}

    private void showWaitingNotifications() {
        Realm realm = Realm.getInstance(this);
        realm.beginTransaction();
        RealmResults<PushNotification> notifications = realm.allObjectsSorted(PushNotification.class, "created", false);
        if (notifications.size() == 0) return;
        if (notifications.size() == 1) {
            showSingleNotification(notifications.get(0));
        } else {
            showCompoundNotification(notifications);
        }
        realm.commitTransaction();
        realm.close();
    }

    private void showSingleNotification(PushNotification pushNotification) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
        notification.setContentTitle(pushNotification.getCause());
        notification.setContentText(pushNotification.getRoad());
        notification.setTicker(pushNotification.getCause() + " - " + pushNotification.getRoad());
        notification.setWhen(pushNotification.getCreated());
        notification.setShowWhen(true);
        notification.setDefaults(NotificationCompat.DEFAULT_ALL);
        notification.setSmallIcon(R.drawable.ic_car);
        notification.setAutoCancel(true);
        notification.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (pushNotification.getDescription() != null && pushNotification.getDescription().length() > 0) {
            NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle(notification);
            style.setBigContentTitle(pushNotification.getCause());
            style.bigText(pushNotification.getDescription());
            style.setSummaryText(pushNotification.getRoad());
            notification.setStyle(style);
        }

        Intent clearIntent = new Intent(this, ClearNotificationsService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, clearIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setDeleteIntent(pi);

        Intent tapIntent = new Intent(this, MainActivity.class);
        tapIntent.putExtra(MainActivity.PARAM_SHOW_LIST, true);
        PendingIntent tpi = PendingIntent.getActivity(this, 0, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(tpi);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(0, notification.build());
    }

    private void showCompoundNotification(RealmResults<PushNotification> pushNotifications) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
        notification.setContentTitle(pushNotifications.size() + " novih dogodkov");
        notification.setTicker(pushNotifications.size() + " novih prometnih dogodkov");
        notification.setNumber(pushNotifications.size());
        notification.setDefaults(NotificationCompat.DEFAULT_ALL);
        notification.setSmallIcon(R.drawable.ic_car);

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(notification);

        for (int i = 0; i < Math.min(pushNotifications.size(), 5); i++) {
            style.addLine(pushNotifications.get(i).getCause() + " - " + pushNotifications.get(i).getRoad());

        }

        if (pushNotifications.size() > 5) {
            style.setSummaryText(" ... še " + String.valueOf(pushNotifications.size() - 5) + " novih.");
        }

        Set<String> causes = new HashSet<>();
        for (int i = 0; i < pushNotifications.size(); i++) {
            causes.add(pushNotifications.get(i).getCause());
        }

        StringBuilder contentText = new StringBuilder();
        for (String cause : causes) {
            contentText.append(cause);
            contentText.append(", ");
        }
        contentText.delete(contentText.length() - 2, contentText.length());
        notification.setContentText(contentText.toString());

        notification.setStyle(style);
        notification.setShowWhen(true);
        notification.setWhen(pushNotifications.get(0).getCreated());
        notification.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notification.setAutoCancel(true);

        Intent clearIntent = new Intent(this, ClearNotificationsService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, clearIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setDeleteIntent(pi);

        Intent tapIntent = new Intent(this, MainActivity.class);
        tapIntent.putExtra(MainActivity.PARAM_SHOW_LIST, true);
        PendingIntent tpi = PendingIntent.getActivity(this, 0, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(tpi);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(0, notification.build());
    }
}
