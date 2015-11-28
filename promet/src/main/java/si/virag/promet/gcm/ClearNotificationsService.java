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
        RealmConfiguration configuration = new RealmConfiguration.Builder(this)
                .name("default.realm")
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm realm = Realm.getInstance(configuration);
        realm.beginTransaction();
        realm.allObjects(PushNotification.class).clear();
        realm.commitTransaction();
        realm.close();
    }
}
