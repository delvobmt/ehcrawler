package com.ntk.ehcrawler.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ntk.ehcrawler.EHConstants;
import com.ntk.ehcrawler.R;
import com.ntk.ehcrawler.adapters.BookAdapter;
import com.ntk.ehcrawler.database.BookProvider;
import com.ntk.ehcrawler.model.BookConstants;
import com.ntk.ehcrawler.services.DatabaseService;

import java.util.HashMap;
import java.util.Map;

import static com.ntk.ehcrawler.EHConstants.SEARCH_BOOLEAN_KEY;
import static com.ntk.ehcrawler.EHConstants.SEARCH_KEY;
import static com.ntk.ehcrawler.EHConstants.SEARCH_PREFERENCES;

public class BookFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = "LOG_" + BookFragment.class.getSimpleName();
    protected RecyclerView mBooksView;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected BookAdapter mAdapter;
    protected SwipeRefreshLayout mRefreshLayout;

    protected Map<String, String> filterMap = new HashMap<>();
    protected boolean mChanged;
    protected boolean mLoaded;
    protected int position;

    public BookFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book, container, false);

        mBooksView = (RecyclerView) view.findViewById(R.id.books_rv);
        mLayoutManager = new LinearLayoutManager(getContext());

        mBooksView.setLayoutManager(mLayoutManager);

        mAdapter = new BookAdapter(getContext(), isLoadAtEndingPage());
        mBooksView.setAdapter(mAdapter);

        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);

        initLoader();

        return view;
    }

    protected boolean isLoadAtEndingPage() {
        return true;
    }

    protected void initLoader(){
        getLoaderManager().initLoader(BookProvider.BOOKS_LOADER, null, this);
    }

    @Override
    public void onDestroy() {
        destroy();
        super.onDestroy();
    }

    protected void destroy(){
        getLoaderManager().destroyLoader(BookProvider.BOOKS_LOADER);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        loadFilter();
        SharedPreferences preferences = getActivity().getSharedPreferences(SEARCH_PREFERENCES, Activity.MODE_PRIVATE);
        position = preferences.getInt(EHConstants.CUR_POSITION, 0);
        super.onResume();
    }

    protected void loadFilter() {
        SharedPreferences preferences = getActivity().getSharedPreferences(SEARCH_PREFERENCES, Activity.MODE_PRIVATE);
        String f_search = preferences.getString(SEARCH_KEY, "");
        mChanged = false;
        for(String key : SEARCH_BOOLEAN_KEY){
            boolean newValue = preferences.getBoolean(key, true);
            String oldValue = filterMap.get(key);
            if(oldValue == null){
                //first load
                filterMap.put(key, newValue?"1":"0");
            }else{
                mChanged |= newValue != "1".equals(oldValue);
                //update new value
                filterMap.put(key, newValue?"1":"0");
            }
        }
        String oldKey = filterMap.get(SEARCH_KEY);
        mChanged |= oldKey != null && !f_search.equals(oldKey);
        // update new value
        filterMap.put(SEARCH_KEY, String.valueOf(f_search));
        DatabaseService.setFilterMap(filterMap);
        if (mChanged) {
            Toast.makeText(getActivity(),"applying search option",Toast.LENGTH_SHORT).show();
            getNewData();
        }
    }

    @Override
    public void onRefresh() {
        getNewData();
        SharedPreferences.Editor edit = getActivity().getSharedPreferences(SEARCH_PREFERENCES, Activity.MODE_PRIVATE).edit();
        edit.putInt(EHConstants.CUR_POSITION, 0);
        edit.commit();
    }

    private void getNewData(){
        synchronized (mRefreshLayout) {
            DatabaseService.startGetBook(getContext(), "0");
            mRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Context context = getContext();
        Uri uri = null;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        if (BookProvider.BOOKS_LOADER == id){
            uri = BookProvider.BOOKS_CONTENT_URI;
            projection = BookConstants.PROJECTION;
            selection = BookConstants.IS_HIDDEN + "!= 1";
            selectionArgs = new String[]{};
        }
        return new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (BookProvider.BOOKS_LOADER == loader.getId()){
            mAdapter.changeCursor(data);
            if (!mLoaded && (data == null || data.getCount() == 0)) {
                mLoaded = true;
                //There is data yet, should call request to download content from service
                getNewData();
            } else {
                synchronized (mRefreshLayout) {
                    mRefreshLayout.setRefreshing(mChanged);
                    mChanged = false;
                    int curPosition = data.getPosition();
                    if(curPosition == -1 && position !=0) {
                        Log.i(LOG_TAG, "move to position: " + position);
                        mBooksView.scrollToPosition(data.getCount() < position ? data.getCount()-1 : position);
                    }
                    //reset position after loaded
                    position = 0;
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
