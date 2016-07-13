package si.virag.promet.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import si.virag.promet.MainActivity;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;

public class PrometPreferences extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PrometApplication application = (PrometApplication) getApplication();
        application.checkUpdateLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // Fix actionbar name for other locales
        toolbar = (Toolbar) findViewById(R.id.preferences_toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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
