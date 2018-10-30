package si.virag.promet.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import dagger.android.AndroidInjection;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import si.virag.promet.MainActivity;
import si.virag.promet.R;
import si.virag.promet.utils.LocaleUtil;
import si.virag.promet.utils.PrometSettings;

public class PushIntentService extends FirebaseMessagingService {

    public static final String DEFAULT_CHANNEL_ID = "default_channel";

    @Inject NotificationStorageModule storage;
    @Inject PrometSettings settings;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        RegisterFcmTokenJob.scheduleGcmUpdate();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name("default.realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        try (Realm realm = Realm.getInstance(configuration)) {

            Map<String, String> data = remoteMessage.getData();
            if (data == null || !data.containsKey("events")) {
                return;
            }

            if (!settings.getShouldReceiveNotifications()) return;

            storage.storeIncomingEvents(realm, data.get("events"));
            storage.filterNotifications(realm);
            showWaitingNotifications(realm);
        } catch (Exception e) {
            Crashlytics.logException(e);
            throw e;
        }
    }

    /**
     * [{"id":177103,"cause":"Delo na cesti","created":1422692083237,"validUntil":1422916200000,"y_wgs":45.92806862270888,"road":"R2-444, Selo - Nova Gorica","x_wgs":13.749834141365874,"causeEn":"Roadworks","roadEn":""},
     *  {"id":177051,"cause":"Zaprta cesta","created":1422619592843,"validUntil":1425073200000,"y_wgs":46.07211621775074,"road":"R3-608, Čepovan - Dolenja Trebuša","x_wgs":13.824534056893757,"causeEn":"Road closure","roadEn":""},
     *  {"id":177032,"cause":"Zaprta cesta","created":1422610908360,"validUntil":1426433400000,"y_wgs":46.03015657207544,"road":"R3-608, Lokve - Čepovan","x_wgs":13.78874899090795,"causeEn":"Road closure","roadEn":""},
     *  {"id":177010,"cause":"Prepoved za tovornjake","created":1422605849870,"validUntil":1422868560000,"y_wgs":46.26182211449827,"road":"R1-225, Stahovica - Črnivec","x_wgs":14.64311920428614,"causeEn":"No freight traffic ","roadEn":""},
     *  {"id":176991,"cause":"Prepoved za tovornjake","created":1422601043593,"validUntil":1422863760000,"y_wgs":45.46412074273359,"road":"G2-106, Fara - Petrina","x_wgs":14.85287071375841,"causeEn":"No freight traffic ","roadEn":""},
     *  {"id":176977,"cause":"Sneg","created":1422597997813,"validUntil":1423263000000,"y_wgs":46.4181384275814,"road":"R1-210, Zg. Jezersko - Sp. Jezersko","x_wgs":14.526675081035254,"causeEn":"Snow","roadEn":""}]
     */


    private void showWaitingNotifications(@NonNull final Realm realm) {
        realm.beginTransaction();
        RealmResults<PushNotification> notifications = realm.where(PushNotification.class).findAll().sort("created");
        if (notifications.size() == 0) return;
        if (notifications.size() == 1) {
            showSingleNotification(notifications.get(0));
        } else {
            showCompoundNotification(notifications);
        }
        realm.cancelTransaction();
    }

    private void showSingleNotification(PushNotification pushNotification) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID);
        notification.setContentTitle(getNotificationCause(pushNotification));
        notification.setContentText(getNotificationRoad(pushNotification));
        notification.setTicker(getNotificationCause(pushNotification) + " - " + getNotificationRoad(pushNotification));
        notification.setWhen(pushNotification.getCreated());
        notification.setShowWhen(true);
        notification.setDefaults(NotificationCompat.DEFAULT_ALL);
        notification.setSmallIcon(R.drawable.ic_car);
        notification.setAutoCancel(true);
        notification.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (getNotificationDescription(pushNotification) != null && getNotificationDescription(pushNotification).length() > 0) {
            NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle(notification);
            style.setBigContentTitle(getNotificationCause(pushNotification));
            style.bigText(getNotificationDescription(pushNotification));
            style.setSummaryText(getNotificationRoad(pushNotification));
            notification.setStyle(style);
        }

        Intent clearIntent = new Intent(this, ClearNotificationsReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, clearIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setDeleteIntent(pi);

        Intent tapIntent = new Intent(this, MainActivity.class);
        tapIntent.putExtra(MainActivity.PARAM_SHOW_LIST, true);
        tapIntent.putExtra(MainActivity.PARAM_SHOW_ITEM_ID, pushNotification.getId());
        tapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent tpi = PendingIntent.getActivity(this, 0, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(tpi);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(0, notification.build());
    }

    private void showCompoundNotification(RealmResults<PushNotification> pushNotifications) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID);

        notification.setContentTitle(getResources().getQuantityString(R.plurals.notifications_multiple_title, pushNotifications.size(), pushNotifications.size()));
        notification.setTicker(getResources().getQuantityString(R.plurals.notifications_multiple_ticker, pushNotifications.size(), pushNotifications.size()));
        notification.setNumber(pushNotifications.size());
        notification.setDefaults(NotificationCompat.DEFAULT_ALL);
        notification.setSmallIcon(R.drawable.ic_car);

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(notification);

        for (int i = 0; i < Math.min(pushNotifications.size(), 5); i++) {
            style.addLine(getNotificationCause(pushNotifications.get(i)) + " - " + getNotificationRoad(pushNotifications.get(i)));

        }

        if (pushNotifications.size() > 5) {
            int value = pushNotifications.size() - 5;
            style.setSummaryText(getResources().getQuantityString(R.plurals.notifications_multiple_summary, value, value));
        }

        Set<String> causes = new HashSet<>();
        for (int i = 0; i < pushNotifications.size(); i++) {
            causes.add(getNotificationCause(pushNotifications.get(i)));
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

        Intent clearIntent = new Intent(this, ClearNotificationsReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, clearIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setDeleteIntent(pi);

        Intent tapIntent = new Intent(this, MainActivity.class);
        tapIntent.putExtra(MainActivity.PARAM_SHOW_LIST, true);
        tapIntent.putExtra(MainActivity.PARAM_SHOW_ITEM_ID, pushNotifications.get(0).getId());
        tapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent tpi = PendingIntent.getActivity(this, 0, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(tpi);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(0, notification.build());
    }


    private String getNotificationCause(PushNotification notification) {
        return LocaleUtil.isSlovenianLocale() ? notification.getCause() : notification.getCauseEn();
    }


    private String getNotificationRoad(PushNotification notification) {
        return LocaleUtil.isSlovenianLocale() || notification.getRoadEn() == null || notification.getRoadEn().length() == 0 ? notification.getRoad() : notification.getRoadEn();
    }

    private String getNotificationDescription(PushNotification notification) {
        return LocaleUtil.isSlovenianLocale() ? notification.getDescription() : notification.getDescrptionEn();
    }
}
