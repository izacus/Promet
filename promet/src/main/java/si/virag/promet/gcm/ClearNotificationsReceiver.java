package si.virag.promet.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ClearNotificationsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ClearNotificationsJob.schedule();
    }
}
