package com.ntk.reactor;

import android.graphics.Rect;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ntk.R;

public class TestActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        SurfaceView surfaceView = findViewById(R.id.video_view);

        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.video);
        mediaPlayer.setDisplay(holder);
        mediaPlayer.setLooping(true);
        Rect surfaceFrame = holder.getSurfaceFrame();
        int width = surfaceFrame.width();
        int videoWidth = mediaPlayer.getVideoWidth();
        float ratio = (float) width/ videoWidth;
        int videoHeight = (int) (mediaPlayer.getVideoHeight() * ratio);

        holder.setFixedSize(width, videoHeight);
        mediaPlayer.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
