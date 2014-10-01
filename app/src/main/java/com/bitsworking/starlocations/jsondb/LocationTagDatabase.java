package com.bitsworking.starlocations.jsondb;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.bitsworking.starlocations.Constants;
import com.bitsworking.starlocations.LocationTag;
import com.bitsworking.starlocations.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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

    private JSONObject root;

    public LocationTagDatabase(Context context) {
        // Remember the app version name to put in db
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appVersionName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            appVersionName = "?";
        }

        // 1. open db file

        // 2. wait for instructions (read, write, query)
        if (!Tools.isExternalStorageWritable()) {
            Log.e(TAG, "External storage not writable. Cannot save db.");
            throw new IllegalStateException();
        }

        // Create directory if needed
        File dir = new File(Tools.getSdCardDirectory());
        if (!dir.exists()) dir.mkdir();

        if (DEBUG_REBUILD_DB) {
            // If we dont want to read db
            Log.w(TAG, "debug: rebuild db");
            setupEmptyDatabase();
            return;
        }

        // Read db string from json file
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
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }

        loadFromJsonString(text.toString().trim());
    }

    public void save() {
        try {
            root.put("version-schema", VERSION_DB_SCHEMA);
            root.put("version-app", appVersionName);

            BufferedWriter bw = new BufferedWriter(new FileWriter(Tools.getSdCardDirectory() + "/" + Constants.DB_FILE, false));
            bw.write(root.toString());
            bw.close();

            Log.v(TAG, "DB Saved");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadFromJsonString(String jsonString) {
        if (!jsonString.isEmpty()) {
            // Load DB from File
            try {
                root = new JSONObject(jsonString);
                Log.v(TAG, "Loaded db from file");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Could not parse JSON string from database file. Creating new DB.");
                setupEmptyDatabase();
            }

        } else {
            Log.v(TAG, "DB from file empty. Setup empty database");
            setupEmptyDatabase();
        }
        Log.v(TAG, "DB: " + root.toString());
    }

    private void setupEmptyDatabase() {
        root = new JSONObject();
        try {
            root.put("locations", new JSONArray());
            root.put("version-schema", VERSION_DB_SCHEMA);
            root.put("version-app", appVersionName + "XX");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int numItems() {
        try {
            return root.getJSONArray("locations").length();
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void put(LocationTag tag) {
        try {
            JSONArray locations = root.getJSONArray("locations");
            locations.put(tag.toJSONObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "added locationTag " + tag);
        Log.v(TAG, "DB: " + root.toString());
    }

    public LocationTag get(int index) {
        try {
            JSONArray locations = root.getJSONArray("locations");
            return LocationTag.fromJSON(locations.getJSONObject(index));
        } catch (JSONException e) {
            e.printStackTrace();
            throw new IndexOutOfBoundsException();
        }
    }
}
