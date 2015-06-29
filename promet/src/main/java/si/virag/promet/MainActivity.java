package si.virag.promet;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.astuetz.PagerSlidingTabStrip;
import com.crashlytics.android.Crashlytics;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import si.virag.promet.fragments.EventListFragment;
import si.virag.promet.fragments.MapFragment;
import si.virag.promet.gcm.ClearNotificationsService;
import si.virag.promet.gcm.RegistrationService;
import si.virag.promet.preferences.PrometPreferences;
import si.virag.promet.utils.PrometSettings;

public class MainActivity extends ActionBarActivity
{
    public static final String PARAM_SHOW_LIST = "ShowList";
    public static final String PARAM_SHOW_ITEM_ID = "ShowListItemId";

    private ViewPager pager;
    private PagerSlidingTabStrip tabs;
    private SystemBarTintManager tintManager;

    @Inject PrometSettings prometSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Transluscent navigation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        PrometApplication application = (PrometApplication) getApplication();
        application.checkUpdateLocale(this);

        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        application.component().inject(this);

        Crashlytics.start(this);
        setContentView(R.layout.activity_main);
        // Fix actionbar name for other locales
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setTitle(R.string.app_name);

        // Set titlebar tint
        tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintColor(getResources().getColor(R.color.theme_color));

        pager = (ViewPager)findViewById(R.id.main_pager);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.main_tabs);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabs.getLayoutParams();
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                params.setMargins(0, tintManager.getConfig().getPixelInsetTop(true), tintManager.getConfig().getPixelInsetRight(), 0);
            }
            else {
                params.setMargins(0, tintManager.getConfig().getPixelInsetTop(true), 0, 0);
            }
            tabs.setLayoutParams(params);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_car);
            ActivityManager.TaskDescription description = new ActivityManager.TaskDescription(getString(R.string.app_name), icon, getResources().getColor(R.color.theme_color));
            setTaskDescription(description);
        }

        setupPages(getIntent().getBooleanExtra(PARAM_SHOW_LIST, false));
        if (getIntent().hasExtra(PARAM_SHOW_ITEM_ID)) {
            EventBus.getDefault().postSticky(new Events.ShowEventInList(getIntent().getLongExtra(PARAM_SHOW_ITEM_ID, 0)));
        }

        clearPendingNotifications();
        checkShowNotificationsDialog();
    }

    private void clearPendingNotifications() {
        Intent i = new Intent(this, ClearNotificationsService.class);
        startService(i);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(PARAM_SHOW_LIST, false)) {
            pager.setCurrentItem(1, true);
        }

        if (intent.hasExtra(PARAM_SHOW_ITEM_ID)) {
            EventBus.getDefault().postSticky(new Events.ShowEventInList(intent.getLongExtra(PARAM_SHOW_ITEM_ID, 0)));
        }

        clearPendingNotifications();
    }

    private void checkShowNotificationsDialog() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.contains(PrometSettings.PREF_NOTIFICATIONS)) {
            new MaterialDialog.Builder(this)
                              .title(getString(R.string.alert_notifications_title))
                              .content(getString(R.string.alert_notifications_content))
                              .positiveText(getString(R.string.alert_notifications_yes))
                              .negativeText(getString(R.string.altert_notifications_no))
                              .callback(new MaterialDialog.ButtonCallback() {
                                  @Override
                                  public void onPositive(MaterialDialog dialog) {
                                      prefs.edit().putBoolean(PrometSettings.PREF_NOTIFICATIONS, true)
                                                  .putBoolean(RegistrationService.PREF_SHOULD_UPDATE_GCM_REGISTRATION, true).apply();
                                      prometSettings.reload();

                                      Intent registerIntent = new Intent(MainActivity.this, RegistrationService.class);
                                      startService(registerIntent);

                                      showPreferences();
                                  }

                                  @Override
                                  public void onNegative(MaterialDialog dialog) {
                                      prefs.edit().putBoolean(PrometSettings.PREF_NOTIFICATIONS, false).apply();
                                      prometSettings.reload();
                                  }
                              })
                              .show();
        }
    }

    private void setupPages(boolean showList) {
        pager.setAdapter(new MainPagesAdapter(getResources(), getSupportFragmentManager()));
        tabs.setShouldExpand(true);
        tabs.setViewPager(pager);

        if (showList) {
            pager.setCurrentItem(1);
        }
    }

    private void showPreferences() {
        Intent preferenceIntent = new Intent(this, PrometPreferences.class);
        startActivity(preferenceIntent);
    }

    private static class MainPagesAdapter extends FragmentPagerAdapter {

        private final Resources res;

        public MainPagesAdapter(Resources res, FragmentManager fm) {
            super(fm);
            this.res = res;
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;

            switch (i) {
                case 0:
                    fragment = new MapFragment();
                    break;
                case 1:
                    fragment = new EventListFragment();
                    break;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position)
            {
                case 0:
                    return res.getString(R.string.tab_map);
                case 1:
                    return res.getString(R.string.tab_list);

            }

            return super.getPageTitle(position);
        }
    }

    public SystemBarTintManager getTintManager() {
        return tintManager;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map, menu);

        menu.findItem(R.id.menu_map_avtoceste).setChecked(prometSettings.getShowAvtoceste());
        menu.findItem(R.id.menu_map_crossings).setChecked(prometSettings.getShowBorderCrossings());
        menu.findItem(R.id.menu_map_lokalne_ceste).setChecked(prometSettings.getShowLokalneCeste());
        menu.findItem(R.id.menu_map_regionalne_ceste).setChecked(prometSettings.getShowRegionalneCeste());
        menu.findItem(R.id.menu_map_dela_na_cesti).setChecked(prometSettings.getShowRoadworks());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_settings) {
            showPreferences();
            return true;
        }

        if (!item.isCheckable())
            return false;

        boolean enabled = !item.isChecked();
        item.setChecked(enabled);

        switch (item.getItemId()) {
            case R.id.menu_map_avtoceste:
                prometSettings.setShowAvtoceste(enabled);
                break;

            case R.id.menu_map_crossings:
                prometSettings.setShowBorderCrossings(enabled);
                break;

            case R.id.menu_map_regionalne_ceste:
                prometSettings.setShowRegionalneCeste(enabled);
                break;

            case R.id.menu_map_lokalne_ceste:
                prometSettings.setShowLokalneCeste(enabled);
                break;

            case R.id.menu_map_dela_na_cesti:
                prometSettings.setShowRoadworks(enabled);
                break;

            default:
                return false;
        }

        EventBus.getDefault().post(new Events.UpdateMap());
        EventBus.getDefault().post(new Events.UpdateEventList());
        return true;
    }

    public void onEventMainThread(Events.RefreshStarted e) {
        setSupportProgressBarIndeterminateVisibility(true);
    }

    public void onEventMainThread(Events.RefreshCompleted e) {
        setSupportProgressBarIndeterminateVisibility(false);
    }

    public void onEventMainThread(Events.ShowPointOnMap e) {
        pager.setCurrentItem(0, true);
    }

    public void onEventMainThread(Events.ShowEventInList e) {
        pager.setCurrentItem(1, true);
    }
}
