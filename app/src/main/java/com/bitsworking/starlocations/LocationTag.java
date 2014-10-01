package com.bitsworking.starlocations;

import android.content.Intent;
import android.location.Address;
import android.util.Log;

import com.bitsworking.starlocations.utils.SimpleHash;
import com.google.android.gms.maps.model.LatLng;

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
    public String title = null;

    public String locationHash = null;  // Hash representing the location

    // Very likely null:
    public String searchQuery = null; // might be null
    public Address address = null;    // might be null

    public LocationTag(LatLng coordinates) {
        this(coordinates, null, null);
    }

//    public LocationTag(LatLng coordinates, String searchQuery) {
//        this(coordinates, searchQuery, null);
//    }

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

    public static LocationTag fromJSON(JSONObject jsonObject) throws JSONException {
        double lat = jsonObject.getDouble("latitude");
        double lng = jsonObject.getDouble("longitude");

        LocationTag tag = new LocationTag(new LatLng(lat, lng));

        // Fill optional fields, do nothing
        try { tag.title = jsonObject.getString("title"); } catch (JSONException e) {}
        try { tag.searchQuery = jsonObject.getString("searchQuery"); } catch (JSONException e) {}

        return tag;
    }

    @Override
    public String toString() {
        return "LocationTag{" +
                "hash=" + locationHash + ", " +
                "latLng=" + latLng + ", " +
                "mSearchParams=" + searchQuery + ", " +
                "address=" + address +
                '}';
    }

    public JSONObject toJSONObject() {
        JSONObject item = new JSONObject();
        try {
            item.put("hash", locationHash);
            item.put("latitude", latLng.latitude);
            item.put("longitude", latLng.longitude);
            item.put("title", title);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return item;
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
        if (address == null) {
            return null;
        }

        String ret = "";
        for (int i=0; i<=address.getMaxAddressLineIndex(); i++) {
            ret += address.getAddressLine(i) + "\n";
        }

//        if (address.getMaxAddressLineIndex() > 0) ret += address.getAddressLine(0) + "\n";
//        if (address.getLocality() != null) ret += address.getLocality() + "\n";
//        if (address.getAdminArea() != null) ret += address.getAdminArea() + "\n";
//        if (address.getSubAdminArea() != null) ret += address.getSubAdminArea() + "\n";
//        ret += address.getCountryName();

        return ret.trim();
    }

    public Intent getShareIntent() {
        String text = "http://maps.google.com?q=" + latLng.latitude + "," + latLng.longitude;
        if (address != null) {
            text += "\n\n" + getAddressInfo();
        }

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "Location Tag");
        i.putExtra(Intent.EXTRA_TEXT, text);
        return i;
    }
}
