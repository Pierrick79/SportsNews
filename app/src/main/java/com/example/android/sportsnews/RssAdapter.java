package com.example.android.sportsnews;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.sportsnews.data.RssContract;

/**
 * Created by Pierrick on 08/04/2015.
 */
public class RssAdapter extends CursorAdapter{
    public RssAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseFirstLayout = true;

    public void setUseFirstLayout(boolean useFirstLayout){
        mUseFirstLayout = useFirstLayout;
    }

    private String convertTitleCursorRowToUXFormat(Cursor cursor){
        // Read title from cursor
        return cursor.getString(cursor.getColumnIndex(RssContract.NewsEntry.COLUMN_TITLE));
    }

    private String convertTimeCursorRowToUXFormat(Cursor cursor){
        // Read date from cursor
        return cursor.getString(cursor.getColumnIndex(RssContract.NewsEntry.COLUMN_TIME));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.title.setText(convertTitleCursorRowToUXFormat(cursor));
        viewHolder.time.setText(convertTimeCursorRowToUXFormat(cursor));
    }

    /**
     * Cache of the children views for a forecast list item.
     */

    public static class ViewHolder {
        public final TextView title;
        public final TextView time;

        public ViewHolder(View view){
            title = (TextView) view.findViewById(R.id.itemTitle);
            time = (TextView) view.findViewById(R.id.itemDate);
        }
    }
}
