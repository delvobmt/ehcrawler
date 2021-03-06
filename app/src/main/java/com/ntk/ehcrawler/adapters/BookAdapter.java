package com.ntk.ehcrawler.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ntk.R;
import com.ntk.ehcrawler.EHConstants;
import com.ntk.ehcrawler.activities.GalleryActivity;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.services.DatabaseService;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import static com.ntk.ehcrawler.EHConstants.SEARCH_PREFERENCES;

public class BookAdapter extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {
    private final Context mContext;
    private boolean loadNewAtEndPage = false;
    private static final String LOG_TAG = "LOG_" + BookAdapter.class.getSimpleName();

    public BookAdapter(Context context) {
        super(context, null);
        mContext = context;
    }

    public BookAdapter(Context context, boolean loadNewAtEndPage) {
        this(context);
        this.loadNewAtEndPage = loadNewAtEndPage;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_view, null);
        return new RecyclerView.ViewHolder(view){};
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final Cursor cursor) {
        View view = holder.itemView;
        final ImageView mImage = (ImageView) view.findViewById(R.id.image_iv);
        final TextView mTitle = (TextView) view.findViewById(R.id.title_tv);
        final TextView mFileCount = (TextView) view.findViewById(R.id.file_count_tv);
        final RatingBar mRate = (RatingBar) view.findViewById(R.id.rate_rb);
        final View mLoading = view.findViewById(R.id.loading);

        final String title = cursor.getString(BookConstants.TITLE_INDEX);
        final String imageSrc = cursor.getString(BookConstants.IMAGE_SRC_INDEX);
        final String id = cursor.getString(0);
        final String url = cursor.getString(BookConstants.URL_INDEX);
        final String type = cursor.getString(BookConstants.TYPE_INDEX);
        final int fileCount = cursor.getInt(BookConstants.FILE_COUNT_INDEX);
        final float rate = cursor.getFloat(BookConstants.RATE_INDEX)/2;
        final int position = cursor.getPosition();

        Picasso.with(mContext).load(imageSrc).into(mImage, new Callback() {
            @Override
            public void onSuccess() {
                mLoading.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                mLoading.setVisibility(View.GONE);
            }
        });
        mTitle.setText(title);
        mFileCount.setText(fileCount+" page(s) "+type);
        mRate.setRating(rate);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, GalleryActivity.class);
                intent.putExtra(BookConstants._ID, id);
                intent.putExtra(BookConstants.TITLE, title);
                intent.putExtra(BookConstants.RATE, rate);
                intent.putExtra(BookConstants.TYPE, type);
                intent.putExtra(BookConstants.URL, url);
                intent.putExtra(BookConstants.FILE_COUNT, fileCount);
                mContext.startActivity(intent);
                if(loadNewAtEndPage) {
                    saveCurrentPosition(position);
                }
            }
        });

        /* prepare new data for next page*/
        int count = cursor.getCount();
        if (position + 1 == count && loadNewAtEndPage) {
            int pageIndex = (int) Math.ceil((position + 1) / (double) EHConstants.BOOKS_PER_PAGE);
            DatabaseService.startGetBook(mContext, pageIndex);
        }
    }

    protected void saveCurrentPosition(int position) {
        SharedPreferences.Editor editor = mContext
                .getSharedPreferences(SEARCH_PREFERENCES, Activity.MODE_PRIVATE).edit();
        editor.putInt(EHConstants.CUR_POSITION, position);
        editor.commit();
        Log.i(LOG_TAG, "saved current position: " + position);
    }
}
