package com.bitsworking.starlocations;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.bitsworking.starlocations.exceptions.InvalidLocationException;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

/**
 * Created by chris on 28/09/2014.
 */
public class Tools {
    private static final String TAG = "Tools";
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    // TODO Later: refactor to use common location class
    public static Intent makeLocationSharingIntent(String subject, String content) {
        String message = "abc ... " + content + "<";
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, message);
        return i;
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    public static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public static Address geocodeLocationNameToAddress(Context context, String locationName) throws IOException, InvalidLocationException {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
        if (addresses.size() == 0) {
            throw new InvalidLocationException();
        }
        return addresses.get(0);
    }

    public static Address geocodeCoordinatesToAddress(Context context, LatLng coordinates) throws IOException, InvalidLocationException {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1);
        if (addresses.size() == 0) {
            throw new InvalidLocationException();
        }
        return addresses.get(0);
    }

    // Returns true if query string appears to be GPS coordinates
    public static LatLng getLatLngFromQuery(String query) {
        String[] parts = query.trim().split(query.contains(",") ? "," : ";");
        if (parts.length == 2) {
            try {
                return new LatLng(Double.valueOf(parts[0]), Double.valueOf(parts[1]));
            } catch (NumberFormatException e) {}
        }
        return null;
    }
}
