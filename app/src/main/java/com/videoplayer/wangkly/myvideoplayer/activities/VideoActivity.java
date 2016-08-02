package com.videoplayer.wangkly.myvideoplayer.activities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;


import com.videoplayer.wangkly.myvideoplayer.R;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.TitleController;
import io.vov.vitamio.widget.VideoView;

/**
 * Created by wangkly on 2016/3/14.
 */
public class VideoActivity extends Activity {

    private VideoView mVideoView;

    private View mVolumeBrightnessLayout;

    private ImageView mOperationBg;

    private ImageView mOperationPercent;

    private AudioManager mAudioManager;

    private FrameLayout ll;

    private int mMaxVolume;
    /**
     * 当前音量
     */
    private int mVolume =-1;
    private float mBrightness = -1f;

    private int mLayout = VideoView.VIDEO_LAYOUT_SCALE;
    private GestureDetector mGestureDetector;
    private MediaController mMediaController;

    private TitleController mTitleController;

    private String videopath ="";

    private String videotitle;

    private ViewGroup.LayoutParams videoViewParams;

    private RelativeLayout  rlVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        Bundle bundle = this.getIntent().getExtras();

        videopath =bundle.getString("videopath");

        videotitle =bundle.getString("title");

        if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
            return;
        ll= (FrameLayout) findViewById(R.id.ll_layout);
        mVideoView = (VideoView) findViewById(R.id.surface_view);
        mVolumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
        mOperationBg = (ImageView) findViewById(R.id.operation_bg);
        mOperationPercent = (ImageView) findViewById(R.id.operation_percent);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        mVideoView.setVideoPath(videopath);

        mMediaController = new MediaController(this,true,ll);

        mTitleController =new TitleController(this,true,ll);

        mVideoView.setMediaController(mMediaController);
        mVideoView.setTitleController(mTitleController);

    //    mTitleController.setFileName(videotitle);


        mVideoView.requestFocus();
        mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
        //不使用硬解码
        mVideoView.setHardwareDecoder(false);

        mGestureDetector = new GestureDetector(this , new MyGestureListener());

        rlVideo = (RelativeLayout) findViewById(R.id.rl_video);
        videoViewParams = (ViewGroup.LayoutParams) rlVideo.getLayoutParams();
       changeToFullScreen(true);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event))
            return true;

        // 处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                endGesture();
                break;
        }

        return super.onTouchEvent(event);
    }

    /** 手势结束 */
    private void endGesture() {
        mVolume = -1;
        mBrightness = -1f;

        // 隐藏
        mDismissHandler.removeMessages(0);
        mDismissHandler.sendEmptyMessageDelayed(0, 500);
    }



    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        /** 双击 */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
//            if (mLayout == VideoView.VIDEO_LAYOUT_ZOOM)
//                mLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
//            else
//                mLayout++;
//            if (mVideoView != null)
//                mVideoView.setVideoLayout(mLayout, 0);
            return true;
        }

        /** 滑动 */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getRawY();
            Display disp = getWindowManager().getDefaultDisplay();
            int windowWidth = disp.getWidth();
            int windowHeight = disp.getHeight();

            if (mOldX > windowWidth * 4.0 / 5)// 右边滑动
                onVolumeSlide((mOldY - y) / windowHeight);
            else if (mOldX < windowWidth / 5.0)// 左边滑动
                onBrightnessSlide((mOldY - y) / windowHeight);

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    /** 定时隐藏 */
    private Handler mDismissHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mVolumeBrightnessLayout.setVisibility(View.GONE);
        }
    };

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        if (mVolume == -1) {
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0)
                mVolume = 0;

            // 显示
            mOperationBg.setImageResource(R.drawable.video_volumn_bg);
            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }

        int index = (int) (percent * mMaxVolume) + mVolume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

        // 变更进度条
        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
        lp.width = findViewById(R.id.operation_full).getLayoutParams().width
                * index / mMaxVolume;
        mOperationPercent.setLayoutParams(lp);
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (mBrightness < 0) {
            mBrightness = getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f)
                mBrightness = 0.50f;
            if (mBrightness < 0.01f)
                mBrightness = 0.01f;

            // 显示
            mOperationBg.setImageResource(R.drawable.video_brightness_bg);
            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }
        WindowManager.LayoutParams lpa = getWindow().getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f)
            lpa.screenBrightness = 1.0f;
        else if (lpa.screenBrightness < 0.01f)
            lpa.screenBrightness = 0.01f;
        getWindow().setAttributes(lpa);

        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
        lp.width = (int) (findViewById(R.id.operation_full).getLayoutParams().width * lpa.screenBrightness);
        mOperationPercent.setLayoutParams(lp);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_STRETCH, 0);
        } else {
            mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_STRETCH, 0);
        }
     }


    private void fullScreenToNormal() {
//        isFullScreen = false;
//        btnVideoFullScreen.setBackgroundResource(R.drawable.window_to_fullscreen_normal);
//        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        videoViewParams.height = viewHeight;
//        videoViewParams.width = LayoutParams.MATCH_PARENT;
//        mVideoView.setLayoutParams(videoViewParams);
//        mLayout = VideoView.VIDEO_LAYOUT_SCALE;
//        mVideoView.setVideoLayout(mLayout, 0);
    }

    private void changeToFullScreen(boolean isLeft) {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        rlVideo.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
//        RelativeLayout.LayoutParams layoutParams =
//                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//       rlVideo.setLayoutParams(layoutParams);
        videoViewParams.height = LayoutParams.MATCH_PARENT;
        videoViewParams.width = LayoutParams.MATCH_PARENT;
        rlVideo.setLayoutParams(videoViewParams);
//        mLayout = VideoView.VIDEO_LAYOUT_SCALE;
//        mVideoView.setVideoLayout(mLayout, 0);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK){
           if(mVideoView.isPlaying()){

               mVideoView.suspend();

           }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}