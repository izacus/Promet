package si.virag.promet.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import si.virag.promet.MainActivity;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;

public class PrometPreferences extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PrometApplication application = (PrometApplication) getApplication();
        application.checkUpdateLocale(this);
        super.onCreate(savedInstanceState);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(true);
        bar.setTitle(R.string.app_name);

        setContentView(R.layout.activity_preferences);
        getFragmentManager().beginTransaction().replace(R.id.preferences_content, new PrometPreferencesFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
