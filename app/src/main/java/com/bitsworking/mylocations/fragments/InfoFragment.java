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
 * The Info Fragment
 */
public class InfoFragment extends Fragment {
    public InfoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_info, container, false);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(Constants.POS_INFO);
    }
}
