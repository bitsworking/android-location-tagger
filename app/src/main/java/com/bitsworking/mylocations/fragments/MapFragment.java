package com.bitsworking.mylocations.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bitsworking.mylocations.Constants;
import com.bitsworking.mylocations.MainActivity;
import com.bitsworking.mylocations.R;

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
}
