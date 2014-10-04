package com.bitsworking.starlocations.fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.widget.Toast;

import com.bitsworking.starlocations.Constants;
import com.bitsworking.starlocations.MainActivity;
import com.bitsworking.starlocations.R;
import com.bitsworking.starlocations.contentproviders.MySearchRecentSuggestionsProvider;

public class SettingsFragment extends PreferenceFragment {
    final String TAG = "SettingsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // DB: Show
        ((Preference) findPreference("pref_key_db_show")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((MainActivity) getActivity()).setFragment(Constants.FRAGMENT_DBVIEWER);
                return false;
            }
        });

        // DB: Backup
        ((Preference) findPreference("pref_key_db_backup")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String fn = ((MainActivity) getActivity()).backupDatabase();
                Toast.makeText(getActivity(), "Database backed up to SDCard/" + fn, Toast.LENGTH_LONG).show();
                return false;
            }
        });

        // DB: Restore
        ((Preference) findPreference("pref_key_db_restore")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((MainActivity) getActivity()).restoreDatabase(false);
                return false;
            }
        });

        // DB: Import
        ((Preference) findPreference("pref_key_db_import")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((MainActivity) getActivity()).restoreDatabase(true);
                return false;
            }
        });

        // DB: Delete
        ((Preference) findPreference("pref_key_db_delete")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((MainActivity) getActivity()).askToDeleteDatabase();
                return false;
            }
        });

        // Search: Clear autocomplete
        ((Preference) findPreference("pref_key_search_clear_autocomplete")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                        MySearchRecentSuggestionsProvider.AUTHORITY, MySearchRecentSuggestionsProvider.MODE);
                suggestions.clearHistory();
                Toast.makeText(getActivity(), "Search history cleared", Toast.LENGTH_LONG).show();
                return false;
            }
        });
    }
}
