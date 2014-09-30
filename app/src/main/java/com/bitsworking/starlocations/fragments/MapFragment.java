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
import android.widget.TextView;
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

import java.util.HashMap;

/**
 * The Map Fragment
 */
public class MapFragment extends Fragment {
    private final String TAG = "MapFragment";

    private MapView mMapView;
    private static GoogleMap mMap;

//    private Location mLastKnownLocation;
    private Handler mHandler = new Handler();

    private Marker lastTempMarker;
    private HashMap<String, LocationTag> markerLocationTags = new HashMap<String, LocationTag>();

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

    private void setUpMap() {
        // My location button overlay
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Log.v(TAG, "onMyLocationButtonCLick");
                Location lastKnownLocation = ((MainActivity) getActivity()).getLocation();
                if (lastKnownLocation == null) {
                    Toast.makeText(getActivity(), "Waiting for location...", Toast.LENGTH_LONG).show();
                    return false;
                }

                addTempMarker(new LocationTag(new LatLng(
                        lastKnownLocation.getLatitude(),
                        lastKnownLocation.getLongitude())), 12);

                return false;
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                addTempMarker(new LocationTag(latLng));
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}

            @Override
            public void onMarkerDrag(Marker marker) {}

            @Override
            public void onMarkerDragEnd(Marker marker) {
//                Toast.makeText(getActivity(), "Marker drag end", Toast.LENGTH_LONG).show();
                addTempMarker(new LocationTag(marker.getPosition()));
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Toast.makeText(getActivity(), "Info Window Clicked", Toast.LENGTH_LONG).show();
            }
        });

        GoogleMap.InfoWindowAdapter customInfoWindowAdapter = new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LocationTag tag = markerLocationTags.get(marker.getId());

                TextView tv = new TextView(getActivity());
                tv.setPadding(10, 10, 10, 10);
                tv.setSingleLine(false);
                tv.setText(tag.getMarkerSnippet());
                return tv;
            }
        };

        mMap.setInfoWindowAdapter(customInfoWindowAdapter);

        // Initial positining
        Location lastKnownLocation = ((MainActivity) getActivity()).getLocation();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), 8.0f));
    }

    public void addTempMarker(LocationTag tag) {
        addTempMarker(tag, mMap.getCameraPosition().zoom);
    }

    public void addTempMarker(LocationTag tag, float zoom) {
        // Remove old marker
        if (lastTempMarker != null) {
            markerLocationTags.remove(lastTempMarker.getId());
            lastTempMarker.remove();
        }

        // Build new marker
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(tag.latLng);
        markerOptions.draggable(true);

        // Add marker, show info window
        lastTempMarker = mMap.addMarker(markerOptions);
        markerLocationTags.put(lastTempMarker.getId(), tag);

        // Animate to marker
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tag.latLng, zoom));

        lastTempMarker.showInfoWindow();
    }
}
