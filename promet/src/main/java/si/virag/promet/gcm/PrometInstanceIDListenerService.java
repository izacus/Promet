package si.virag.promet.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

public class PrometInstanceIDListenerService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        Intent registrationIntent = new Intent(this, RegistrationService.class);
        startService(registrationIntent);
    }
}
