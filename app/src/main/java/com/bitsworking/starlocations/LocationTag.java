package com.bitsworking.starlocations;

import android.location.Address;
import android.util.Log;

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
        Log.v(TAG, address.toString());
        if (address == null) return null;

        String ret = "Lat: " + latLng.latitude + "\nLng: " + latLng.longitude;
        ret += "<br><br />";
        if (address.getMaxAddressLineIndex() > 0) ret += address.getAddressLine(0) + ", ";
        if (address.getLocality() != null) ret += address.getLocality() + ", ";
        if (address.getAdminArea() != null) ret += address.getAdminArea() + ", ";
        if (address.getSubAdminArea() != null) ret += address.getSubAdminArea() + ", ";
        ret += address.getCountryName();
        return ret;
    }
}
