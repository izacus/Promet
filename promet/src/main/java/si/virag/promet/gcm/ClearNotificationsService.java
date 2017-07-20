package si.virag.promet.gcm;

import android.app.IntentService;
import android.content.Intent;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class ClearNotificationsService extends IntentService {

    public ClearNotificationsService() {
        super("Clear notifications service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name("default.realm")
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm realm = Realm.getInstance(configuration);
        realm.beginTransaction();
        realm.where(PushNotification.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
        realm.close();
    }
}
