package paul.gdaib.com.alarmclock;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.datetimepicker.HapticFeedbackController;
import com.android.datetimepicker.Utils;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.HashSet;

import paul.gdaib.com.alarmclock.bean.Alarm;
import paul.gdaib.com.alarmclock.utils.AlarmUtils;
import paul.gdaib.com.alarmclock.utils.LogUtils;
import paul.gdaib.com.alarmclock.utils.ToastUtils;
import paul.gdaib.com.alarmclock.view.LabelDialogFragment;
import paul.gdaib.com.alarmclock.view.RadioDialogFragment;
import paul.gdaib.com.alarmclock.view.SquareToggleButton;

/**
 * Created by Paul on 2016/10/26.
 */

public class AddAlarmActivity extends AppCompatActivity implements RadialPickerLayout.OnValueSelectedListener,
        View.OnClickListener, LabelDialogFragment.AlarmLabelDialogHandler {

    private static final String TAG = AddAlarmActivity.class.getSimpleName();
    private static final String SYSTEM_DEFAULT_ALARM = "content://settings/system/alarm_alert";
    private final int[] DAY_ORDER = new int[]{
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
    };

    private static final int DEFAULT_HOUR = 12;
    private static final int DEFAULT_MINTUE = 12;
    private static final int PULSE_ANIMATOR_DELAY = 300;
    public static final int HOUR_INDEX = 0;
    public static final int MINUTE_INDEX = 1;


    private HapticFeedbackController mHapticFeedbackController;
    private int mSelectedColor;
    private int mUnSelectedColor;
    private Alarm mAlarm;

    private RadialPickerLayout mTimePicker;
    private TextView mHoursView;
    private TextView mMinutesView;
    private TextView mLabelTV;
    private TextView mRingtoneTV;
    private RelativeLayout mRingtoneRL;
    private SquareToggleButton[] mDayBtns = new SquareToggleButton[7];
    private SwitchCompat mVibrateSwitch;
    private TextView mCloseTypeTV;
    private RelativeLayout mCodeRL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int type = getIntent().getIntExtra("flags", AlarmClockFragment.REQUEST_CODE_ADD);
        if (type == AlarmClockFragment.REQUEST_CODE_ADD) {
            mAlarm = new Alarm(DEFAULT_HOUR, DEFAULT_MINTUE);
            mAlarm.mEnabled = true;
        } else if (type == AlarmClockFragment.REQUEST_CODE_EDIT) {
            long alarmId = getIntent().getLongExtra("alarmId", Alarm.INVALID_ID);
            mAlarm = Alarm.getAlarmById(getContentResolver(), alarmId);
            if (mAlarm == null) {
                finish();
            }
        }

        mSelectedColor = getResources().getColor(com.android.datetimepicker.R.color.blue);
        mUnSelectedColor = getResources().getColor(R.color.white);
        setContentView(R.layout.activity_add_alarm);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 选择文字
        mHoursView = (TextView) findViewById(R.id.tv_hours);
        mHoursView.setText(String.format("%02d", mAlarm.mHour));
        mHoursView.setTextColor(mSelectedColor);
        mHoursView.setOnClickListener(this);
        mMinutesView = (TextView) findViewById(R.id.tv_minutes);
        mMinutesView.setText(String.format("%02d", mAlarm.mMinutes));
        mMinutesView.setTextColor(mUnSelectedColor);
        mMinutesView.setOnClickListener(this);
        ObjectAnimator pulseAnimator2 = Utils.getPulseAnimator(mHoursView, 0.85f, 1.1f);
        pulseAnimator2.setStartDelay(PULSE_ANIMATOR_DELAY);
        pulseAnimator2.start();
        // 时钟
        mHapticFeedbackController = new HapticFeedbackController(this);
        mTimePicker = (RadialPickerLayout) findViewById(R.id.time_picker);
        mTimePicker.setOnValueSelectedListener(this);
        mTimePicker.initialize(this, mHapticFeedbackController, mAlarm.mHour, mAlarm.mMinutes, true);
        mTimePicker.setCurrentItemShowing(HOUR_INDEX, true);
        // 标签
        mLabelTV = (TextView) findViewById(R.id.tv_label);
        mLabelTV.setText(mAlarm.getDefaultLabelOrDefind(this));
        mLabelTV.setOnClickListener(this);
        // 铃声
        mRingtoneTV = (TextView) findViewById(R.id.tv_ringtone);
        refreshRingtoneTitle();
        mRingtoneRL = (RelativeLayout) findViewById(R.id.rl_ringtone);
        mRingtoneRL.setOnClickListener(this);
        // 震动
        mVibrateSwitch = (SwitchCompat) findViewById(R.id.switch_vibrate);
        mVibrateSwitch.setChecked(mAlarm.mEnabled);
        findViewById(R.id.rl_vibrate).setOnClickListener(this);
        // 星期
        LinearLayout dayBtnLL = (LinearLayout) findViewById(R.id.ll_dayOfWeeks);
        HashSet<Integer> setDays = mAlarm.mDaysOfWeak.getSetDays();
        for (int i = 0; i < 7; i++) {
            final int index = i;
            mDayBtns[index] = (SquareToggleButton) dayBtnLL.getChildAt(index);
            if (setDays.contains(DAY_ORDER[i])) {
                mDayBtns[index].setChecked(true);
            } else {
                mDayBtns[index].setChecked(false);
            }
            mDayBtns[index].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mAlarm.mDaysOfWeak.setDaysOfWeek(isChecked, DAY_ORDER[index]);
                }
            });
        }
        // 关闹钟
        findViewById(R.id.rl_close_type).setOnClickListener(this);
        mCloseTypeTV = (TextView) findViewById(R.id.tv_close_type);
        if (mAlarm.mCloseType == Alarm.TYPE_NONE) {
            mCloseTypeTV.setText("无");
        } else if (mAlarm.mCloseType == Alarm.TYPE_MATH) {
            mCloseTypeTV.setText("数学");
        } else if (mAlarm.mCloseType == Alarm.TYPE_CODE) {
            mCloseTypeTV.setText("扫码");
        }
        // 打开开扫码
        mCodeRL = (RelativeLayout) findViewById(R.id.rl_code);
        mCodeRL.setOnClickListener(this);
        if (mAlarm.mCloseType == Alarm.TYPE_CODE) {
            mCodeRL.setVisibility(View.VISIBLE);
        } else {
            mCodeRL.setVisibility(View.GONE);
        }
    }

    @Override
    public void onValueSelected(int pickerIndex, int newValue, boolean autoAdvance) {
        if (pickerIndex == HOUR_INDEX) {
            String hours = String.format("%02d", newValue);
            mHoursView.setText(hours);
            mHoursView.setTextColor(mUnSelectedColor);
            mMinutesView.setTextColor(mSelectedColor);
            mTimePicker.setCurrentItemShowing(MINUTE_INDEX, true);
            ObjectAnimator pulseAnimator1 = Utils.getPulseAnimator(mMinutesView, 0.85f, 1.1f);
            pulseAnimator1.setStartDelay(PULSE_ANIMATOR_DELAY);
            pulseAnimator1.start();
            mAlarm.mHour = newValue;
        } else if (pickerIndex == MINUTE_INDEX) {
            String minutes = String.format("%02d", newValue);
            mMinutesView.setText(minutes);
            mAlarm.mMinutes = newValue;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_hours:
                mTimePicker.setCurrentItemShowing(HOUR_INDEX, true);
                mHoursView.setTextColor(mSelectedColor);
                mMinutesView.setTextColor(mUnSelectedColor);
                ObjectAnimator pulseAnimator1 = Utils.getPulseAnimator(mHoursView, 0.85f, 1.1f);
                pulseAnimator1.setStartDelay(PULSE_ANIMATOR_DELAY);
                pulseAnimator1.start();
                break;
            case R.id.tv_minutes:
                mTimePicker.setCurrentItemShowing(MINUTE_INDEX, true);
                mHoursView.setTextColor(mUnSelectedColor);
                mMinutesView.setTextColor(mSelectedColor);
                ObjectAnimator pulseAnimator2 = Utils.getPulseAnimator(mMinutesView, 0.85f, 1.1f);
                pulseAnimator2.setStartDelay(PULSE_ANIMATOR_DELAY);
                pulseAnimator2.start();
                break;
            case R.id.rl_ringtone:
                launchRingTonePicker(mAlarm);
                break;
            case R.id.rl_vibrate:
                mVibrateSwitch.toggle();
                mAlarm.mVibrate = mVibrateSwitch.isChecked();
                break;
            case R.id.tv_label:
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                final Fragment prev = getSupportFragmentManager().findFragmentByTag("label_fragment");
                if (prev != null) {
                    ft.remove(prev);
                }

                final LabelDialogFragment newFragment = LabelDialogFragment.newInstance(mAlarm, mAlarm.mLabel, "", this);
                ft.add(newFragment, "label_fragment");
                ft.commitAllowingStateLoss();
                getFragmentManager().executePendingTransactions();
                break;
            case R.id.rl_close_type:
                RadioDialogFragment radioDialogFragment = RadioDialogFragment.newInstance(mAlarm, new RadioDialogFragment.OnRadioSelected() {
                    @Override
                    public void onRadioSelected(int type) {
                        mAlarm.mCloseType = type;
                        if (type == Alarm.TYPE_NONE) {
                            mCloseTypeTV.setText("无");
                            mCodeRL.setVisibility(View.GONE);
                        } else if (type == Alarm.TYPE_MATH) {
                            mCloseTypeTV.setText("数学");
                            mCodeRL.setVisibility(View.GONE);
                        } else if (type == Alarm.TYPE_CODE) {
                            mCloseTypeTV.setText("扫码");
                            mCodeRL.setVisibility(View.VISIBLE);
                        }
                    }
                });
                radioDialogFragment.show(getSupportFragmentManager(), "radioDialog");
                break;
            case R.id.rl_code:
                new IntentIntegrator(this)
                        .setOrientationLocked(false)
                        .setCaptureActivity(CustomScanActivity.class)
                        .initiateScan();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() != null) {
                String ScanResult = intentResult.getContents();
                mAlarm.mStringCode = ScanResult;
                Toast.makeText(this, "扫描成功=" + ScanResult, Toast.LENGTH_LONG).show();
            }
            return;
        }

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_RINGTONE) {
            saveRingtoneUri(data);
            refreshRingtoneTitle();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_ok) {
            if (mAlarm.mCloseType == Alarm.TYPE_CODE) {
                if (TextUtils.isEmpty(mAlarm.mStringCode)) {
                    ToastUtils.showOnly(this, "您还未扫码，请扫码用于关闭闹钟");
                    return true;
                }
            }
            Intent intent = new Intent();
            intent.putExtra("alarm", mAlarm);
            setResult(Activity.RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogLabelSet(Alarm alarm, String label, String tag) {
        mAlarm.mLabel = label;
        mLabelTV.setText(mAlarm.getDefaultLabelOrDefind(this));
    }

    /**
     * 获取系统设置的闹钟铃声Uri
     *
     * @param context
     * @return
     */
    private Uri getSystemAlarmRingetonUrl(Context context) {
        Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM);
        if (uri == null) {
            uri = Uri.parse(SYSTEM_DEFAULT_ALARM);
        }
        return uri;
    }

    private String getRingToneTitle(Context context, Uri uri) {
        Ringtone ringTone = RingtoneManager.getRingtone(context, uri);
        String title = ringTone.getTitle(context);
        return title;
    }

    private static final int REQUEST_CODE_RINGTONE = 0x0001;

    private void launchRingTonePicker(Alarm alarm) {
        Uri oldRingtone = Alarm.NO_RINGTONE_URI.equals(alarm.mAlert) ? null : alarm.mAlert;
        final Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, oldRingtone);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        startActivityForResult(intent, REQUEST_CODE_RINGTONE);
    }

    private void saveRingtoneUri(Intent intent) {
        Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
        if (uri == null) uri = Alarm.NO_RINGTONE_URI;
        mAlarm.mAlert = uri;
    }

    private void refreshRingtoneTitle() {
        String ringtoneName;
        if (mAlarm.mAlert.equals(Alarm.NO_RINGTONE_URI)) {
            ringtoneName = getString(R.string.silent_alarm_summary);
        } else {
            if (!AlarmUtils.isRingtoneExisted(this, mAlarm.mAlert.toString())) {
                mAlarm.mAlert = getSystemAlarmRingetonUrl(this);
            }
            ringtoneName = getRingToneTitle(this, mAlarm.mAlert);
        }
        mRingtoneTV.setText(ringtoneName);
    }
}
