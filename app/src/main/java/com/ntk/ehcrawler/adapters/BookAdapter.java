package com.ntk.ehcrawler.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.model.BookConstants;
import com.squareup.picasso.Picasso;

public class BookAdapter extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {
    private final Context mContext;

    public BookAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_view, null);
        return new RecyclerView.ViewHolder(view){};
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, Cursor cursor) {
//        View view = holder.itemView;
//        ImageView mImage = (ImageView) view.findViewById(R.id.image_iv);
//        TextView mTitle = (TextView) view.findViewById(R.id.title_tv);
//        String title = cursor.getString(BookConstants.TITLE_INDEX);
//        String imageSrc = cursor.getString(BookConstants.IMAGE_SRC_INDEX);
//        Picasso.with(mContext).load(imageSrc).fit().into(mImage);
//        mTitle.setText(title);
    }
}
