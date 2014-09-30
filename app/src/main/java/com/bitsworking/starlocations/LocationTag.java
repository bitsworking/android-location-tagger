package com.bitsworking.starlocations;

import android.location.Address;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Chris Hager <chris@linuxuser.at> on 29/09/14.
 */
public class LocationTag {
    private final static String TAG = "LocationTag";

    public LatLng latLng = null; // should always be populated
    public Address address = null;    // might be null
    public String searchQuery = null; // might be null

    public LocationTag(LatLng coordinates) {
        this(coordinates, null);
    }

    public LocationTag(LatLng coordinates, String searchQuery) {
        this(coordinates, searchQuery, null);
    }

    public LocationTag(LatLng coordinates, String searchQuery, Address address) {
        this.latLng = coordinates;
        this.searchQuery = searchQuery;
        this.address = address;
    }

    @Override
    public String toString() {
        return "LocationTag{" +
                "latLng=" + latLng + ", " +
                "mSearchParams=" + searchQuery + ", " +
                "address=" + address +
                '}';
    }

    public String getMarkerTitle() {
        if (searchQuery != null)
            return searchQuery;
        else
            return latLng.toString();
    }

    public String getMarkerSnippet() {
        if (address != null)
            return address.toString();
        return null;
    }
}
