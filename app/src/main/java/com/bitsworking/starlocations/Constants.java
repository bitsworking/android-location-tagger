package com.bitsworking.starlocations;

public interface Constants {
    static final int FRAGMENT_MAP = 0;
    static final int FRAGMENT_LIST = 1;
    static final int FRAGMENT_SETTINGS = 2;
    static final int FRAGMENT_DBVIEWER = 3;

    static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    static final String PLACES_API_TYPE_AUTOCOMPLETE = "/autocomplete";
    static final String PLACES_API_OUT_JSON = "/json";
    static final String PLACES_API_KEY = "AIzaSyDxQUIFKrAxLLb-P_svIFIWd-IjWmvyYNs";

    static final String SD_DIRECTORY = "/Locations";
    static final String DB_FILE_EXT = ".json";
    static final String DB_FILE = "locations";
    static final String DB_FILE_BACKUP_PREFIX = "locations-backup_";
}