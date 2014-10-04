package com.bitsworking.starlocations;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.SearchRecentSuggestions;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
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
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

    private Handler mHandler = new Handler();

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private boolean gps_enabled = false;
    private boolean network_enabled = false;

    private LocationManager mLocationManager;
    private ShareActionProvider mShareActionProvider;
    private Location mLastKnownLocation = null;

    private SearchView mSearchView;

    private int fragment_attached = -1;
    private Fragment mLastFragment;

    // Store for all saved location tags
//    private LocationTagDatabase mLocationTagDatabase;
    private LocationTagManager mLocationTagManager;

    // Store for all temporary location tags
    private ArrayList<LocationTag> mTempLocationTags = new ArrayList<LocationTag>();

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

        // Instantiate the Tag Manager
        mLocationTagManager = new LocationTagManager(this);

        // Get the intent, verify the action and get the query
        handleIntent(getIntent());

//        parseLocationsFromText("Rasumofskygasse 28\n" +
//                "1030 Wien\n" +
//                "48.31013627,15.790834687\n" +
//                "https://www.google.com/maps/preview?q=Rasumofskygasse+28,+1030+Wien&ftid=0x476d076dec808089:0xaea718f50b73857c&hl=en&gl=us\n" +
//                "http://goo.gl/maps/kv4wL");
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

            handleSearch(query, true);

        } else if (Intent.ACTION_SEND.equals(intent.getAction())) {
            Log.v(TAG, "Send intent: " + intent);
            String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
            String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            Log.v(TAG, "- text: " + text);
            parseLocationsFromText(text);

        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Log.v(TAG, "View intent: " + intent);
            Log.v(TAG, "- data: " + intent.getDataString());
            parseLocationsFromDataUri(intent.getData());
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
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        Log.v(TAG, "onPause");
        mLocationManager.removeUpdates(locationListener);
        mLocationTagManager.getDatabase().logDb();
    }

    public void onBackPressed(){
        // do something here and don't write super.onBackPressed()
        if (fragment_attached == FRAGMENT_MAP && mMapFragment.isOverlayVisible()) {
            mMapFragment.closeOverlay();
        } else if (fragment_attached == FRAGMENT_SETTINGS) {
            mNavigationDrawerFragment.selectItem(FRAGMENT_MAP);
        } else if (fragment_attached == FRAGMENT_DBVIEWER) {
            mNavigationDrawerFragment.selectItem(FRAGMENT_SETTINGS);
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
            case FRAGMENT_SETTINGS:
                fragment = new SettingsFragment();
                mTitle = getString(R.string.title_section2);
                break;
            case FRAGMENT_DBVIEWER:
                fragment = new DBViewerFragment();
                mTitle = getString(R.string.fragment_dbviewer);
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
        if (fragment_attached == FRAGMENT_SETTINGS) {
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
    private void handleSearch(final String query, final boolean addToHistory) {
        Log.v(TAG, "handleSearch: " + query);

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
                    } catch (final IOException e) {
                        e.printStackTrace();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getBaseContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
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

                // Successfully found coords. Save to recent suggestions.
                if (addToHistory) {
                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getBaseContext(),
                            MySearchRecentSuggestionsProvider.AUTHORITY, MySearchRecentSuggestionsProvider.MODE);
                    suggestions.saveRecentQuery(query, null);
                }

                final LocationTag tag = new LocationTag(coords, query, address);
                addTempLocationTag(tag, true);
            }
        };
        thread.start();

        // Show Map Fragment
        mNavigationDrawerFragment.selectItem(FRAGMENT_MAP);
    }

    public void askToDeleteTag(final LocationTag tag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete saved marker?")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LocationTagDatabase db = mLocationTagManager.getDatabase();
                        if (db.contains(tag.uid)) {
                            db.remove(tag.uid);
                            db.save();
                        }

                        removeTempLocationTag(tag, true);
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void askToMoveSavedTag(final LocationTag tag, final LatLng newPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Move saved marker?")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        tag.setLatLng(newPosition);

                        saveLocationTag(tag);

                        mMapFragment.removeMarker(tag.mapMarker);
                        mMapFragment.addMarker(tag);

                        // Update overlay
                        if (mMapFragment.isOverlayVisible()) {
                            mMapFragment.showOverlay(tag);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMapFragment.removeMarker(tag.mapMarker);
                        mMapFragment.addMarker(tag);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void askToDeleteDatabase() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete the database?")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mLocationTagManager.getDatabase().deleteDatabase();
                        mLocationTagManager.removeAllTempLocationTags();
                        mMapFragment = new MapFragment();
                        Toast.makeText(getBaseContext(), "Database deleted", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

//    public LocationTagDatabase getLocationTagDatabase() {
//        return mLocationTagManager.getDatabase();
//    }

    public void saveLocationTag(LocationTag tag) {
        mLocationTagManager.getDatabase().put(tag);
        mLocationTagManager.getDatabase().save();
    }

    public boolean isSavedLocationTag(LocationTag tag) {
        return mLocationTagManager.getDatabase().contains(tag.uid);
    }

    // Returns all saved and unsaved location tags
    public Collection<LocationTag> getAllLocationTags() {
        // get saved tags
        Collection<LocationTag> collection = mLocationTagManager.getDatabase().getAll();

        // get unsaved tags
        collection.addAll(mLocationTagManager.getTempLocationTags());
        return collection;
    }

    public void addTempLocationTag(final LocationTag tag, boolean showOnMapIfSetup) {
        mLocationTagManager.addTempLocationTag(tag);

        if (showOnMapIfSetup && mMapFragment.isMapsSetup) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mMapFragment.addMarker(tag, true, 12, true);
                }
            });
        }
    }

    public void removeTempLocationTag(final LocationTag tag, boolean removeFromMapIfSetup) {
        mLocationTagManager.removeTempLocationTag(tag);

        if (removeFromMapIfSetup && mMapFragment.isMapsSetup && tag.mapMarker != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mMapFragment.removeMarker(tag.mapMarker);
                }
            });
        }
    }

    private void parseLocationsFromText(String text) {
//        ProgressDialog progressDialog = ProgressDialog.show(this, null, "Searching location 2...");
//        progressDialog.setCancelable(true);

        // 1. GPS Coordinates
        Pattern pattern = Pattern.compile("([0-9]+[.][0-9]+[ ,]+[0-9]+[.][0-9]+)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String gpsString = matcher.group(1);
            Log.v(TAG, "Found gps coords: " + gpsString);

            String[] parts = gpsString.split(",");
            final LatLng coords = new LatLng(
                    Double.valueOf(parts[0].trim()),
                    Double.valueOf(parts[1].trim())
            );

            addTempLocationTag(new LocationTag(coords), true);
        }

        // 2. google maps links
        pattern = Pattern.compile("google.com/maps.*q=([^& \n]*)");
        matcher = pattern.matcher(text);
        while (matcher.find()) {
            String query = matcher.group(1);
            Log.v(TAG, "Found google maps query: " + query);
            handleSearch(query, false);
        }

        // 3. goo.gl links
        pattern = Pattern.compile("goo[.]gl/maps/([a-zA-Z0-9]*)");
        matcher = pattern.matcher(text);
        while (matcher.find()) {
            String url = "https://goo.gl/maps/" + matcher.group(1);
            Log.v(TAG, "Found goo.gl shortcut X: " + url);
//            addTempLocationTag(new LocationTag(new LatLng(48.31013627, 15.790834687)), true);
        }
    }

    private void parseLocationsFromDataUri(Uri data) {
        // TODO (geo:lat,lng?q=lat,lng
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            // DB: Show
            ((Preference) findPreference("pref_key_db_show")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ((MainActivity) getActivity()).mNavigationDrawerFragment.selectItem(FRAGMENT_DBVIEWER);
                    return false;
                }
            });

            // DB: Delete
            ((Preference) findPreference("pref_key_db_delete")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ((MainActivity) getActivity()).askToDeleteDatabase();
                    return false;
                }
            });

            // Search: Clear autocomplete
            ((Preference) findPreference("pref_key_search_clear_autocomplete")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                            MySearchRecentSuggestionsProvider.AUTHORITY, MySearchRecentSuggestionsProvider.MODE);
                    suggestions.clearHistory();
                    Toast.makeText(getActivity(), "Search history cleared", Toast.LENGTH_LONG).show();
                    return false;
                }
            });
        }
    }

    public static class DBViewerFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            TextView rootView = new TextView(getActivity());
            rootView.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            try {
                JSONObject root = new JSONObject(((MainActivity) getActivity()).mLocationTagManager.getDatabase().getDatabaseFileContents());
                rootView.setText((root.toString(4)));
            } catch (IOException e) {
                e.printStackTrace();
                rootView.setText(e.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                rootView.setText(e.toString());
            }
            return rootView;
        }
    }
}
