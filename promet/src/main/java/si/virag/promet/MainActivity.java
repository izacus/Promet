package si.virag.promet;

import android.app.Activity;
import android.os.Bundle;
import si.virag.promet.fragments.MapFragment;

public class MainActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null)
        {
            getFragmentManager().beginTransaction().replace(R.id.main_container, new MapFragment()).commit();
        }
    }
}
