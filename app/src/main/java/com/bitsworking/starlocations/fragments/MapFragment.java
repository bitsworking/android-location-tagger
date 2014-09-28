package com.bitsworking.starlocations.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bitsworking.starlocations.Constants;
import com.bitsworking.starlocations.MainActivity;
import com.bitsworking.starlocations.R;

/**
 * The Map Fragment
 */
public class MapFragment extends Fragment {
    public MapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(Constants.POS_MAP);
    }

    public void updateLocation(Location location) {

    }
}
