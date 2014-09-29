package com.bitsworking.starlocations.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bitsworking.starlocations.LocationTag;
import com.bitsworking.starlocations.MainActivity;
import com.bitsworking.starlocations.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * The Map Fragment
 */
public class MapFragment extends Fragment {
    private final String TAG = "MapFragment";

    private MapView mMapView;
    private static GoogleMap mMap;

    private Location mLastKnownLocation;
    private Handler mHandler = new Handler();

    private Marker lastTempMarker;
    private LatLng lastTempLatLng;

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
        mLastKnownLocation = ((MainActivity) getActivity()).getLocation();

        // Initial positining
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), 8.0f));

        // My location button overlay
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Log.v(TAG, "onMyLocationButtonCLick");
                if (mLastKnownLocation == null) {
                    Toast.makeText(getActivity(), "Waiting for location...", Toast.LENGTH_LONG).show();
                    return false;
//                    if (lastUnsafeLocation == null) {
//                    } else {
//                        // Uncertain last known position
//                        Toast.makeText(getActivity(), "Found helper location...", Toast.LENGTH_LONG).show();
//                        mLastKnownLocation = lastUnsafeLocation;
//                    }
                }

                (new Thread() {
                    @Override
                    public void run() {
                        LocationTag tag = LocationTag.fromCoordinates(getActivity(), new LatLng(
                                mLastKnownLocation.getLatitude(),
                                mLastKnownLocation.getLongitude()));
                        handleSearchResult(tag);
                    }
                }).start();

                return false;
            }
        });
    }

    public void handleSearchResult(final LocationTag location) {
        Log.v(TAG, "searching for " + location);
        if (location == null) {
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                lastTempLatLng = location.getCoordinates();

                MarkerOptions lastTemporaryMarkerOptions = new MarkerOptions();
                lastTemporaryMarkerOptions.position(lastTempLatLng);
                lastTemporaryMarkerOptions.title(location.getSearchParams().getQuery());
                lastTemporaryMarkerOptions.snippet(location.getAddress().toString().replace(",", "\n"));


                if (lastTempMarker != null) {
                    lastTempMarker.remove();
                }

                lastTempMarker = mMap.addMarker(lastTemporaryMarkerOptions);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastTempLatLng, mMap.getCameraPosition().zoom));
            }
        });
    }
}
