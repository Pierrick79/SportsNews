/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.sportsnews;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sportsnews.data.RssContract;

import java.io.IOException;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create the detail fragment and add it to the activity
        // using a fragment transaction.
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.news_detail_container, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private static final String FORECAST_SHARE_HASHTAG = " #SportsNews";
        private static final int DETAIL_LOADER = 0;
        static final String DETAIL_URI = "URI";

        private static final String[] FORECAST_COLUMNS = {
                RssContract.NewsEntry.TABLE_NAME + "." + RssContract.NewsEntry._ID,
                RssContract.NewsEntry.COLUMN_DATE,
                RssContract.NewsEntry.COLUMN_SHORT_DESC,
                RssContract.NewsEntry.COLUMN_URL_IMG,
                RssContract.NewsEntry.COLUMN_LINK,
                RssContract.NewsEntry.COLUMN_TITLE
        };

        private String mForecast;
        private ShareActionProvider mShareActionProvider;
        private Uri mUri;

        private TextView mDescView;
        private TextView mSportsView;
        private TextView mDateView;
        private TextView mTitleView;
        private static ImageView mImgView;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Bundle arguments = getArguments();
            if(arguments != null){
                mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            }

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            mDescView = (TextView) rootView.findViewById(R.id.detail_desc);
            mSportsView = (TextView) rootView.findViewById(R.id.detail_sport);
            //mDateView = (TextView) rootView.findViewById(R.id.detail_date);
            mTitleView = (TextView) rootView.findViewById(R.id.detail_title);
            mImgView = (ImageView) rootView.findViewById(R.id.detail_image);

            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);
            // Retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);
            // Get the provider and hold onto it to set/change the share intent.
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            // If onLoadFinished happens before this, we can go ahead and set the share intent now.
            if(mForecast != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
            return shareIntent;
        }

        @Override
        public void onActivityCreated(Bundle saveInstanceState) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            super.onActivityCreated(saveInstanceState);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if(null != mUri){
                // Now create and return a CursorLoader that will take care of
                // creating a Cursor for the data being displayed.
                return new CursorLoader(getActivity(), mUri, FORECAST_COLUMNS, null, null, null);
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (!data.moveToFirst()) {
                return;
            }
            // Read title from cursor and update views
            String Title = data.getString(data.getColumnIndex(RssContract.NewsEntry.COLUMN_TITLE));

            mSportsView.setText(Utility.getPreferredSports(getActivity()));
            mTitleView.setText(Title);

            // Read ImgUrl from cursor and load the image
            String ImgUrl = data.getString(data.getColumnIndex(RssContract.NewsEntry.COLUMN_URL_IMG));

            LoadImage loadImage = new LoadImage();
            loadImage.execute(ImgUrl);

            // Read Desc and Link from cursor and update views
            String Desc = data.getString(data.getColumnIndex(RssContract.NewsEntry.COLUMN_SHORT_DESC));
            String Link = data.getString(data.getColumnIndex(RssContract.NewsEntry.COLUMN_LINK));

            mDescView.setText(Desc);

            //add link to text and add Read More text
            mDescView.append(Html.fromHtml("<a href=" + Link + ">Lire la Suite</a>"));
            mDescView.setMovementMethod(LinkMovementMethod.getInstance());

            mForecast = Title + " (" + Link + ")";

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

        void onSportsChanged(String newSports) {
            // replace the uri, since the sports has changed
            Uri uri = mUri;
            if(null != uri){
                long date = RssContract.NewsEntry.getDateFromUri(uri);
                Uri updatedUri = RssContract.NewsEntry.buildNewsSportsWithDate(newSports, date);
                mUri = updatedUri;
                getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader){}


        //Add AsyncTask to LoadImage on Detail
        public static class LoadImage extends AsyncTask<String, Void, Drawable> {
            private String url;

            @Override
            protected Drawable doInBackground(String... params) {
                if (params.length == 0) {
                    return null;
                }

                url = params[0];

                Drawable Img = null;

                try {
                    Img = Utility.drawableFromUrl(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return Img;
            }

            @Override
            protected void onPostExecute(Drawable result){
                if(result != null){
                    mImgView.setImageDrawable(result);
                }
            }
        }
    }
}

