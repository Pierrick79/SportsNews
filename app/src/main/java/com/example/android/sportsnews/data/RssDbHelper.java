package com.example.android.sportsnews.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.sportsnews.data.RssContract.SportsEntry;
import com.example.android.sportsnews.data.RssContract.NewsEntry;

/**
 * Created by Pierrick on 09/04/2015.
 */
public class RssDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "news.db";

    public RssDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold sports.  A sports consists of the string supplied in the
        // sports setting and the sports name
        final String SQL_CREATE_SPORTS_TABLE = "CREATE TABLE " + SportsEntry.TABLE_NAME + " (" +
                SportsEntry._ID + " INTEGER PRIMARY KEY," +
                SportsEntry.COLUMN_SPO_NAME + " TEXT NOT NULL, " +
                SportsEntry.COLUMN_SPO_SETTING + " TEXT UNIQUE NOT NULL );";

        final String SQL_CREATE_NEWS_TABLE = "CREATE TABLE " + NewsEntry.TABLE_NAME + " (" +
                NewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                // the ID of the sports entry associated with this news data
                NewsEntry.COLUMN_SPO_KEY + " INTEGER NOT NULL, " +
                NewsEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                NewsEntry.COLUMN_TIME + " TEXT NOT NULL, " +
                NewsEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +
                NewsEntry.COLUMN_LINK + " TEXT NOT NULL, " +
                NewsEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                NewsEntry.COLUMN_URL_IMG + " TEXT NOT NULL, " +
                // Set up the sports column as a foreign key to news table.
                " FOREIGN KEY (" + NewsEntry.COLUMN_SPO_KEY + ") REFERENCES " +
                SportsEntry.TABLE_NAME + " (" + SportsEntry._ID + "), " +
                // To assure the application have just one news entry per date
                // per sports, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + NewsEntry.COLUMN_SPO_KEY + ", " + NewsEntry.COLUMN_DATE + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_NEWS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SPORTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST " + SportsEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST " + NewsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
