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

    public String getMarkerSnippet() {
        String ret = "";
        ret += "Lat: " + latLng.latitude + "\n";
        ret += "Lng: " + latLng.longitude;

        if (address != null) {
            ret += "\n\n" + getAddressInfo();
        }

        return ret;
    }

    public String getAddressInfo() {
        if (address != null) {
            return null;
        }

        String ret = "";
        if (address.getMaxAddressLineIndex() > 0) ret += address.getAddressLine(0) + "\n";
        if (address.getLocality() != null) ret += address.getLocality() + "\n";
        if (address.getAdminArea() != null) ret += address.getAdminArea() + "\n";
        if (address.getSubAdminArea() != null) ret += address.getSubAdminArea() + "\n";
        ret += address.getCountryName();


        return ret;
    }
}
