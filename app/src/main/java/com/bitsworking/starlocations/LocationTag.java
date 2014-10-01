package com.bitsworking.starlocations;

import android.content.Intent;
import android.location.Address;
import android.util.Log;

import com.bitsworking.starlocations.utils.SimpleHash;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Chris Hager <chris@linuxuser.at> on 29/09/14.
 */
public class LocationTag {
    private final static String TAG = "LocationTag";

    private LatLng latLng = null; // should always be populated
    public String title = "";

    public String locationHash = null;  // Hash representing the location

    // Very likely null:
    public String searchQuery = null; // might be null
    public Address address = null;    // might be null

    // Temporary reference to a Google Map Marker
    public Marker mapMarker = null;

    public LocationTag(LatLng coordinates) {
        this(coordinates, null, null);
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;

        try {
            this.locationHash = SimpleHash.sha1(String.format("%s/%s", latLng.latitude, latLng.longitude)).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            this.locationHash = Tools.random(10);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            this.locationHash = Tools.random(10);
        }
    }

    public LocationTag(LatLng coordinates, String searchQuery, Address address) {
        setLatLng(coordinates);
        this.searchQuery = searchQuery;
        this.address = address;
    }

    @Override
    public String toString() {
        return "LocationTag{" +
                "hash=" + locationHash + ", " +
                "title=" + title + ", " +
                "latLng=" + latLng + ", " +
                "mSearchParams=" + searchQuery + ", " +
                "address=" + address +
                '}';
    }

    public String getMarkerSnippet() {
        String ret = "";
        if (title != null && !title.isEmpty()) {
            ret += title + "\n\n";
        }

        ret += "Lat: " + latLng.latitude + "\n";
        ret += "Lng: " + latLng.longitude;

        if (address != null) {
            ret += "\n\n" + getAddressInfo(true);
        }

        return ret;
    }

    public String getAddressInfo(boolean lineBreaks) {
        if (address == null) {
            return null;
        }

        String ret = "";
        for (int i=0; i<=address.getMaxAddressLineIndex(); i++) {
            ret += address.getAddressLine(i);
            ret += lineBreaks ? "\n" : ", ";
        }

        if (!lineBreaks && ret.length() > 0) {
            // remove trailing `, `
            ret = ret.substring(0, ret.length() - 2);
        }
        return ret.trim();
    }

    public Intent getShareIntent() {
        String text = "http://maps.google.com?q=" + latLng.latitude + "," + latLng.longitude;
        if (address != null) {
            text += "\n\n" + getAddressInfo(true);
        }

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "Location Tag");
        i.putExtra(Intent.EXTRA_TEXT, text);
        return i;
    }
}
