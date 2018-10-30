package si.virag.promet.gcm;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import org.threeten.bp.Duration;

import androidx.annotation.NonNull;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class ClearNotificationsJob extends Job {

    public static final String TAG = "clear_notifications_job";

    public static final void schedule() {
        new JobRequest.Builder(TAG)
                .setUpdateCurrent(true)
                .setExecutionWindow(1, Duration.ofHours(1).toMillis())
                .build()
                .scheduleAsync();
    }


    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name("default.realm")
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm realm = Realm.getInstance(configuration);
        realm.beginTransaction();
        realm.where(PushNotification.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
        realm.close();
        return Result.SUCCESS;
    }
}
