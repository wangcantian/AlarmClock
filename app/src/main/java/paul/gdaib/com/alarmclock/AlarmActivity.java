package paul.gdaib.com.alarmclock;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentBreadCrumbs;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import paul.gdaib.com.alarmclock.alarms.AlarmAlertWakeLock;
import paul.gdaib.com.alarmclock.alarms.AlarmService;
import paul.gdaib.com.alarmclock.alarms.AlarmStateManager;
import paul.gdaib.com.alarmclock.bean.Alarm;
import paul.gdaib.com.alarmclock.bean.AlarmInstance;
import paul.gdaib.com.alarmclock.utils.LogUtils;
import paul.gdaib.com.alarmclock.utils.ToastUtils;
import paul.gdaib.com.alarmclock.view.TextTime;

/**
 * Created by Paul on 2016/10/23.
 */

public class AlarmActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = AlarmActivity.class.getSimpleName();
    public static final String ALARM_SNOOZE_ACTION = "com.android.deskclock.ALARM_SNOOZE";
    public static final String ALARM_DISMISS_ACTION = "com.android.deskclock.ALARM_DISMISS";

    private AlarmInstance mAlarmInstance;
    private Button mCloseAlarmBtn;
    private EditText mMathET;
    private int mVolumeBehavior;

    private int left;
    private int right;
    private int yunsuan;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ALARM_SNOOZE_ACTION)) {
                snooze();
            } else if (action.equals(ALARM_DISMISS_ACTION)) {
                dismiss();
            } else if (action.equals(AlarmService.ALARM_DONE_ACTION)) {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long instanceId = AlarmInstance.getId(getIntent().getData());
        mAlarmInstance = AlarmInstance.getInstanceById(getContentResolver(), instanceId);
        if (mAlarmInstance == null) finish();


        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mVolumeBehavior = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(SettingsActivity.KEY_VOLUME_BUTTONS, SettingsActivity.DEFAULT_VOLUME_BEHAVIOR);

        setContentView(R.layout.activity_alarm);
        TextTime tt = (TextTime) findViewById(R.id.tt_clock_time);
        tt.setTime(mAlarmInstance.mHour, mAlarmInstance.mMinute);
        mCloseAlarmBtn = (Button) findViewById(R.id.btn_close_alarm);
        mCloseAlarmBtn.setOnClickListener(this);
        findViewById(R.id.btn_snooze).setOnClickListener(this);
        ((TextView) findViewById(R.id.tv_label)).setText(mAlarmInstance.getLabelOrDefault(this));
        if (mAlarmInstance.mCloseType == Alarm.TYPE_NONE) {
            mCloseAlarmBtn.setText("关闭");
        } else if (mAlarmInstance.mCloseType == Alarm.TYPE_MATH) {
            findViewById(R.id.rl_math).setVisibility(View.VISIBLE);
            mMathET = (EditText) findViewById(R.id.et_math);
            TextView tv_math = (TextView) findViewById(R.id.tv_math);
            mCloseAlarmBtn.setText("完成");
            left = (int) (Math.random() * 50);
            right = (int) (Math.random() * 50);
            yunsuan = (int) (Math.random() * 3);
            switch (yunsuan) {
                case 0:
                    tv_math.setText(left + " + " + right + " = ");
                    break;
                case 1:
                    tv_math.setText(left + " - " + right + " = ");
                    break;
                case 2:
                    tv_math.setText(left + " * " + right + " = ");
                    break;
            }
        } else if (mAlarmInstance.mCloseType == Alarm.TYPE_CODE) {
            mCloseAlarmBtn.setText("扫码后关闭");
        }

        IntentFilter intentFilter = new IntentFilter(AlarmService.ALARM_DONE_ACTION);
        intentFilter.addAction(ALARM_SNOOZE_ACTION);
        intentFilter.addAction(ALARM_DISMISS_ACTION);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        if (mAlarmInstance == null) {
            super.onDestroy();
            return;
        }
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        LogUtils.e(TAG, "dispatchKeyEvent" + event.getKeyCode());
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_POWER:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_FOCUS:
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (mVolumeBehavior) {
                        case 1:
                            snooze();
                            break;
                        case 2:
                            dismiss();
                            break;
                        default:
                            break;
                    }
                }
                return true;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    @Override
    public void onBackPressed() {
    }

    private void snooze() {
        AlarmStateManager.setSnoozeState(this, mAlarmInstance);
    }

    private void dismiss() {
        AlarmStateManager.setDismissState(this, mAlarmInstance);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close_alarm:
                dismiss();
                break;
            case R.id.btn_snooze:
                if (mAlarmInstance.mCloseType == Alarm.TYPE_CODE) {
                    new IntentIntegrator(this)
                            .setOrientationLocked(false)
                            .setCaptureActivity(CustomScanActivity.class)
                            .initiateScan();
                    return;
                } else if (mAlarmInstance.mCloseType == Alarm.TYPE_MATH) {
                    String result = mMathET.getText().toString();
                    if (TextUtils.isEmpty(result)) {
                        ToastUtils.showOnly(this, "答案不能为空！");
                        return;
                    } else {
                        int re;
                        try {
                            re = Integer.valueOf(result);
                        } catch (Exception e) {
                            ToastUtils.showOnly(this, "答案必须为数字！");
                            return;
                        }
                        switch (yunsuan) {
                            case 0:
                                if (left + right == re) {
                                    dismiss();
                                    return;
                                }
                                break;
                            case 1:
                                if (left - right == re) {
                                    dismiss();
                                    return;
                                }
                                break;
                            case 2:
                                if (left * right == re) {
                                    dismiss();
                                    return;
                                }
                                break;
                        }
                        ToastUtils.showOnly(this, "答案错误！");
                        return;
                    }
                }
                snooze();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() != null) {
                String ScanResult = intentResult.getContents();
                if (ScanResult.equals(mAlarmInstance.mStringCode)) {
                    ToastUtils.showOnly(this, "扫码正确，闹钟关闭");
                    dismiss();
                } else {
                    ToastUtils.showOnly(this, "扫码错误！");
                }
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
