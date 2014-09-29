package com.bitsworking.starlocations.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.bitsworking.starlocations.Constants;
import com.bitsworking.starlocations.MainActivity;
import com.bitsworking.starlocations.R;

/**
 * The Info Fragment
 */
public class InfoFragment extends Fragment {
    private TextView tvLat;
    private TextView tvLng;
    private TextView tvAcc;
    private TextView tvAlt;
    private Button btnSave;
    private Button btnMap;

    private Location mLastKnownLocation;
    private Location currentLocation;

    private boolean showLiveUpdates = false;

    public InfoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_info, container, false);

        tvLat = (TextView) rootView.findViewById(R.id.tvLat);
        tvLng = (TextView) rootView.findViewById(R.id.tvLng);
        tvAcc = (TextView) rootView.findViewById(R.id.tvAcc);
        tvAlt = (TextView) rootView.findViewById(R.id.tvAlt);

        btnSave = (Button) rootView.findViewById(R.id.btnSave);
        btnMap = (Button) rootView.findViewById(R.id.btnMap);

        // Attach checkbox handler
        ((CheckBox) rootView.findViewById(R.id.cb_live_updates)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showLiveUpdates = isChecked;
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        ((MainActivity) activity).onSectionAttached(Constants.FRAGMENT_INFO);
    }

    public void useCurrentLocation(Location location) {
        currentLocation = location;
        if (currentLocation == null) {
            return;
        }
        btnSave.setEnabled(true);
        btnMap.setEnabled(true);
        tvLat.setText(String.valueOf(location.getLatitude()));
        tvLng.setText(String.valueOf(location.getLongitude()));
        tvAcc.setText(String.valueOf(location.getAccuracy()));
        tvAlt.setText(String.valueOf(location.getAltitude()));
    }

    public void newLastKnownLocation(Location location) {
        mLastKnownLocation = location;
        if (showLiveUpdates) {
            useCurrentLocation(location);
        }
    }
}
