package com.bitsworking.mylocations;

import android.app.Activity;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import com.bitsworking.mylocations.fragments.InfoFragment;
import com.bitsworking.mylocations.fragments.ListFragment;
import com.bitsworking.mylocations.fragments.MapFragment;
import com.bitsworking.mylocations.fragments.NavigationDrawerFragment;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private final static int POS_MAP = 0;
    private final static int POS_LIST = 1;
    private final static int POS_INFO = 2;

    private MapFragment mMapFragment = new MapFragment();
    private ListFragment mListFragment = new ListFragment();
    private InfoFragment mInfoFragment = new InfoFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        if (position == POS_MAP) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, mMapFragment)
                    .commit();
            mTitle = getString(R.string.title_section1);
        } else if (position == POS_LIST) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, mListFragment)
                    .commit();
            mTitle = getString(R.string.title_section2);
        } else if (position == POS_INFO) {
            mTitle = getString(R.string.title_section3);
            fragmentManager.beginTransaction()
                    .replace(R.id.container, mInfoFragment)
                    .commit();
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
