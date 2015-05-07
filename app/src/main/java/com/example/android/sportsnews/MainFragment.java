package com.example.android.sportsnews;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.android.sportsnews.data.RssContract;

/**
 * Created by Pierrick on 07/04/2015.
 */
public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int FORECAST_LOADER = 0;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
        RssContract.NewsEntry.TABLE_NAME + "." + RssContract.NewsEntry._ID,
        RssContract.NewsEntry.COLUMN_DATE,
        RssContract.NewsEntry.COLUMN_SHORT_DESC,
        RssContract.NewsEntry.COLUMN_TIME,
        RssContract.NewsEntry.COLUMN_URL_IMG,
        RssContract.NewsEntry.COLUMN_LINK,
        RssContract.SportsEntry.COLUMN_SPO_SETTING,
        RssContract.SportsEntry.COLUMN_SPO_NAME,
        RssContract.NewsEntry.COLUMN_TITLE
    };

    private ProgressBar progressBar;
    private ListView mListView;
    private View view;
    private RssAdapter mForecastAdapter;
    private int mPosition = ListView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private boolean mUseFirstLayout;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback{
        public void onItemSelected(Uri dateUri);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.mainfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_refresh){
            startService();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // The ForecastAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        if (view == null){
                mForecastAdapter = new RssAdapter(getActivity(), null, 0);
                view = inflater.inflate(R.layout.fragment_main, container, false);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
                // Get a reference to the ListView, and attach this adapter to it.
                mListView = (ListView) view.findViewById(R.id.listview_forecast);
                // We'll call our MainActivity
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // CursorAdapter returns a cursor at the correct position for getItem(), or null
                        // if it cannot seek to that position.
                        Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                        if (cursor != null) {
                            String sportsSetting = Utility.getPreferredSports(getActivity());
                            ((Callback) getActivity()).onItemSelected(RssContract.NewsEntry.buildNewsSportsWithDate(
                                    sportsSetting, cursor.getLong(cursor.getColumnIndex(RssContract.NewsEntry.COLUMN_DATE))));
                        }
                        mPosition = position;
                    }
                });
                startService();
        } else {
                ViewGroup parent = (ViewGroup) view.getParent();
                parent.removeView(view);
        }

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
           mForecastAdapter.setUseFirstLayout(mUseFirstLayout);
        return view;
    }

    public void setUseFirstLayout(boolean useFirstLayout){
        mUseFirstLayout = useFirstLayout;
        if(mForecastAdapter != null){
            mForecastAdapter.setUseFirstLayout(mUseFirstLayout);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if(mPosition != ListView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, filter the query to return news only for
        // dates after or including today.

        // Sort order:  Ascending, by date.
        String sportsSetting = Utility.getPreferredSports(getActivity());
        String sortOrder = RssContract.NewsEntry.COLUMN_DATE + " ASC";
        Uri newsForSportsUri = RssContract.NewsEntry.buildNewsSportsWithStartDate(
                sportsSetting, System.currentTimeMillis());
        return new CursorLoader(getActivity(),newsForSportsUri,
                FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor){
        mForecastAdapter.swapCursor(cursor);
        if(mPosition != ListView.INVALID_POSITION){
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader){
        mForecastAdapter.swapCursor(null);
    }

    // Service used to update data
    public void startService() {
        Intent intent = new Intent(getActivity(),RssService.class);
        intent.putExtra(RssService.RECEIVER, resultReceiver);
        getActivity().startService(intent);
    }

    private final ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
        @SuppressWarnings("unchecked")
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            progressBar.setVisibility(View.GONE);
            mListView.setAdapter(mForecastAdapter);
        };
    };

    // since we read the sports when we create the loader, all we need to do is restart things
   void onSportsChanged(){
       startService();
       getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
   }
}
