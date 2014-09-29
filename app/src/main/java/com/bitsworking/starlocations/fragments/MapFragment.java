package com.bitsworking.starlocations.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bitsworking.starlocations.Constants;
import com.bitsworking.starlocations.LocationTag;
import com.bitsworking.starlocations.MainActivity;
import com.bitsworking.starlocations.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * The Map Fragment
 */
public class MapFragment extends Fragment {
    private final String TAG = "MapFragment";

    private MapView mMapView;
    private static GoogleMap mMap;

    private Activity mActivity;

    private Location mLastKnownLocation;

    public MapFragment() {
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "onCreate");

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        Log.v(TAG, "Setting up mMapView");
        mMapView.onCreate(savedInstanceState);

        mMap = mMapView.getMap();
        MapsInitializer.initialize(this.getActivity());

        setUpMap();

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.v(TAG, "onAttach");
        mActivity = activity;
//        ((MainActivity) activity).onSectionAttached(Constants.FRAGMENT_MAP);
    }


    @Override
    public void onResume() {
        Log.v(TAG, "onResume");
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        Log.v(TAG, "onLowMemory");
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void newLastKnownLocation(Location location) {
//        Log.v(TAG, "newL");
        mLastKnownLocation = location;
    }

    private void setUpMap() {
        final Location lastUnsafeLocation = ((MainActivity) getActivity()).getLocation();
        UiSettings uiSettings = mMap.getUiSettings();

        mMap.setMyLocationEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Log.v(TAG, "onMyLocationButtonCLick");
                if (mLastKnownLocation == null) {
                    if (lastUnsafeLocation == null) {
                        Toast.makeText(getActivity(), "Waiting for location...", Toast.LENGTH_LONG).show();
                        return true;
                    } else {
                        // Uncertain last known position
                        Toast.makeText(getActivity(), "Found helper location...", Toast.LENGTH_LONG).show();
                        mLastKnownLocation = lastUnsafeLocation;
                    }
                }
                mMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).title("My Home").snippet("Home Address"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),
                        mLastKnownLocation.getLongitude()), 10.0f));
                return false;
            }
        });

//        mMap.addMarker(new MarkerOptions().position(new LatLng(lastUnsafeLocation.getLatitude(), lastUnsafeLocation.getLongitude())).title("My Home").snippet("Home Address"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastUnsafeLocation.getLatitude(), lastUnsafeLocation.getLongitude()), 8.0f));
    }

    public void handleSearchResult(LocationTag location) {
        Log.v(TAG, "searching for " + location);
        if (location == null) {
            return;
        }

        MarkerOptions markerOptions = new MarkerOptions()
                .position(location.getCoordinates())
                .title(location.getSearchParams().getQuery())
                .snippet(location.getAddress().toString().replace(",", "\n"));

        mMap.addMarker(markerOptions);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location.getCoordinates(), 8.0f));
    }
}
