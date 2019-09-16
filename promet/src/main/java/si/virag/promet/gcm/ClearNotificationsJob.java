package si.virag.promet.gcm;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public final class ClearNotificationsJob extends Worker {

    public static void schedule(Context context) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ClearNotificationsJob.class)
                .build();
        WorkManager.getInstance(context).enqueue(request);
    }

    public ClearNotificationsJob(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name("default.realm")
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm realm = Realm.getInstance(configuration);
        realm.beginTransaction();
        realm.where(PushNotification.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
        realm.close();
        return Result.success();
    }
}
