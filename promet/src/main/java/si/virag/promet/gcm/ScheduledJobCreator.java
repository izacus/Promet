package si.virag.promet.gcm;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class ScheduledJobCreator implements JobCreator {

    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case RegisterFcmTokenJob.TAG:
                return new RegisterFcmTokenJob();
            case ClearNotificationsJob.TAG:
                return new ClearNotificationsJob();
            default:
                return null;
        }
    }
}
