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
    private final String JSON_KEY_TIMESTAMP_CREATED = "savedTimestamp";
    private final String JSON_KEY_TIMESTAMP_EDITED = "editedTimestamp";
    private final String JSON_KEY_LATITUDE = "latitude";
    private final String JSON_KEY_LONGITUDE = "longitude";
    private final String JSON_KEY_TITLE = "title";
    private final String JSON_KEY_UID = "uid";
    private final String JSON_KEY_SEARCHQUERY = "searchQuery";

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

    public String getDatabaseFileContents() throws IOException {
        File dbFile = new File(Tools.getSdCardDirectory(), Constants.DB_FILE);
        StringBuilder text = new StringBuilder();

        BufferedReader br = new BufferedReader(new FileReader(dbFile));
        String line;
        while ((line = br.readLine()) != null) {
            text.append(line);
            text.append('\n');
        }
        br.close();

        // Parse database into JSON Object
        return text.toString().trim();
    }

    /**
     * Load database from JSON string
     */
    private void loadDatabaseFromFile() {
        try {
            JSONObject root = new JSONObject(getDatabaseFileContents());
            JSONArray locations = root.getJSONArray("locations");
            final int cnt = locations.length();
            for (int i=0; i<cnt; i++) {
                JSONObject j = locations.getJSONObject(i);

                double lat = j.getDouble(JSON_KEY_LATITUDE);
                double lng = j.getDouble(JSON_KEY_LONGITUDE);
                LocationTag tag = new LocationTag(new LatLng(lat, lng));
                tag.uid = j.getString(JSON_KEY_UID);

                try { tag.savedTimestamp = j.getLong(JSON_KEY_TIMESTAMP_CREATED); } catch (JSONException e) {}
                try { tag.editedTimestamp = j.getLong(JSON_KEY_TIMESTAMP_EDITED); } catch (JSONException e) {}

                try { tag.title = j.getString(JSON_KEY_TITLE); } catch (JSONException e) {}

                // Fill optional fields, do nothing
                try { tag.title = j.getString(JSON_KEY_TITLE); } catch (JSONException e) {}
                try { tag.searchQuery = j.getString(JSON_KEY_SEARCHQUERY); } catch (JSONException e) {}

                locationTags.put(tag.uid, tag);
            }

            Log.v(TAG, "Loaded db from file");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Could not parse JSON string from database file. Creating new DB." + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Could not parse JSON string from database file. Creating new DB. " + e.toString());
        }
    }

    /**
     * Save database to sd card json file
     */
    public void save() {
        try {
            long currentTimestamp = System.currentTimeMillis();
            File dbFile = new File(Tools.getSdCardDirectory(), Constants.DB_FILE);
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(dbFile), "UTF-8"));

            writer.beginObject(); // start root object

            writer.name("developer").value("Chris Hager <chris@bitsworking.com>");
            writer.name("version-schema").value(VERSION_DB_SCHEMA);
            writer.name("version-app").value(appVersionName);

            // Write locations array
            writer.name("locations");
            writer.beginArray(); // start locations array

            for (LocationTag tag : locationTags.values()) {
                writer.beginObject();

                // Original saved timestamp if exists, else current timestamp
                long savedTimestamp = (tag.savedTimestamp != null) ? tag.savedTimestamp : currentTimestamp;
                tag.savedTimestamp = savedTimestamp;
                tag.editedTimestamp = currentTimestamp;
                writer.name(JSON_KEY_TIMESTAMP_CREATED).value(savedTimestamp);
                writer.name(JSON_KEY_TIMESTAMP_EDITED).value(currentTimestamp);

                // Object values
                writer.name(JSON_KEY_UID).value(tag.uid);
                writer.name(JSON_KEY_LATITUDE).value(tag.getLatLng().latitude);
                writer.name(JSON_KEY_LONGITUDE).value(tag.getLatLng().longitude);
                writer.name(JSON_KEY_TITLE).value(tag.title);

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

    public boolean contains(String uid) {
        return locationTags.containsKey(uid);
    }

    // Add a LocationTag to the database
    public void put(LocationTag tag) {
        locationTags.put(tag.uid, tag);
        Log.v(TAG, "added locationTag " + tag);
    }

    // Get a LocationTag from the database by array index
    public LocationTag get(String uid) {
        return locationTags.get(uid);
    }

    public Collection<LocationTag> getAll() {
        return locationTags.values();
    }

    // Remove
    public void remove(String uid) {
        locationTags.remove(uid);
        Log.v(TAG, "removed locationTag " + uid);
    }

    // Deletes database file and empties all content from memoty
    public void deleteDatabase() {
        locationTags.clear();
        File dbFile = new File(Tools.getSdCardDirectory(), Constants.DB_FILE);
        dbFile.delete();
    }

    public void logDb() {
        Log.v(TAG, locationTags.toString());
    }
}
