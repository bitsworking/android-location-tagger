package com.bitsworking.starlocations;

import android.content.Context;

import com.bitsworking.starlocations.jsondb.LocationTagDatabase;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Manages both saved and unsaved markers
 *
 * Created by chris on 04/10/2014.
 */
public class LocationTagManager {
    private LocationTagDatabase mLocationTagDatabase;

    private Collection<LocationTag> mTempLocationTags = new ArrayList<LocationTag>();

    public LocationTagManager(Context context) {
        mLocationTagDatabase = new LocationTagDatabase(context);
//        mLocationTagDatabase.logDb();
    }

    public LocationTagDatabase getDatabase() {
        return mLocationTagDatabase;
    }

    public Collection<LocationTag> getTempLocationTags() {
        return mTempLocationTags;
    }

    public void addTempLocationTag(LocationTag tag) {
        mTempLocationTags.add(tag);
    }

    public void removeTempLocationTag(LocationTag tag) {
        mTempLocationTags.remove(tag);
    }
}
