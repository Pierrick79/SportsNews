package com.example.android.sportsnews.data;

/**
 * Created by Pierrick on 10/04/2015.
 */
import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class RssProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private RssDbHelper mOpenHelper;

    static final int NEWS = 100;
    static final int NEWS_WITH_SPORTS = 101;
    static final int NEWS_WITH_SPORTS_AND_DATE = 102;
    static final int SPORTS = 300;

    private static final SQLiteQueryBuilder sNewsBySportsSettingQueryBuilder;

    static{
        sNewsBySportsSettingQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //news INNER JOIN sports ON news.sports_id = sports._id
        sNewsBySportsSettingQueryBuilder.setTables(
                RssContract.NewsEntry.TABLE_NAME + " INNER JOIN " +
                        RssContract.SportsEntry.TABLE_NAME +
                        " ON " + RssContract.NewsEntry.TABLE_NAME +
                        "." + RssContract.NewsEntry.COLUMN_SPO_KEY +
                        " = " + RssContract.SportsEntry.TABLE_NAME +
                        "." + RssContract.SportsEntry._ID);
    }

    //sports.sports_setting = ?
    private static final String sSportsSettingSelection =
            RssContract.SportsEntry.TABLE_NAME+
                    "." + RssContract.SportsEntry.COLUMN_SPO_SETTING + " = ? ";

    //sports.sports_setting = ? AND date >= ?
    private static final String sSportsSettingWithStartDateSelection =
            RssContract.SportsEntry.TABLE_NAME+
                    "." + RssContract.SportsEntry.COLUMN_SPO_SETTING + " = ? AND " +
                    RssContract.NewsEntry.COLUMN_DATE + " >= ? ";

    //sports.sports_setting = ? AND date = ?
    private static final String sSportsSettingAndDaySelection =
            RssContract.SportsEntry.TABLE_NAME +
                    "." + RssContract.SportsEntry.COLUMN_SPO_SETTING + " = ? AND " +
                    RssContract.NewsEntry.COLUMN_DATE + " = ? ";

    private Cursor getNewsBySportsSetting(Uri uri, String[] projection, String sortOrder) {
        String sportsSetting = RssContract.NewsEntry.getSportsSettingFromUri(uri);
        long startDate = RssContract.NewsEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = sSportsSettingSelection;
            selectionArgs = new String[]{sportsSetting};
        } else {
            selectionArgs = new String[]{sportsSetting, Long.toString(startDate)};
            selection = sSportsSettingWithStartDateSelection;
        }

        return sNewsBySportsSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getNewsBySportsSettingAndDate(
            Uri uri, String[] projection, String sortOrder) {
        String sportsSetting = RssContract.NewsEntry.getSportsSettingFromUri(uri);
        long date = RssContract.NewsEntry.getDateFromUri(uri);

        return sNewsBySportsSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sSportsSettingAndDaySelection,
                new String[]{sportsSetting, Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RssContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, RssContract.PATH_NEWS, NEWS);
        matcher.addURI(authority, RssContract.PATH_NEWS + "/*", NEWS_WITH_SPORTS);
        matcher.addURI(authority, RssContract.PATH_NEWS + "/*/#", NEWS_WITH_SPORTS_AND_DATE);

        matcher.addURI(authority, RssContract.PATH_SPORTS, SPORTS);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new RssDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case NEWS_WITH_SPORTS_AND_DATE:
                return RssContract.NewsEntry.CONTENT_ITEM_TYPE;
            case NEWS_WITH_SPORTS:
                return RssContract.NewsEntry.CONTENT_TYPE;
            case NEWS:
                return RssContract.NewsEntry.CONTENT_TYPE;
            case SPORTS:
                return RssContract.SportsEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "news/*/*"
            case NEWS_WITH_SPORTS_AND_DATE:
            {
                retCursor = getNewsBySportsSettingAndDate(uri, projection, sortOrder);
                break;
            }
            // "news/*"
            case NEWS_WITH_SPORTS: {
                retCursor = getNewsBySportsSetting(uri, projection, sortOrder);
                break;
            }
            // "news"
            case NEWS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RssContract.NewsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "sports"
            case SPORTS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RssContract.SportsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case NEWS: {
                normalizeDate(values);
                long _id = db.insert(RssContract.NewsEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = RssContract.NewsEntry.buildNewsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case SPORTS: {
                long _id = db.insert(RssContract.SportsEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = RssContract.SportsEntry.buildSportsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (null == selection) selection = "1";
        switch (match) {
            case NEWS:
                rowsDeleted = db.delete(RssContract.NewsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SPORTS:
                rowsDeleted = db.delete(RssContract.SportsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(RssContract.NewsEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(RssContract.NewsEntry.COLUMN_DATE);
            values.put(RssContract.NewsEntry.COLUMN_DATE, RssContract.normalizeDate(dateValue));
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case NEWS:
                normalizeDate(values);
                rowsUpdated = db.update(RssContract.NewsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case SPORTS:
                rowsUpdated = db.update(RssContract.SportsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NEWS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(RssContract.NewsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
