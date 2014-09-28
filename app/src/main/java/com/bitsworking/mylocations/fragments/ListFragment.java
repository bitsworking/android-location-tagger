package com.bitsworking.mylocations.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bitsworking.mylocations.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class ListFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

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
//            ((MainActivity) activity).onSectionAttached(
//                    getArguments().getInt(ARG_SECTION_NUMBER));
    }
}
