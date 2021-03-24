package com.hayvn.hayvnapp.Constant;

public class SharedprefConstants {
    public static final String PATIENT_PREFIX = "patient_prefix";
    public static final String TIME_LAST_SYNC = "time_last_sync";
    public static final String KEEP_PAST_PREF = "keep_past_x_days_of_data";
    public static final String KEEP_PAST_PREF_v01 = "1 day";
    public static final String KEEP_PAST_PREF_v02 = "1 week";
    public static final String KEEP_PAST_PREF_v03 = "2 weeks";
    public static final String KEEP_PAST_PREF_v04 = "1 month";
    public static final String KEEP_PAST_PREF_v05 = "3 months";
    public static final String KEEP_PAST_PREF_DEFAULT = KEEP_PAST_PREF_v02;
    public static final String[] KEEP_PAST_PREF_ARR = {
            KEEP_PAST_PREF_v01, KEEP_PAST_PREF_v02,KEEP_PAST_PREF_v03,KEEP_PAST_PREF_v04,KEEP_PAST_PREF_v05
    };
    public static final int[] KEEP_PAST_PREF_ARR_INT = {
            2, 8, 15, 32, 95
    };


    public static final String TIME_LAST_SYNC_START_CASES = "time_last_sync_start_cases";
    public static final String TIME_LAST_SYNC_START_STORIES = "time_last_sync_start_stories";
    public static final String TIME_LAST_SYNC_START_FILES = "time_last_sync_start_files";
    public static final String SYNCED_ALL_ALREADY = "SYNCED_ALL_ALREADY";

    public static final String NUMBER_OF_SHARDS = "number_of_shards";
    public static final String FOR_WELCOME_DIALOG = "LOGIN_OR_REGISTER";
    public static final String FOR_WELCOME_DIALOG_VAL_LOGIN = "login";
    public static final String MEDIA_NOT_SYNCED = "mediaNotSynced";


    public static final String DEFAULT_PATIENT_PREFIX = "WRN";

    public static final String WELCOME_DIALOG_AFTER_REGISTER = "WELCOME_DIALOG_AFTER_REGISTER";
    public static final String WELCOME_DIALOG_AFTER_LOGIN = "WELCOME_DIALOG_AFTER_LOGIN";
    public static final String WELCOME_DIALOG_NOT_NEEDED = "WELCOME_DIALOG_NOT_NEEDED";


    public static final String COUNTRY_LIST_UPD_TIME = "COUNTRY_LIST_UPD_TIME";
    public static final String COUNTRY_LIST_FULL = "COUNTRY_LIST_FULL";


    public static final String ENTRYTITLE_LIST_UPD_TIME = "ENTRYTITLE_LIST_UPD_TIME";
    public static final String ENTRYTITLE_LIST_FULL = "ENTRYTITLE_LIST_FULL";

    public static final String TIME_LAST_PIN = "TIME_LAST_PIN";
    public static final String PIN_ATTEMPTS_TOO_MANY = "PIN_ATTEMPTS_TOO_MANY";
    public static final String YES_PIN_ATTEMPTS_TOO_MANY = "YES_PIN_ATTEMPTS_TOO_MANY";

    public static final String FAVOURITES_LIST = "FAVOURITES_LIST";
    public static final String REMOVED_FROM_FAVOURITES_LIST = "REMOVED_FROM_FAVOURITES_LIST";
}
