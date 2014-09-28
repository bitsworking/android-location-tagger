package com.bitsworking.starlocations;

import android.app.Activity;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = getTitle();

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

        // Register the listener with the Location Manager to receive location updates
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        mLocationManager.removeUpdates(locationListener);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        if (position == POS_MAP) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, mMapFragment)
                    .commit();
        } else if (position == POS_LIST) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, mListFragment)
                    .commit();
        } else if (position == POS_INFO) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, mInfoFragment)
                    .commit();
        }
    }

    public void onSectionAttached(int number) {
        // callback when a fragment section has been attached
        switch (number) {
            case POS_MAP:
                mTitle = getString(R.string.title_section1);
                break;
            case POS_LIST:
                mTitle = getString(R.string.title_section2);
                break;
            case POS_INFO:
                mTitle = getString(R.string.title_section3);
                break;
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
        } else if (item.getItemId() == R.id.action_location_current) {
            Toast.makeText(this, getLocation().toString(), Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            if (Tools.isBetterLocation(location, mLastKnownLocation)) {
                // Have a better, newer location!
                Log.v(TAG, "new good location: " + location.toString());
                mLastKnownLocation = location;
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
}
