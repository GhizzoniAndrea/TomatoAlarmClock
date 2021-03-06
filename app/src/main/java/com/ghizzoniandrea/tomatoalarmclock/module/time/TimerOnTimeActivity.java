package com.ghizzoniandrea.tomatoalarmclock.module.time;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.ghizzoniandrea.tomatoalarmclock.R;
import com.ghizzoniandrea.tomatoalarmclock.common.Constants;
import com.ghizzoniandrea.tomatoalarmclock.db.DBUtil;
import com.ghizzoniandrea.tomatoalarmclock.entity.TomatoAlarmClock;
import com.ghizzoniandrea.tomatoalarmclock.library.timer.OttoAppConfig;
import com.ghizzoniandrea.tomatoalarmclock.library.timer.TimerOnTimeEvent;
import com.ghizzoniandrea.tomatoalarmclock.util.AudioPlayer;

/**
 * Created by ghizzoniandrea on 2017/2/7.
 */
public class TimerOnTimeActivity extends FragmentActivity implements View.OnClickListener {

    private AudioManager mAudioManager;
    private int mCurrentVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_on_time);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        playRing();
        initViews();

        OttoAppConfig.getInstance().post(new TimerOnTimeEvent());

        SharedPreferences preferences = getSharedPreferences(
                Constants.EXTRA_WEAC_SHARE, Activity.MODE_PRIVATE);
        long currentTaskID = preferences.getLong(Constants.CURRENT_TASK_ID, 0);
        TomatoAlarmClock clock = new DBUtil(getBaseContext()).loadByKey(currentTaskID);
        clock.setFinish(1);
        new DBUtil(getBaseContext()).update(clock);
    }

    private void playRing() {
        mAudioManager = (AudioManager) getSystemService(
                Context.AUDIO_SERVICE);

        mCurrentVolume = mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        // 设置铃声音量
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                6, AudioManager.ADJUST_SAME);

        SharedPreferences shares = getSharedPreferences(
                Constants.EXTRA_WEAC_SHARE, Activity.MODE_PRIVATE);
        String ringUrl = shares.getString(Constants.RING_URL_TIMER, Constants.DEFAULT_RING_URL);

        // 默认铃声
        switch (ringUrl) {
            case Constants.DEFAULT_RING_URL:
                AudioPlayer.getInstance(this).playRaw(R.raw.alarm_clock_default, true, false);
                break;
            // 无铃声
            case Constants.NO_RING_URL:
                AudioPlayer.getInstance(this).stop();
                break;
            default:
                AudioPlayer.getInstance(this).play(ringUrl, true, false);
                break;
        }
    }

    private void initViews() {
        Button rogerBtn = (Button) findViewById(R.id.roger_btn);
        rogerBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        // 停止播放
        AudioPlayer.getInstance(this).stop();
        // 设置铃声音量
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                mCurrentVolume, AudioManager.ADJUST_SAME);
        finish();
        overridePendingTransition(0, android.R.anim.fade_in);
    }
}
