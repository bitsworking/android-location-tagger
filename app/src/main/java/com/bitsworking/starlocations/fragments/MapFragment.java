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
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bitsworking.starlocations.LocationTag;
import com.bitsworking.starlocations.MainActivity;
import com.bitsworking.starlocations.R;
import com.bitsworking.starlocations.Tools;
import com.bitsworking.starlocations.exceptions.InvalidLocationException;
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

    private Handler mHandler = new Handler();

    private LocationTag overlayLocationTag = null;

    private Intent shareIntent = null;

    // <marker.id, tag>
    private HashMap<String, LocationTag> markerLocationTags = new HashMap<String, LocationTag>();

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
//        TODO: get all tags, not only saved ones
//        mLocationTagDatabase = ((MainActivity) getActivity()).getLocationTagDatabase();

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
                // Update title info and replace marker
                overlayLocationTag.title = ((EditText) rlOverlay.findViewById(R.id.etTitle)).getText().toString();

                // Save to database
                ((MainActivity) getActivity()).saveLocationTag(overlayLocationTag);

                removeMarker(overlayLocationTag.mapMarker);
                addMarker(overlayLocationTag, true, null, true);

                // Needs to be called last, because it removes the overlayLocationTag
                closeOverlay();
            }
        });


        // Remove button
        ((Button) rlOverlay.findViewById(R.id.btnRemove)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "XXX remove tag click " + overlayLocationTag);
                removeLocationTag(overlayLocationTag);
                closeOverlay();
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

                addMarker(new LocationTag(new LatLng(
                        lastKnownLocation.getLatitude(),
                        lastKnownLocation.getLongitude())), true, 12, true);

                return false;
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                addMarker(new LocationTag(latLng), true, 10, true);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                closeOverlay();
                return false;
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
                LocationTag tag = markerLocationTags.get(marker.getId());

                // Check if we dragged a saved marker
                if (((MainActivity) getActivity()).isSavedLocationTag(tag)) {
                    ((MainActivity) getActivity()).askToMoveSavedTag(tag, marker.getPosition());
                } else {
                    // Else add/move this temporary marker
                    addMarker(new LocationTag(marker.getPosition()), true, 10, true);
                }
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
//                marker.hideInfoWindow();
                showOverlay(markerLocationTags.get(marker.getId()));
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
        for (LocationTag tag : ((MainActivity) getActivity()).getAllLocationTags()) {
            Log.v(TAG, "Got saved tag: " + tag.toString());
            addMarker(tag);
        }

        // Initial positining
        Location lastKnownLocation = ((MainActivity) getActivity()).getLocation();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), 8.0f));
    }

//    public Marker addTempMarker(LocationTag tag) {
//        return addTempMarker(tag, mMap.getCameraPosition().zoom);
//    }

    public void geocodeNewMarker(final LocationTag tag) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    tag.address = Tools.geocodeCoordinatesToAddress(getActivity(), tag.getLatLng());
                    Log.v(TAG, "NEW ADDRESS: " + tag.address.toString());

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Upadte overlay, if still about this marker
                            if (isOverlayVisible() && overlayLocationTag.uid == tag.uid) {
                                ((TextView) rlOverlay.findViewById(R.id.tvAddress)).setText(tag.getAddressInfo(false));
                            }

                            // Update the marker info window with address, if its already opened
                            if (tag.mapMarker.isInfoWindowShown()) {
                                tag.mapMarker.showInfoWindow();
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

    public void showOverlay(LocationTag tag) {
        Log.v(TAG, "showOverlay: " + tag.toString());
        overlayLocationTag = tag;

        ((TextView) rlOverlay.findViewById(R.id.tvLatitude)).setText(String.valueOf(tag.getLatLng().latitude));
        ((TextView) rlOverlay.findViewById(R.id.tvLongitude)).setText(String.valueOf(tag.getLatLng().longitude));
        ((EditText) rlOverlay.findViewById(R.id.etTitle)).setText(tag.title);

        TextView tvAddress = (TextView) rlOverlay.findViewById(R.id.tvAddress);
        if (tag.address == null) {
            tvAddress.setText("");
        } else {
            tvAddress.setText(tag.getAddressInfo(false));
        }

        rlOverlay.setVisibility(View.VISIBLE);

        // Get the intent again, perhaps we did geocoding or stuff
        shareIntent = tag.getShareIntent();
        ((MainActivity) getActivity()).setShareActionIntent(shareIntent);
    }

    public void closeOverlay() {
        if (!isOverlayVisible()) return;
        overlayLocationTag = null;
        rlOverlay.setVisibility(View.GONE);
        shareIntent = null;
        ((MainActivity) getActivity()).setShareActionIntent(shareIntent);
    }

    public boolean isOverlayVisible() {
        return rlOverlay.getVisibility() == View.VISIBLE;
    }

    public void addMarker(LocationTag tag) {
        addMarker(tag, false, null, false);
    }

    public void addMarker(LocationTag tag, boolean showInfoWindow,
                          Integer animateCameraWithZoom, boolean setShareIntent) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(tag.getLatLng());
        markerOptions.draggable(true);

        // Set color
        if (tag.savedTimestamp != null) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        }

        tag.mapMarker = mMap.addMarker(markerOptions);
        markerLocationTags.put(tag.mapMarker.getId(), tag);
        ((MainActivity) getActivity()).addTempLocationTag(tag);

        if (showInfoWindow) {
            tag.mapMarker.showInfoWindow();
        }

        if (animateCameraWithZoom != null) {
            // Animate to marker
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tag.getLatLng(), animateCameraWithZoom));
        }

        if (tag.address == null) {
            // Geocode: get address from coordinates
            geocodeNewMarker(tag);
        }

        if (setShareIntent) {
            shareIntent = tag.getShareIntent();
            ((MainActivity) getActivity()).setShareActionIntent(shareIntent);
        }

    }

    // Remove a locationtag / its marker
    public void removeLocationTag(LocationTag tag) {
        if (((MainActivity) getActivity()).isSavedLocationTag(tag)) {
            // If its a saved marker, ask whether really to delete
            ((MainActivity) getActivity()).askToDeleteTag(tag);

        } else {
            // If unsaved, then just remove
            ((MainActivity) getActivity()).removeTempLocationTag(tag);
            removeMarker(tag.mapMarker);
        }
    }

    public void removeMarker(Marker marker) {
        if (marker != null) {
            markerLocationTags.remove(marker.getId());
            marker.remove();
        }
    }
}
