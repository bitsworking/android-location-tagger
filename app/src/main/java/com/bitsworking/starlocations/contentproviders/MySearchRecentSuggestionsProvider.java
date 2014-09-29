package com.bitsworking.starlocations.contentproviders;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by Chris Hager <chris@linuxuser.at> on 29/09/14.
 */
public class MySearchRecentSuggestionsProvider extends android.content.SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.bitsworking.starlocations.contentproviders.MySearchRecentSuggestionsProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public MySearchRecentSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}