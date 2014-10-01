package com.bitsworking.starlocations.jsondb;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.JsonWriter;
import android.util.Log;

import com.bitsworking.starlocations.Constants;
import com.bitsworking.starlocations.LocationTag;
import com.bitsworking.starlocations.Tools;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;

/**
 * JSON Location Database
 *
 * Created by Chris Hager <chris@linuxuser.at> on 01/10/14.
 */
public class LocationTagDatabase {
    private final String TAG = "LocationTagDatabase";

    static final int VERSION_DB_SCHEMA = 1;
    private final boolean DEBUG_REBUILD_DB = false;

    private String appVersionName;

//    private JSONObject root;
    private HashMap<String, LocationTag> locationTags = new HashMap<String, LocationTag>();

    public LocationTagDatabase(Context context) {
        // Remember the app version name to put in db
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appVersionName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            appVersionName = "?";
        }

        // Check if SD card is writable
        if (!Tools.isExternalStorageWritable()) {
            Log.e(TAG, "External storage not writable. Cannot save db.");
            throw new IllegalStateException();
        }

        // Create directory if needed
        File dir = new File(Tools.getSdCardDirectory());
        if (!dir.exists()) dir.mkdir();

        // Read db from json file
        loadDatabaseFromFile();
    }

    private void loadDatabaseFromFile() {
        File dbFile = new File(Tools.getSdCardDirectory(), Constants.DB_FILE);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(dbFile));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();

            // Parse database into JSON Object
            loadDatabaseFromJsonString(text.toString().trim());

        } catch (IOException e) {
            // If we couldn't read database on sd card, setup new one
            e.printStackTrace();
        }
    }

    /**
     * Load database from JSON string
     */
    private void loadDatabaseFromJsonString(String jsonString) {
        if (!jsonString.isEmpty()) {
            try {
                JSONObject root = new JSONObject(jsonString);
                JSONArray locations = root.getJSONArray("locations");
                final int cnt = locations.length();
                for (int i=0; i<cnt; i++) {
                    JSONObject j = locations.getJSONObject(i);

                    double lat = j.getDouble("latitude");
                    double lng = j.getDouble("longitude");
                    LocationTag tag = new LocationTag(new LatLng(lat, lng));

                    // Fill optional fields, do nothing
                    try { tag.title = j.getString("title"); } catch (JSONException e) {}
                    try { tag.searchQuery = j.getString("searchQuery"); } catch (JSONException e) {}

                    locationTags.put(tag.locationHash, tag);
                }

                Log.v(TAG, "Loaded db from file");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Could not parse JSON string from database file. Creating new DB.");
            }
        } else {
            Log.v(TAG, "DB from file empty. Setup empty database");
        }
    }

    /**
     * Save database to sd card json file
     */
    public void save() {
        try {
            File dbFile = new File(Tools.getSdCardDirectory(), Constants.DB_FILE);
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(dbFile), "UTF-8"));

            writer.beginObject(); // start root object
            writer.name("version-schema").value(VERSION_DB_SCHEMA);
            writer.name("version-app").value(appVersionName);

            // Write locations array
            writer.name("locations");
            writer.beginArray(); // start locations array

            for (LocationTag tag : locationTags.values()) {
                writer.beginObject();
                writer.name("hash").value(tag.locationHash);
                writer.name("latitude").value(tag.getLatLng().latitude);
                writer.name("longitude").value(tag.getLatLng().longitude);
                writer.name("title").value(tag.title);
                writer.endObject();
            }

            writer.endArray();  // end locations array
            writer.endObject(); // end root object
            writer.close();
            Log.v(TAG, "Saved Database");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Create a new, blank database
//    private void setupEmptyDatabase() {
//        root = new JSONObject();
//        try {
//            root.put("locations", new JSONArray());
//            root.put("version-schema", VERSION_DB_SCHEMA);
//            root.put("version-app", appVersionName + "XX");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    // Get number of items in the database
    public int numItems() {
        return locationTags.size();
    }

    public boolean contains(String hash) {
        return locationTags.containsKey(hash);
    }

    // Add a LocationTag to the database
    public void put(LocationTag tag) {
        locationTags.put(tag.locationHash, tag);
        Log.v(TAG, "added locationTag " + tag);
    }

    // Get a LocationTag from the database by array index
    public LocationTag get(String hash) {
        return locationTags.get(hash);
    }

    public Collection<LocationTag> getAll() {
        return locationTags.values();
    }

    // Remove
    public void remove(String hash) {
        locationTags.remove(hash);
        Log.v(TAG, "removed locationTag " + hash);
    }
}
