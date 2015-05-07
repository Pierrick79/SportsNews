package com.example.android.sportsnews.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by Pierrick on 09/04/2015.
 */
public class RssContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.example.android.sportsnews";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sportsnews.app/news is a valid path for
    // looking at news data. content://com.example.android.sportsnews.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_SPORTS = "sports";
    public static final String PATH_NEWS = "news";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /* Inner class that defines the table contents of the sports table */
    public static final class SportsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SPORTS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SPORTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SPORTS;

        // Table name
        public static final String TABLE_NAME = "sports";

        // The sports setting string is what will be sent to rss
        // as the sports query.
        public static final String COLUMN_SPO_SETTING = "sports_setting";

        // Name of Sports
        public static final String COLUMN_SPO_NAME = "sports_name";

        public static Uri buildSportsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the News table */
    public static final class NewsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_NEWS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWS;

        public static final String TABLE_NAME = "news";

        // Column with the foreign key into the sports table.
        public static final String COLUMN_SPO_KEY = "sports_id";
        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_DATE = "date";
        // Time as returned by RSS
        public static final String COLUMN_TIME = "time";
        // not use
        public static final String COLUMN_NEWS_KEY = "news_id";
        // returned by RSS
        public static final String COLUMN_SHORT_DESC = "short_desc";
        // returned by RSS
        public static final String COLUMN_TITLE = "title";
        // returned by RSS
        public static final String COLUMN_LINK = "link";
        // returned by RSS
        public static final String COLUMN_URL_IMG = "url_img";

        public static Uri buildNewsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildNewsSports(String sportsSetting) {
            return CONTENT_URI.buildUpon().appendPath(sportsSetting).build();
        }

        public static Uri buildNewsSportsWithStartDate(String sportsSetting, long startDate) {
            long normalizedDate = normalizeDate(startDate);
            return CONTENT_URI.buildUpon().appendPath(sportsSetting)
                    .appendQueryParameter(COLUMN_DATE, Long.toString(normalizedDate)).build();
        }

        public static Uri buildNewsSportsWithDate(String sportsSetting, long date) {
            return CONTENT_URI.buildUpon().appendPath(sportsSetting)
                    .appendPath(Long.toString(normalizeDate(date))).build();
        }

        public static String getSportsSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static long getDateFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }

        public static long getStartDateFromUri(Uri uri) {
            String dateString = uri.getQueryParameter(COLUMN_DATE);
            if (null != dateString && dateString.length() > 0)
                return Long.parseLong(dateString);
            else
                return 0;
        }
    }
}
