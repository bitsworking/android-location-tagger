package com.bitsworking.starlocations;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.SearchView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.bitsworking.starlocations.fragments.InfoFragment;
import com.bitsworking.starlocations.fragments.ListFragment;
import com.bitsworking.starlocations.fragments.MapFragment;
import com.bitsworking.starlocations.fragments.NavigationDrawerFragment;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, Constants {
    private final String TAG = "MainActivity";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Content fragments
     */
    private MapFragment mMapFragment = new MapFragment();
    private ListFragment mListFragment = new ListFragment();
    private InfoFragment mInfoFragment = new InfoFragment();

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private Location mLastKnownLocation;
    private LocationManager mLocationManager;
    private ShareActionProvider mShareActionProvider;

    private int section_attached = 0;
    private Fragment mLastFragment;

    private boolean gps_enabled = false;
    private boolean network_enabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Default section and title
        section_attached = 0;
        mTitle = getString(R.string.title_section0);
//        mTitle = getTitle();

        // Setup Navigation Drawer
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Acquire a reference to the system Location Manager
        mLocationManager = (LocationManager) getSystemService(Activity.LOCATION_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        //exceptions will be thrown if provider is not permitted.
        try { gps_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); } catch (Exception e) {}
        try { network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER); } catch (Exception e) {}

        // If no location enabled, we have a problem (TODO)
//        if(!gps_enabled && !network_enabled) {}

        // Register the listener with the Location Manager to receive location updates
        if (gps_enabled)
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        if (network_enabled)
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        mLocationManager.removeUpdates(locationListener);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Log.v(TAG, "onNavigationDrawerItemSelected: " + position);
        if (position == POS_MAP) {
            showFragment(mMapFragment);
            mTitle = getString(R.string.title_section0);
        } else if (position == POS_LIST) {
            showFragment(mListFragment);
            mTitle = getString(R.string.title_section1);
        } else if (position == POS_INFO) {
            showFragment(mInfoFragment);
            mTitle = getString(R.string.title_section2);
        }

        section_attached = position;
    }


//    protected void showFragment(int resId, Fragment fragment, String tag, String lastTag, boolean addToBackStack ) {
    protected void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (mLastFragment != null) {
            transaction.hide(mLastFragment);
        }

        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.container, fragment);
        }

        mLastFragment = fragment;

        transaction.commit();
    }

    public void restoreActionBar() {
        ActionBar mActionBar = getActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);

            // Setup SearchView
            SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // clicked on the search button, input field expanded
                }
            });
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // Submitted text for search
                    Toast.makeText(getBaseContext(), "Query: " + query, Toast.LENGTH_LONG).show();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });

            // Setup ShareActionView with default intent
            mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.action_share).getActionProvider();
            mShareActionProvider.setShareIntent(Tools.makeLocationSharingIntent("Location Tag", "http://www.bitsworking.com"));

            // Setup Actionbar
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Set the intent for the share-action in the Action Bar
     */
    public void setShareActionIntent(Intent i) {
        mShareActionProvider.setShareIntent(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//
//        if (id == R.id.action_settings) {
//            // Settings
//            return true;
//        }
//        } else if (id == R.id.action_location_current) {
//            // Use current location action bar icon
//            if (section_attached == POS_INFO) {
//                mInfoFragment.useCurrentLocation(getLocation());
//            }
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            if (Tools.isBetterLocation(location, mLastKnownLocation)) {
                // Have a better, newer location!
                Log.v(TAG, "new good location: " + location.toString());
                makeUseOfNewLocation(location);
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    public Location getLocation() {
        if (mLastKnownLocation == null) {
            return mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else {
            return mLastKnownLocation;
        }
    }

    private void makeUseOfNewLocation(Location location) {
        // Update ui and code on new location
        mLastKnownLocation = location;

        // Let fragments know about better location
        if (section_attached == POS_MAP) {
            mMapFragment.newLastKnownLocation(location);
        } else if (section_attached == POS_LIST) {
//            Toast.makeText(this, getLocation().toString(), Toast.LENGTH_SHORT).show();
        } else if (section_attached == POS_INFO) {
            mInfoFragment.newLastKnownLocation(location);
        }
    }
}
