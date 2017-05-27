package com.example.guowei.myvideoplayer;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.guowei.myvideoplayer.CoustomView.CustomVideoView;
import com.example.guowei.myvideoplayer.util.DensityUtils;

import java.io.File;

/**
 * 自定义视频播放器 20170526
 */

public class MainActivity extends AppCompatActivity {
    private CustomVideoView mVideoView;
    private LinearLayout controllerLayout;
    private RelativeLayout mVideoLayout;
    private ImageView mPlayOrPauseIv,mSwitchScreenIv,mVolumeIv;
    private SeekBar mVideoPlaySeek,mVolumeSeek;
    private TextView mCurrentTv,mTotalTv;

    private FrameLayout mCenterTiplayout;
    private ImageView progressIcon,progressPercent;
    private static final int UPDATE_UI = 0;
    private static final int CONTROL_HIDE = 1;
    private boolean isAdjust =false;
    private int threshoid = 54;
    private float lastX,lastY;
    private int screenHeight,screenWidth;

    private int mCurrentPosition;

    private AudioManager mAudioManager;
    private float mBrightness;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        setListener();
        startPlay();
    }
    /**
     * 视频播放
     */
    private void startPlay() {
        /*
         * 本地播放
         */
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator+"dream.mp4";
        mVideoView.setVideoPath(path);
        /*
         * 网络播放
         */
//        mVideoView.setVideoURI(Uri.parse(""));
        /*使用MediaController控制视频播放
         */
//        MediaController mController = new MediaController(this);
//        /**
//         * 设置mVideoView与MediaController关联
//         */
//        mVideoView.setMediaController(mController);
//        /**
//         * 设置MediaController与mVideoView关联
//         */
//        mController.setMediaPlayer(mVideoView);
        mVideoView.start();
        UIhandle.sendEmptyMessage(UPDATE_UI);
    }
    /**初始化控件*/
    private void initUI() {
        DensityUtils.init(this);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mVideoView = (CustomVideoView) findViewById(R.id.video_view);
        controllerLayout = (LinearLayout) findViewById(R.id.video_controller_layout);
        mPlayOrPauseIv = (ImageView) findViewById(R.id.video_play);
        mSwitchScreenIv = (ImageView) findViewById(R.id.iv_screen);
        mVolumeIv = (ImageView) findViewById(R.id.iv_volume);
        mVideoPlaySeek = (SeekBar) findViewById(R.id.video_seek);
        mVolumeSeek = (SeekBar) findViewById(R.id.volume_seek);
        mCurrentTv = (TextView) findViewById(R.id.tv_current_time);
        mTotalTv = (TextView) findViewById(R.id.tv_total_time);
        mVideoLayout = (RelativeLayout) findViewById(R.id.video_layout);
        /*音量，亮度提示布局*/
        mCenterTiplayout = (FrameLayout) findViewById(R.id.progress_layout);
        progressIcon = (ImageView) findViewById(R.id.progress_theme);
        progressPercent = (ImageView) findViewById(R.id.progress_bg);

        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        // 设置音量
        mVolumeSeek.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mVolumeSeek.setProgress(volume);
        if (volume == 0) {
            mVolumeIv.setImageResource(R.mipmap.mute);
        }

    }
    /**
     * 设置控件监听
     */
    private void setListener() {
        /**
         * 控制视频的播放和暂停
         */
        mPlayOrPauseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mVideoView.isPlaying()){
                    mPlayOrPauseIv.setImageResource(R.drawable.play_btn_style);
                    mVideoView.pause();
                    UIhandle.removeMessages(UPDATE_UI);
                }else{
                    mPlayOrPauseIv.setImageResource(R.drawable.pause_btn_style);
                    mVideoView.start();
                    UIhandle.sendEmptyMessage(UPDATE_UI);
                }

            }
        });
        /**
         * 播放器进度条拖动监听
         */
        mVideoPlaySeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mVideoView.seekTo(progress);
                    updateTextViewWithTimeFormat(mCurrentTv,progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                UIhandle.removeMessages(UPDATE_UI);
                if(!mVideoView.isPlaying()){
                    mVideoView.start();
                    mPlayOrPauseIv.setImageResource(R.drawable.pause_btn_style);
                }
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                UIhandle.sendEmptyMessage(UPDATE_UI);

            }
        });
        /**
         * 横竖屏切换按钮监听
         */
        mSwitchScreenIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }else{
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }

            }
        });
/**
 * 滑动监听改变音量和亮度
 */
    mVideoView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float x= event.getX();
            float y = event.getY();
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    lastX = x;
                    lastY = y;
                    controllerLayout.setVisibility(View.VISIBLE);
                    if(UIhandle.hasMessages(CONTROL_HIDE)){
                        UIhandle.removeMessages(CONTROL_HIDE);
                    }
                    if(mVideoView.isPlaying()&&mVideoView.canPause()){
                        mPlayOrPauseIv.setImageResource(R.drawable.play_btn_style);
                        mVideoView.pause();
                    }
                break;
                case MotionEvent.ACTION_MOVE:
                    float detlax = x-lastX;
                    float detlay = y -lastY;
                    float absdetlax = Math.abs(detlax);
                    float absdetlay = Math.abs(detlay);
                    if(absdetlax>threshoid&&absdetlay>threshoid){
                        if(absdetlax<absdetlay){
                         isAdjust = true;
                        }else{
                         isAdjust = false;
                        }
                    }else if(absdetlax<threshoid&&absdetlay>threshoid){
                        isAdjust=true;
                    }else if(absdetlax>threshoid&&absdetlay<threshoid){
                        isAdjust=false;
                    }
                    if(isAdjust){
                        if(x<screenWidth/2){
                            changeBrightness(-detlay);
                        }else {
                            changeVolume(-detlay);
                        }
                    }
                    lastX = x;
                    lastY = y;
                    break;
                case MotionEvent.ACTION_UP:
                    lastX = 0;
                    lastY = 0;
                    mCenterTiplayout.setVisibility(View.GONE);

                    UIhandle.sendEmptyMessageDelayed(CONTROL_HIDE,5000);
                    break;
            }
            return true;
        }
    });
        /**
         * 音量进度条拖动
         */
        mVolumeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mVolumeSeek.setProgress(progress);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
                if(progress==0){
                    mVolumeIv.setImageResource(R.mipmap.mute);
                }else{
                    mVolumeIv.setImageResource(R.mipmap.volume);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mVideoView.canSeekForward()&&mCurrentPosition!=0){
            mVideoView.seekTo(mCurrentPosition);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mVideoView.canPause()){
            mPlayOrPauseIv.setImageResource(R.drawable.play_btn_style);
            mCurrentPosition = mVideoView.getCurrentPosition();
            mVideoView.pause();
        }
        if(UIhandle.hasMessages(UPDATE_UI)) {
            UIhandle.removeMessages(UPDATE_UI);
        }
        if(UIhandle.hasMessages(CONTROL_HIDE)){
            UIhandle.removeMessages(CONTROL_HIDE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mVideoView.canPause()){
            mPlayOrPauseIv.setImageResource(R.drawable.play_btn_style);
            mVideoView.pause();
        }
        if(UIhandle.hasMessages(UPDATE_UI)){
            UIhandle.removeMessages(UPDATE_UI);
        }
        if(UIhandle.hasMessages(CONTROL_HIDE)){
            UIhandle.removeMessages(CONTROL_HIDE);
        }

    }

    /**
     * 隐藏系统状态栏
     */
    private void setSystemUiHide(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            View decoView = getWindow().getDecorView();
            decoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /**
     * 显示系统状态栏
     */
    private void setSystemUiShow(){
    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
        View decoView = getWindow().getDecorView();
        decoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }
}
    /**更新UI的线程*/
    private Handler UIhandle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==UPDATE_UI){
                 mCurrentPosition = mVideoView.getCurrentPosition();
                int mTotalPosition = mVideoView.getDuration();
                updateTextViewWithTimeFormat(mCurrentTv,mCurrentPosition);
                updateTextViewWithTimeFormat(mTotalTv,mTotalPosition);
                mVideoPlaySeek.setMax(mTotalPosition);
                mVideoPlaySeek.setProgress(mCurrentPosition);
                UIhandle.sendEmptyMessageDelayed(UPDATE_UI,500);
            }else if(msg.what==CONTROL_HIDE){
                controllerLayout.setVisibility(View.GONE);
            }
        }
    };
    /**
     * 时间格式化
     * @param textView
     * @param millisecond
     */
    private void updateTextViewWithTimeFormat(TextView textView, int millisecond) {
        int s = millisecond/1000;
        int hh = s/3600;
        int mm = s%3600/60;
        int ss= s%60;
        String str;
        if(hh!=0){
            str = String.format("%02d:%02d:%02d",hh,mm,ss);
        }else{
            str = String.format("%02d:%02d",mm,ss);
        }
        textView.setText(str);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            setSystemUiHide();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            mSwitchScreenIv.setImageResource(R.mipmap.exit_full_screen);
            mVolumeSeek.setVisibility(View.VISIBLE);
            mVolumeIv.setVisibility(View.VISIBLE);

            setControllerShowAutoHide();

        }else{
            setSystemUiShow();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtils.dp2px(250));
            mSwitchScreenIv.setImageResource(R.mipmap.full_screen);

            mVolumeSeek.setVisibility(View.GONE);
            mVolumeIv.setVisibility(View.GONE);
            setControllerShowAutoHide();

        }
    }

    /**
     * 屏幕切换,control显示然后5s自动隐藏
     */
    private void setControllerShowAutoHide() {
        if(controllerLayout.getVisibility()==View.GONE){
            controllerLayout.setVisibility(View.VISIBLE);
        }
        UIhandle.sendEmptyMessageDelayed(CONTROL_HIDE,5000);
    }

    /**
     * 切换屏幕后视频控件的拉伸处理
     * @param width
     * @param height
     */
    private void setVideoViewScale(int width,int height){
        ViewGroup.LayoutParams params1 =  mVideoLayout.getLayoutParams();
        params1.width = width;
        params1.height = height;
        mVideoLayout.setLayoutParams(params1);

        ViewGroup.LayoutParams params2 =  mVideoView.getLayoutParams();
        params2.width = width;
        params2.height = height;
        mVideoView.setLayoutParams(params2);
    }
    /**
     * 根据滑动的距离改变音量处理
     */
    private void changeVolume(float detlaY){
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int mCurrent =mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int index = (int) (detlaY/screenHeight*max*3);
        int volume = Math.max(mCurrent+index,0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,volume,0);
        mVolumeSeek.setProgress(volume);
        if(volume==0){
            mVolumeIv.setImageResource(R.mipmap.mute);
        }else{
            mVolumeIv.setImageResource(R.mipmap.volume);
        }
        if(mCenterTiplayout.getVisibility()==View.GONE){
            mCenterTiplayout.setVisibility(View.VISIBLE);
        }
        progressIcon.setImageResource(R.mipmap.video_voice_bg);
        ViewGroup.LayoutParams params = progressPercent.getLayoutParams();
        params.width = (int) (DensityUtils.dp2px(94)*volume*1.0f/max);
        progressPercent.setLayoutParams(params);
    }
    /**
     * 根据滑动的距离改变亮度处理
     */
    private void changeBrightness(float detlaY){
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        mBrightness = attributes.screenBrightness;
        float index = detlaY/screenHeight;
        mBrightness+=index;
        if(mBrightness<0.01f){
            mBrightness=0.01f;
        }if(mBrightness>1f){
            mBrightness=1f;
        }
        attributes.screenBrightness=mBrightness;
        getWindow().setAttributes(attributes);

        if(mCenterTiplayout.getVisibility()==View.GONE){
            mCenterTiplayout.setVisibility(View.VISIBLE);
        }
        progressIcon.setImageResource(R.mipmap.video_brightness_bg);
        ViewGroup.LayoutParams params = progressPercent.getLayoutParams();
        params.width = (int) (DensityUtils.dp2px(94)*mBrightness);
        progressPercent.setLayoutParams(params);


    }

    /**
     * 返回点击处理
     */
    @Override
    public void onBackPressed() {
        if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else{
            super.onBackPressed();
        }
    }
}
