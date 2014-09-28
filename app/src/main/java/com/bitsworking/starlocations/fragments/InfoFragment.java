package com.bitsworking.starlocations.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bitsworking.starlocations.Constants;
import com.bitsworking.starlocations.MainActivity;
import com.bitsworking.starlocations.R;

/**
 * The Info Fragment
 */
public class InfoFragment extends Fragment {
    public TextView tvLat;
    public TextView tvLng;
    public TextView tvAcc;
    public TextView tvAlt;

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
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        updateLocation(((MainActivity) activity).getLocation());
        ((MainActivity) activity).onSectionAttached(Constants.POS_INFO);
    }

    public void updateLocation(Location location) {
        tvLat.setText(String.valueOf(location.getLatitude()));
        tvLng.setText(String.valueOf(location.getLongitude()));
        tvAcc.setText(String.valueOf(location.getAccuracy()));
        tvAlt.setText(String.valueOf(location.getAltitude()));
    }
}
