package com.bitsworking.starlocations.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bitsworking.starlocations.LocationTag;
import com.bitsworking.starlocations.MainActivity;
import com.bitsworking.starlocations.R;
import com.bitsworking.starlocations.Tools;
import com.bitsworking.starlocations.exceptions.InvalidLocationException;
import com.bitsworking.starlocations.jsondb.LocationTagDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;

/**
 * The Map Fragment
 */
public class MapFragment extends Fragment {
    private final String TAG = "MapFragment";

    private MapView mMapView;
    private static GoogleMap mMap;
    private RelativeLayout rlOverlay;

//    private Location mLastKnownLocation;
    private Handler mHandler = new Handler();

    private Marker lastTempMarker;
    private HashMap<String, LocationTag> markerLocationTags = new HashMap<String, LocationTag>();

    private LocationTag overlayLocationTag = null;
    private LocationTagDatabase mLocationTagDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        // Acquire reference to location tag database
        mLocationTagDatabase = ((MainActivity) getActivity()).getLocationTagDatabase();

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        Log.v(TAG, "Setting up mMapView");
        mMapView.onCreate(savedInstanceState);

        mMap = mMapView.getMap();
        MapsInitializer.initialize(this.getActivity());

        setUpMap();

        // Handle overlay touch
        rlOverlay = (RelativeLayout) rootView.findViewById(R.id.rlOverlay);
        rlOverlay.setVisibility(View.GONE);
        rlOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Stop from propagating to mapview
                return true;
            }
        });

        // Close overlay
        ((TextView) rlOverlay.findViewById(R.id.tvClose)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeOverlay();
            }
        });

        // Share button
        ((Button) rlOverlay.findViewById(R.id.btnShare)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(overlayLocationTag.getShareIntent());
            }
        });

        // Save button
        ((Button) rlOverlay.findViewById(R.id.btnSave)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).saveLocationTag(overlayLocationTag);
                addMarker(overlayLocationTag);
                delTempMarker();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.v(TAG, "onAttach");
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
            public void onMarkerDragStart(Marker marker) {
                marker.hideInfoWindow();
            }

            @Override
            public void onMarkerDrag(Marker marker) {}

            @Override
            public void onMarkerDragEnd(Marker marker) {
                addTempMarker(new LocationTag(marker.getPosition()));
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
//                marker.hideInfoWindow();
                showLocationOverlay(markerLocationTags.get(marker.getId()));
            }
        });

        GoogleMap.InfoWindowAdapter customInfoWindowAdapter = new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                TextView tv = new TextView(getActivity());
                tv.setPadding(10, 10, 10, 10);
                tv.setSingleLine(false);

                if (markerLocationTags.containsKey(marker.getId())) {
                    LocationTag tag = markerLocationTags.get(marker.getId());
                    tv.setText(tag.getMarkerSnippet());
                }

                return tv;
            }
        };

        mMap.setInfoWindowAdapter(customInfoWindowAdapter);

        // Add saved markers
        for (LocationTag tag : mLocationTagDatabase.getAll()) {
            Log.v(TAG, "Got saved tag: " + tag.toString());
            addMarker(tag);
        }

        // Initial positining
        Location lastKnownLocation = ((MainActivity) getActivity()).getLocation();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), 8.0f));
    }

    public void addTempMarker(LocationTag tag) {
        addTempMarker(tag, mMap.getCameraPosition().zoom);
    }

    public void delTempMarker() {
        if (lastTempMarker != null) {
            markerLocationTags.remove(lastTempMarker.getId());
            lastTempMarker.remove();
        }
    }

    public void addTempMarker(final LocationTag tag, float zoom) {
        delTempMarker();
        closeOverlay();

        // Build new marker
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(tag.getLatLng());
        markerOptions.draggable(true);

        // Add marker, show info window
        lastTempMarker = mMap.addMarker(markerOptions);
        markerLocationTags.put(lastTempMarker.getId(), tag);

        // Animate to marker
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tag.getLatLng(), zoom));

        lastTempMarker.showInfoWindow();
        ((MainActivity) getActivity()).setShareActionIntent(tag.getShareIntent());

        if (tag.address == null) {
            // Geocode: get address from coordinates
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        tag.address = Tools.geocodeCoordinatesToAddress(getActivity(), tag.getLatLng());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.v(TAG, "NEW ADDRESS: " + tag.address.toString());
                                ((TextView) rlOverlay.findViewById(R.id.tvAddress)).setText(tag.getAddressInfo(false));

                                // Update Share Intent
                                ((MainActivity) getActivity()).setShareActionIntent(tag.getShareIntent());

                                // Update the marker info window with address, if its already opened
                                if (lastTempMarker.isInfoWindowShown()) {
                                    lastTempMarker.showInfoWindow();
                                }
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InvalidLocationException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }
    }

    private void showLocationOverlay(LocationTag tag) {
        overlayLocationTag = tag;

        ((TextView) rlOverlay.findViewById(R.id.tvLatitude)).setText(String.valueOf(tag.getLatLng().latitude));
        ((TextView) rlOverlay.findViewById(R.id.tvLongitude)).setText(String.valueOf(tag.getLatLng().longitude));

        TextView tvAddress = (TextView) rlOverlay.findViewById(R.id.tvAddress);
        if (tag.address == null) {
            tvAddress.setText("");
        } else {
            tvAddress.setText(tag.getAddressInfo(false));
        }

        rlOverlay.setVisibility(View.VISIBLE);

        // Get the intent again, perhaps we did geocoding or stuff
        ((MainActivity) getActivity()).setShareActionIntent(tag.getShareIntent());
    }

    public void closeOverlay() {
        if (!isOverlayVisible()) return;
        overlayLocationTag = null;
        rlOverlay.setVisibility(View.GONE);

        // Show info window if marker, else remove share intent
//        if (lastTempMarker != null) {
//            lastTempMarker.showInfoWindow();
//        }
    }

    public boolean isOverlayVisible() {
        return rlOverlay.getVisibility() == View.VISIBLE;
    }

    public void addMarker(final LocationTag tag) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(tag.getLatLng());
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        Marker marker = mMap.addMarker(markerOptions);
        markerLocationTags.put(marker.getId(), tag);
    }
}
