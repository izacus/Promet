package si.virag.promet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.afollestad.materialdialogs.MaterialDialog;
import com.franmontiel.localechanger.LocaleChanger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import rx.Subscriber;
import si.virag.promet.fragments.CamerasFragment;
import si.virag.promet.fragments.EventListFragment;
import si.virag.promet.fragments.MapFragment;
import si.virag.promet.gcm.ClearNotificationsJob;
import si.virag.promet.gcm.RegisterFcmTokenJob;
import si.virag.promet.preferences.PrometPreferences;
import si.virag.promet.utils.ActivityUtilities;
import si.virag.promet.utils.PrometSettings;

public class MainActivity extends AppCompatActivity
{
    public static final String PARAM_SHOW_LIST = "ShowList";
    public static final String PARAM_SHOW_ITEM_ID = "ShowListItemId";

    @NonNull
    private Toolbar toolbar;
    @NonNull
    private ViewPager pager;
    @NonNull
    private TabLayout tabs;
    @NonNull
    private SystemBarTintManager tintManager;

    @Inject PrometSettings prometSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityUtilities.setupTransluscentNavigation(this);
        PrometApplication application = (PrometApplication) getApplication();

        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        application.component().inject(this);

        setContentView(R.layout.activity_main);
        // Fix actionbar name for other locales
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        tintManager = ActivityUtilities.setupSystembarTint(this, toolbar);
        pager = (ViewPager)findViewById(R.id.main_pager);
        tabs = (TabLayout) findViewById(R.id.main_tabs);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_car);
            ActivityManager.TaskDescription description = new ActivityManager.TaskDescription(getString(R.string.app_name), icon, ContextCompat.getColor(this,R.color.theme_color));
            setTaskDescription(description);
        }

        setupPages(getIntent().getBooleanExtra(PARAM_SHOW_LIST, false));
        if (getIntent().hasExtra(PARAM_SHOW_ITEM_ID)) {
            EventBus.getDefault().postSticky(new Events.ShowEventInList(getIntent().getLongExtra(PARAM_SHOW_ITEM_ID, 0)));
        }

        clearPendingNotifications();
    }



    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleChanger.configureBaseContext(newBase));
    }

    private void clearPendingNotifications() {
        ClearNotificationsJob.schedule();
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
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            return;
        }

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
                                                  .putBoolean(RegisterFcmTokenJob.PREF_SHOULD_UPDATE_GCM_REGISTRATION, true).apply();
                                      prometSettings.reload();

                                      RegisterFcmTokenJob.scheduleGcmUpdate();
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
        tabs.setupWithViewPager(pager);

        if (showList) {
            pager.setCurrentItem(1);
        }
    }

    private void showPreferences() {
        Intent preferenceIntent = new Intent(this, PrometPreferences.class);
        startActivity(preferenceIntent);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
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
                case 2:
                    fragment = new CamerasFragment();
                    break;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position)
            {
                case 0:
                    return res.getString(R.string.tab_map);
                case 1:
                    return res.getString(R.string.tab_list);
                case 2:
                    return res.getString(R.string.tab_cameras);
            }

            return super.getPageTitle(position);
        }
    }

    public SystemBarTintManager getTintManager() {
        return tintManager;
    }

    @Override
    @SuppressLint("CheckResult")
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(Boolean aBoolean) {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onComplete() {
                        checkShowNotificationsDialog();
                    }
                });

        updateHeaderView();
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
        menu.findItem(R.id.menu_map_cameras).setChecked(prometSettings.getShowCameras());
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

            case R.id.menu_map_cameras:
                prometSettings.setShowCameras(enabled);
                break;

            default:
                return false;
        }

        EventBus.getDefault().post(new Events.UpdateMap());
        EventBus.getDefault().post(new Events.UpdateEventList());
        return true;
    }

    private void showUpdateTimeInActionBar(@NonNull final LocalDateTime updateTime) {
        final ActionBar ab = getSupportActionBar();
        if (ab == null) return;

        // Check if today
        if (updateTime.toLocalDate().equals(LocalDate.now())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            ab.setSubtitle("Podatki z " + formatter.format(updateTime) + ".");
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd. MM. HH:mm");
            ab.setSubtitle("Podatki z " + formatter.format(updateTime) + ".");
        }
    }

    private void updateHeaderView() {
        if (prometSettings.getShowAvtoceste() &&
                prometSettings.getShowLokalneCeste() &&
                prometSettings.getShowBorderCrossings() &&
                prometSettings.getShowRegionalneCeste()) {

            toolbar.setSubtitle(null);
            return;
        }

        String check = "\u2713";
        String cross = "\u2717";

        String text = getString(R.string.list_hint,
                prometSettings.getShowAvtoceste() ? check : cross,
                prometSettings.getShowBorderCrossings() ? check : cross,
                prometSettings.getShowRegionalneCeste() ? check : cross,
                prometSettings.getShowLokalneCeste() ? check : cross);
        toolbar.setSubtitle(text);
    }

    public void onEventMainThread(Events.RefreshStarted e) {
        setSupportProgressBarIndeterminateVisibility(true);
    }

    public void onEventMainThread(Events.RefreshCompleted e) {
        setSupportProgressBarIndeterminateVisibility(false);
        if (e.lastUpdateTime != null) {
            showUpdateTimeInActionBar(e.lastUpdateTime);
        }
    }

    public void onEventMainThread(Events.ShowPointOnMap e) {
        pager.setCurrentItem(0, true);
    }

    public void onEventMainThread(Events.ShowEventInList e) {
        pager.setCurrentItem(1, true);
    }

    public void onEventMainThread(Events.UpdateActivityHeader e) { updateHeaderView(); }
}
