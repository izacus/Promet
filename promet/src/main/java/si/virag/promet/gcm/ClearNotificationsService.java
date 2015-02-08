package si.virag.promet.gcm;

import android.app.IntentService;
import android.content.Intent;

import io.realm.Realm;

public class ClearNotificationsService extends IntentService {

    public ClearNotificationsService() {
        super("Clear notifications service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Realm realm = Realm.getInstance(this);
        realm.beginTransaction();
        realm.allObjects(PushNotification.class).clear();
        realm.commitTransaction();
        realm.close();
    }
}
