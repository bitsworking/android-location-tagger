package com.bitsworking.starlocations;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

/**
 * Created by Chris Hager <chris@linuxuser.at> on 29/09/14.
 */
public class LocationTag {
    private final static String TAG = "LocationTag";

    enum SearchType {
        COORDINATES,
        LOCATION_NAME;
    }

    public static class SearchParams {
        private SearchType searchType;
        private String query;

        public SearchParams(SearchType searchType, String query) {
            this.searchType = searchType;
            this.query = query;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public SearchType getSearchType() {
            return searchType;
        }

        public void setSearchType(SearchType searchType) {
            this.searchType = searchType;
        }

        @Override
        public String toString() {
            return "SearchParams{" +
                    "searchType=" + searchType +
                    ", query='" + query + '\'' +
                    '}';
        }
    }

    private LatLng mCoordinates = null;
    private Address mAddress = null;

    private SearchParams mSearchParams = null;

    /**
     * Query for either GPS coordinates or location name
     */
    public static LocationTag fromLocationQuery(Context context, String query) {
        Log.v(TAG, "fromLocationQuery: " + query);
        // Check whether GPS
        String[] parts = query.split(query.contains(",") ? "," : ";");
        if (parts.length == 2) {
            try {
                LatLng coordinates = new LatLng(Double.valueOf(parts[0]), Double.valueOf(parts[1]));
                return fromCoordinates(context, coordinates);
            } catch (NumberFormatException e) {
                // not gps coords. proceed to handling as location name
            }
        }

        return fromLocationName(context, query);
    }

    private static LocationTag fromLocationName(Context context, String locationName) {
        Log.v(TAG, "fromLocationName: " + locationName);

        LocationTag tag = new LocationTag();
        tag.setSearchParams(new SearchParams(SearchType.LOCATION_NAME, locationName));

        Geocoder geocoder = new Geocoder(context);
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses.size() > 0) {
                Log.v(TAG, addresses.get(0).toString());
                tag.setAddress(addresses.get(0));
                tag.setCoordinates(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()));
            } else {
                Log.w(TAG, "No address found for locationName: " + locationName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "tag: " + tag.toString());
        return tag;
    }

    private static LocationTag fromCoordinates(Context context, LatLng coordinates) {
        Log.v(TAG, "fromLocationCoordinates: " + coordinates.toString());

        LocationTag tag = new LocationTag();
        tag.setCoordinates(coordinates);
        tag.setSearchParams(new SearchParams(SearchType.COORDINATES, coordinates.toString()));

        Geocoder geocoder = new Geocoder(context);
        try {
            List<Address> addresses = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1);
            if (addresses.size() > 0) {
                Log.v(TAG, addresses.get(0).toString());
                tag.setAddress(addresses.get(0));
                tag.setCoordinates(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()));
            } else {
                Log.w(TAG, "No address found for coordinates");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "tag: " + tag.toString());
        return tag;
    }

    public LatLng getCoordinates() {
        return mCoordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.mCoordinates = coordinates;
    }

    public Address getAddress() {
        return mAddress;
    }

    public void setAddress(Address address) {
        this.mAddress = address;
    }

    public SearchParams getSearchParams() {
        return mSearchParams;
    }

    public void setSearchParams(SearchParams searchParams) {
        this.mSearchParams = searchParams;
    }

    @Override
    public String toString() {
        return "LocationTag{" +
                "mSearchParams=" + mSearchParams +
                "mCoordinates=" + mCoordinates +
                ", mAddress=" + mAddress + '\'' +
                '}';
    }
}
