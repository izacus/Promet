package si.virag.promet;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.astuetz.PagerSlidingTabStrip;
import si.virag.promet.fragments.EventListFragment;
import si.virag.promet.fragments.MapFragment;

public class MainActivity extends FragmentActivity
{
    private ViewPager pager;
    private PagerSlidingTabStrip tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pager = (ViewPager)findViewById(R.id.main_pager);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.main_tabs);
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
}
