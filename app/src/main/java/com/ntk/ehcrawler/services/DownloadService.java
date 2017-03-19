package com.ntk.ehcrawler.services;

import android.app.IntentService;
import android.content.Intent;

import com.ntk.ehcrawler.EHUtils;

public class DownloadService extends IntentService{

    public static final String ACTION_GET = "GET";

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent!=null){
            String action = intent.getAction();
            if(ACTION_GET.equals(action)){
                EHUtils.getBooks();
            }
        }
    }
}
