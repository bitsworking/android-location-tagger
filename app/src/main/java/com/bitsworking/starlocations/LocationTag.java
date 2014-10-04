package com.bitsworking.starlocations;

import android.content.Intent;
import android.location.Address;
import android.net.Uri;

import com.bitsworking.starlocations.utils.SimpleHash;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Chris Hager <chris@linuxuser.at> on 29/09/14.
 */
public class LocationTag {
    private final static String TAG = "LocationTag";

    public String uid = null;     // unique random id of this tag
    private LatLng latLng = null; // is always there
    public String title = "";

    // If this tag has been saved, the timestamp of the initial save is stored
    // (current time in milliseconds since January 1, 1970 00:00:00.0 UTC.)
    public Long savedTimestamp = null;

    // If this tag has been created/edited, the timestamp of the create/last edit is stored
    // (current time in milliseconds since January 1, 1970 00:00:00.0 UTC.)
    public Long editedTimestamp = null;

    // Very likely null
    public String searchQuery = null; // might be null
    public Address address = null;    // might be null

    // Temporary reference to a Google Map Marker
    public Marker mapMarker = null;

    public LocationTag(LatLng coordinates) {
        this(coordinates, null, null);
    }

    public LocationTag(LatLng coordinates, String searchQuery, Address address) {
        setLatLng(coordinates);
        this.searchQuery = searchQuery;
        this.address = address;
        this.uid = "_" + Tools.getRandomString(8);
    }

    public boolean isSaved() {
        return savedTimestamp != null;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    @Override
    public String toString() {
        return "LocationTag{" +
                "uid=" + uid + ", " +
                "saved=" + savedTimestamp + ", " +
                "edited=" + editedTimestamp + ", " +
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
        String subject = "Location Tag";
        if (title != null && !title.isEmpty()) subject += ": " + title;

        String link = String.format("https://www.google.com/maps?q=%s,%s", latLng.latitude, latLng.longitude);
        String text = link;
        if (address != null) {
            text += "\n\n" + getAddressInfo(true);
        }

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, text);
        return i;
    }


    public Intent getViewIntent() {
        String uri = String.format("geo:%s,%s?q=%s,%s", latLng.latitude, latLng.longitude, latLng.latitude, latLng.longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        return intent;
    }
}
