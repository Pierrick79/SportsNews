package com.example.android.sportsnews;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Log;

import com.example.android.sportsnews.data.RssContract;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Vector;

/**
 * Created by Pierrick on 08/04/2015.
 */
public class RssService extends IntentService {

    public static final String ITEMS = "items";
    public static final String RECEIVER = "receiver";
    public String param;
    private final String LOG_TAG = RssService.class.getSimpleName();

    public RssService() {
        super("RssService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Service started");

        param = Utility.getPreferredSports(this);

        //Rss Link
        final String RSS_LINK = "http://www.lequipe.fr/rss/actu_rss_" + param + ".xml";

        List<RssItem> rssItems = null;
        //We test Internet Connection
        if (Utility.isOnline(this) == true) {
            try {
                //Recovery Data
                RssParser parser = new RssParser();
                rssItems = parser.parse(getInputStream(RSS_LINK));
                //Add to database
                addDatabase(rssItems);
            } catch (XmlPullParserException e) {
                Log.w(e.getMessage(), e);
            } catch (IOException e) {
                Log.w(e.getMessage(), e);
            }
        };
        Bundle bundle = new Bundle();
        bundle.putSerializable(ITEMS, (Serializable) rssItems);
        ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);
        receiver.send(0, bundle);
    }

    //connection to Link
    public InputStream getInputStream(String link) {
        try {
            URL url = new URL(link);
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            Log.w(LOG_TAG, "Exception while retrieving the input stream", e);
            return null;
        }
    }

    //add database
    void addDatabase(List<RssItem> rssItems){
        Time dayTime = new Time();
        dayTime.setToNow();
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
        dayTime = new Time();

        Vector<ContentValues> cVVector = new Vector<ContentValues>(rssItems.size());

        //cover item and add it to vector
        for (int i = 0; i < rssItems.size(); i++){
            long dateTime = dayTime.setJulianDay(julianStartDay+i);

            long sportsId = addSports(param, param);

            ContentValues newsValues = new ContentValues();

            newsValues.put(RssContract.NewsEntry.COLUMN_SPO_KEY, sportsId);
            newsValues.put(RssContract.NewsEntry.COLUMN_DATE, dateTime);
            newsValues.put(RssContract.NewsEntry.COLUMN_TIME, (rssItems.get(i).getDate()));
            newsValues.put(RssContract.NewsEntry.COLUMN_LINK, (rssItems.get(i).getLink()));
            newsValues.put(RssContract.NewsEntry.COLUMN_SHORT_DESC, (rssItems.get(i).getDescription()));
            newsValues.put(RssContract.NewsEntry.COLUMN_TITLE, (rssItems.get(i).getTitle()));
            newsValues.put(RssContract.NewsEntry.COLUMN_URL_IMG, (rssItems.get(i).getImage()));

            cVVector.add(newsValues);
        }

        if (cVVector.size() > 0){
            //transform vector in array
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            //add to database
            this.getContentResolver().bulkInsert(RssContract.NewsEntry.CONTENT_URI, cvArray);

            this.getContentResolver().delete(RssContract.NewsEntry.CONTENT_URI,
                    RssContract.NewsEntry.COLUMN_DATE + " <= ?",
                    new String[] {Long.toString(dayTime.setJulianDay(julianStartDay-1))});

            //Notify
            notifyNews(cVVector.size());
        }

        String sortOrder = RssContract.NewsEntry.COLUMN_DATE + " ASC";
        Uri newsForLocationUri = RssContract.NewsEntry.buildNewsSportsWithStartDate(
                param,System.currentTimeMillis());

        Cursor cur = this.getContentResolver().query(newsForLocationUri, null, null, null, sortOrder);

        cVVector = new Vector<ContentValues>(cur.getCount());
        if (cur.moveToFirst()){
            do {
                ContentValues cv = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cur, cv);
                cVVector.add(cv);
            } while (cur.moveToNext());
        }

        Log.d(LOG_TAG, "Tache Complete. " + cVVector.size() + " Inserted");
    }


    //add in sports database
    long addSports(String sportsSetting, String sportsName) {
        long sportsId;

        Cursor sportsCursor = this.getContentResolver().query(
                RssContract.SportsEntry.CONTENT_URI,
                new String[]{RssContract.SportsEntry._ID},
                RssContract.SportsEntry.COLUMN_SPO_SETTING + " = ?",
                new String[]{sportsSetting},
                null);

        if (sportsCursor.moveToFirst()) {
            int sportsIdIndex = sportsCursor.getColumnIndex(RssContract.SportsEntry._ID);
            sportsId = sportsCursor.getLong(sportsIdIndex);
        } else {
            ContentValues sportsValues = new ContentValues();

            sportsValues.put(RssContract.SportsEntry.COLUMN_SPO_NAME, sportsName);
            sportsValues.put(RssContract.SportsEntry.COLUMN_SPO_SETTING, sportsSetting);

            Uri insertedUri = this.getContentResolver().insert(
                    RssContract.SportsEntry.CONTENT_URI,
                    sportsValues
            );

            sportsId = ContentUris.parseId(insertedUri);
        }

        sportsCursor.close();
        return sportsId;
    }

    private void notifyNews(int nbLigne){

        //Recovery Notification Manager
        NotificationManager mNotificationManager =
                (NotificationManager)this.getSystemService(this.NOTIFICATION_SERVICE);

        //Creation of Notification. No action on clic.
        NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(this.getString(R.string.app_name))
                .setContentText("Mise à jour de " + nbLigne + " données")
                .setAutoCancel(true);
        mBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(), 0));
        mNotificationManager.notify(3004, mBuilder.build());
    }
}
