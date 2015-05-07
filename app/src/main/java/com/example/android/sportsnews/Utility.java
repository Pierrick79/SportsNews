package com.example.android.sportsnews;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Pierrick on 10/04/2015.
 */
public class Utility {
    //Recovery sports preferences
    public static String getPreferredSports(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sport_key),context.getString(R.string.pref_sport_foot));
    }

    //Test connectivity
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    //Read Image from url
    protected static Drawable drawableFromUrl(URL url) throws IOException {
        Drawable ret = null;
        InputStream stream = null;
        try {
                stream = url.openStream();
                ret = Drawable.createFromStream(stream, "src");
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            Log.e("drawableFromUrl","Failed to read image from URL !", e);
            throw new IOException("ImageFromUrlFailure",e);
        } finally {
            if (stream != null) try { stream.close(); } catch (Exception ex) {Log.wtf("drawableFromUrl","Failed to close the stream !",ex);}
        }
        return ret;
    }

    public static Drawable drawableFromUrl(String urls) throws IOException, MalformedURLException {
        URL url = new URL(urls);
        return drawableFromUrl(url);
    }
}
