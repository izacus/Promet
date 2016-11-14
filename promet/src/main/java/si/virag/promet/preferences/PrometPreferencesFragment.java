package si.virag.promet.preferences;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;

import javax.inject.Inject;

import si.virag.promet.MainActivity;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;
import si.virag.promet.gcm.RegistrationService;
import si.virag.promet.utils.PrometSettings;

public class PrometPreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject PrometSettings settings;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addPreferencesFromResource(R.xml.preferences_lollipop);
        } else {
            addPreferencesFromResource(R.xml.preferences);
        }
        PrometApplication application = (PrometApplication) getActivity().getApplication();
        application.component().inject(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Activity activity = getActivity();
        if (activity == null) return;

        if (key.equalsIgnoreCase("gcm_enabled")) {
            checkSetEnabledNotificationPreferences();
            sharedPreferences.edit().putBoolean(RegistrationService.PREF_SHOULD_UPDATE_GCM_REGISTRATION, true).apply();
        }

        settings.reload();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkSetEnabledNotificationPreferences();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Activity activity = getActivity();
        if (activity == null) return;

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        Intent i = new Intent(activity, RegistrationService.class);
        activity.startService(i);
    }

    private void checkSetEnabledNotificationPreferences() {
        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
        boolean enable = preferences.getBoolean(PrometSettings.PREF_NOTIFICATIONS, false);

        findPreference(PrometSettings.PREF_NOTIFICATIONS_CROSSINGS).setEnabled(enable);
        findPreference(PrometSettings.PREF_NOTIFICATIONS_HIGHWAYS).setEnabled(enable);
        findPreference(PrometSettings.PREF_NOTIFICATIONS_REGIONAL).setEnabled(enable);
        findPreference(PrometSettings.PREF_NOTIFICATIONS_LOCAL).setEnabled(enable);
    }
}
