package si.virag.promet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;
import com.astuetz.PagerSlidingTabStrip;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import si.virag.promet.fragments.EventListFragment;
import si.virag.promet.fragments.MapFragment;

public class MainActivity extends FragmentActivity
{
    private ViewPager pager;
    private PagerSlidingTabStrip tabs;
    private SystemBarTintManager tintManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintColor(getResources().getColor(R.color.theme_color));

        pager = (ViewPager)findViewById(R.id.main_pager);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.main_tabs);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabs.getLayoutParams();
        params.setMargins(0, tintManager.getConfig().getPixelInsetTop(true), 0, 0);
        tabs.setLayoutParams(params);
        setupPages();
    }

    private void setupPages() {
        pager.setAdapter(new MainPagesAdapter(getSupportFragmentManager()));
        tabs.setShouldExpand(true);
        tabs.setViewPager(pager);
    }

    private static class MainPagesAdapter extends FragmentPagerAdapter {

        public MainPagesAdapter(FragmentManager fm) {
            super(fm);
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
                    return "Zemljevid";
                case 1:
                    return "Seznam";

            }

            return super.getPageTitle(position);
        }
    }

    public SystemBarTintManager getTintManager() {
        return tintManager;
    }
}
