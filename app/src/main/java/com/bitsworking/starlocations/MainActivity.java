package com.bitsworking.starlocations;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.widget.SearchView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.bitsworking.starlocations.contentproviders.MySearchRecentSuggestionsProvider;
import com.bitsworking.starlocations.exceptions.InvalidLocationException;
import com.bitsworking.starlocations.fragments.InfoFragment;
import com.bitsworking.starlocations.fragments.ListFragment;
import com.bitsworking.starlocations.fragments.MapFragment;
import com.bitsworking.starlocations.fragments.NavigationDrawerFragment;
import com.bitsworking.starlocations.jsondb.LocationTagDatabase;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;


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

    private Location mLastKnownLocation = null;
    private LocationManager mLocationManager;
    private ShareActionProvider mShareActionProvider;
    private Handler mHandler = new Handler();

    private SearchView mSearchView;

    private int fragment_attached = -1;
    private Fragment mLastFragment;

    private LocationTagDatabase mLocationTagDatabase;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Default section and title
        mTitle = getString(R.string.title_section0);

        // Setup Navigation Drawer
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Acquire a reference to the system Location Manager
        mLocationManager = (LocationManager) getSystemService(Activity.LOCATION_SERVICE);

        mLocationTagDatabase = new LocationTagDatabase(this);

        // Get the intent, verify the action and get the query
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Log.v(TAG, "handleIntent: " + intent.toString());
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
//            Log.v(TAG, "handleIntent->search: " + query);

            // If user comes from autocomplete, fill edittext like normal input
            mSearchView.setQuery("", false);
            mSearchView.setIconified(true);

            handleSearch(query);
        }
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        //exceptions will be thrown if provider is not permitted.
        try { gps_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); }
        catch (Exception e) {
            Log.w(TAG, "GPS location provider is disabled.");
        }
        try { network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER); }
        catch (Exception e) {
            Log.w(TAG, "Network location provider is disabled.");
        }

        // If no location enabled, we have a problem (TODO)
//        if(!gps_enabled && !network_enabled) {}

        // Register the listener with the Location Manager to receive location updates
        if (gps_enabled)
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        if (network_enabled)
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

//        mLocationDatabase.save();
        Toast.makeText(this, "DB: " + mLocationTagDatabase.numItems() + " items", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause");
        mLocationManager.removeUpdates(locationListener);
        super.onPause();  // Always call the superclass method first
    }

    public void onBackPressed(){
        // do something here and don't write super.onBackPressed()
        if (fragment_attached == FRAGMENT_MAP && mMapFragment.isOverlayVisible()) {
            mMapFragment.closeOverlay();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int fragmentId) {
        // update the main content by replacing fragments
        Log.v(TAG, "onNavigationDrawerItemSelected: " + fragmentId);

        if (fragment_attached == fragmentId) {
            Log.w(TAG, "Not showing the same fragment: " + fragmentId);
            return;
        }

        Fragment fragment = null;

        switch (fragmentId) {
            case FRAGMENT_MAP:
                fragment = mMapFragment;
                mTitle = getString(R.string.title_section0);
                break;
            case FRAGMENT_LIST:
                fragment = mListFragment;
                mTitle = getString(R.string.title_section1);
                break;
            case FRAGMENT_INFO:
                fragment = mInfoFragment;
                mTitle = getString(R.string.title_section2);
                break;
        }

        if (fragment == null) {
            Log.e(TAG, "Cannot attach fragment that is null");
            return;
        }

        fragment_attached = fragmentId;

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
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

            // Setup ShareActionView with default intent
            mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.action_share).getActionProvider();
            mShareActionProvider.setShareIntent(null);

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

    /**
     * Returns a learned location, or as fallback the last known by system
     */
    public Location getLocation() {
        if (mLastKnownLocation == null) {
            // return unsafe location
            Location l1 = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location l2 = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            return (Tools.isBetterLocation(l2, l1)) ? l2 : l1;
        } else {
            // return last learned location
            return mLastKnownLocation;
        }
    }

    /**
     * Send info about new learned location to the fragments
     * @param location
     */
    private void makeUseOfNewLocation(Location location) {
        // Update ui and code on new location
        mLastKnownLocation = location;

        // Let fragments know about better location
        if (fragment_attached == FRAGMENT_INFO) {
            mInfoFragment.newLastKnownLocation(location);
        }
    }

    /**
     * Start the Google Places API autocomplete in a Thread
     * @param input
     */
    private void autocomplete(final String input) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                _autocomplete(input);
            }
        };
        thread.start();
    }

    private ArrayList<String> _autocomplete(String input) {
        return _autocomplete(input, null, null);
    }

    private ArrayList<String> _autocomplete(String input, Location bias_location, Integer bias_radius_m) {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + PLACES_API_TYPE_AUTOCOMPLETE + PLACES_API_OUT_JSON);
            sb.append("?key=" + PLACES_API_KEY);
//            sb.append("&components=country:uk");
//            sb.append("&types=(geocode)");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
            Log.v(TAG, jsonResults.toString());
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    /**
     * `handleSearch` handles any custom search query by user input.
     * Typically this is either GPS coordinates or a location name.
     *
     * This method creates a LocationTag with the coordinates, in
     * case of a location name after geocoding the name to coordinates.
     *
     * @param query
     */
    private void handleSearch(final String query) {
        Log.v(TAG, "handleSearch: " + query);

        // Save to recent suggestions
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                MySearchRecentSuggestionsProvider.AUTHORITY, MySearchRecentSuggestionsProvider.MODE);
        suggestions.saveRecentQuery(query, null);

        // Geocoding and shit in background thread
        Thread thread = new Thread() {
            @Override
            public void run() {
                // See if we can get GPS coordinates from this query
                LatLng coords = Tools.getLatLngFromQuery(query);
                Address address = null;

                // If not, try geocoding the location name
                if (coords == null) {
                    try {
                        address = Tools.geocodeLocationNameToAddress(getBaseContext(), query);
                        coords = new LatLng(address.getLatitude(), address.getLongitude());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    } catch (InvalidLocationException e) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getBaseContext(), "Could not find " + query, Toast.LENGTH_LONG).show();
                            }
                        });
                        return;
                    }
                }

                final LocationTag tag = new LocationTag(coords, query, address);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mMapFragment.addTempMarker(tag);
                    }
                });
            }
        };
        thread.start();

        // Show Map Fragment
        mNavigationDrawerFragment.selectItem(FRAGMENT_MAP);
    }

    public void saveLocationTag(LocationTag tag) {
        mLocationTagDatabase.put(tag);
        mLocationTagDatabase.save();
    }

    public LocationTagDatabase getLocationTagDatabase() {
        return mLocationTagDatabase;
    }
}
