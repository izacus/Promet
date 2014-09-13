package si.virag.promet.preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import si.virag.promet.MainActivity;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;

public class PrometPreferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ListPreference langPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.app_name); // This is wrong if language was changed, so force change :)
        addPreferencesFromResource(R.xml.preferences);

        langPreference = (ListPreference) findPreference("app_lang");
        langPreference.setSummary(langPreference.getEntry());
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equalsIgnoreCase("app_lang")) {
            langPreference.setSummary(langPreference.getEntry());
            ((PrometApplication)getApplication()).checkUpdateLocale(this);
            finish();
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
