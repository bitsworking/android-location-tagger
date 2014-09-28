package com.bitsworking.starlocations.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bitsworking.starlocations.Constants;
import com.bitsworking.starlocations.MainActivity;
import com.bitsworking.starlocations.R;

/**
 * The List Fragment
 */
public class ListFragment extends Fragment {
    public ListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(Constants.POS_LIST);
    }
}
