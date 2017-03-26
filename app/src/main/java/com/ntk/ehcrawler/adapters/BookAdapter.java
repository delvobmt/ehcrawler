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

import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.activities.GalleryActivity;
import com.ntk.ehcrawler.model.BookConstants;
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
        View view = holder.itemView;
        ImageView mImage = (ImageView) view.findViewById(R.id.image_iv);
        TextView mTitle = (TextView) view.findViewById(R.id.title_tv);
        TextView mFileCount = (TextView) view.findViewById(R.id.file_count_tv);
        RatingBar mRate = (RatingBar) view.findViewById(R.id.rate_rb);

        String title = cursor.getString(BookConstants.TITLE_INDEX);
        String imageSrc = cursor.getString(BookConstants.IMAGE_SRC_INDEX);
        final String url = cursor.getString(BookConstants.URL_INDEX);
        String type = cursor.getString(BookConstants.TYPE_INDEX);
        int fileCount = cursor.getInt(BookConstants.FILE_COUNT_INDEX);
        float rate = cursor.getFloat(BookConstants.RATE_INDEX)/2;

        Picasso.with(mContext).load(imageSrc).into(mImage);
        mTitle.setText(title);
        mFileCount.setText(fileCount+" page(s) "+type);
        mRate.setRating(rate);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, GalleryActivity.class);
                intent.putExtra(BookConstants.URL, url);
                mContext.startActivity(intent);
            }
        });
    }
}
