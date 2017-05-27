package com.ntk.ehcrawler.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ntk.ehcrawler.EHConstants;
import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.activities.GalleryActivity;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.services.DatabaseService;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class BookAdapter extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {
    private final Context mContext;

    public BookAdapter(Context context) {
        super(context, null);
        mContext = context;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_view, null);
        return new RecyclerView.ViewHolder(view){};
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, Cursor cursor) {
        int position = cursor.getPosition()+1;
        int count = cursor.getCount();
        if(position == count){
            int pageIndex = position/ EHConstants.BOOKS_PER_PAGE;
            DatabaseService.startGetBook(mContext, String.valueOf(pageIndex));
        }
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

        Picasso.with(mContext).load(imageSrc).into(mImage, new Callback() {
            @Override
            public void onSuccess() {
                mLoading.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
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
            }
        });
    }
}
