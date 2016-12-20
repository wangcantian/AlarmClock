package paul.gdaib.com.alarmclock;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import paul.gdaib.com.alarmclock.adapter.AlarmAdapter;
import paul.gdaib.com.alarmclock.alarms.AlarmStateManager;
import paul.gdaib.com.alarmclock.bean.Alarm;
import paul.gdaib.com.alarmclock.bean.AlarmInstance;
import paul.gdaib.com.alarmclock.utils.AlarmUtils;
import paul.gdaib.com.alarmclock.utils.LogUtils;
import paul.gdaib.com.alarmclock.view.LabelDialogFragment;
import paul.gdaib.com.alarmclock.view.recycleView.RecycleViewDivider;
import paul.gdaib.com.alarmclock.view.recycleView.SwapRecyclerView;

/**
 * A placeholder fragment containing a simple view.
 */
public class AlarmClockFragment extends Fragment implements
        AlarmAdapter.OnEditClickListener,
        OperationAlarm,
        FragmentPresenter,
        TimePickerDialog.OnTimeSetListener,
        LabelDialogFragment.AlarmLabelDialogHandler {

    private static final String TAG = AlarmClockFragment.class.getSimpleName();
    private static final String SYSTEM_DEFAULT_ALARM = "content://settings/system/alarm_alert";
    private static final String KEY_ALARM_RINGTONE = "alarm_ringtone";
    public static final String TAG_LABEL_FRAGMENT = "label_fragment";
    public static final int REQUEST_CODE_ADD = 1;
    public static final int REQUEST_CODE_EDIT = 2;

    private SwapRecyclerView mRecyclerView;
    private AlarmAdapter mAlarmAdapter;
    private List<Alarm> mAlarList;

    public AlarmClockFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setVolumeControlStream(AudioManager.STREAM_ALARM);

        if (TextUtils.isEmpty(getPrefsAlarmRingtone(getContext()))) {
            setApplicationAlarmRingtoneToPrefs(getContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView = (SwapRecyclerView) getView().findViewById(R.id.alarm_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.VERTICAL, 5, getResources().getColor(android.R.color.transparent)));
        mAlarList = new ArrayList<>();
        ContentResolver contentResolver = getContext().getContentResolver();
        mAlarList.addAll(Alarm.getAlarmsBySelection(contentResolver, null, null, Alarm.DEFAULT_SORT_ORDER));
        mAlarmAdapter = new AlarmAdapter(getActivity(), this, this, mRecyclerView, mAlarList, this);
        mRecyclerView.setAdapter(mAlarmAdapter);
    }

    /**
     * 获得保存的闹钟铃声
     *
     * @param context
     * @return
     */
    private String getPrefsAlarmRingtone(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String ringtone = prefs.getString(KEY_ALARM_RINGTONE, "");
        return ringtone;
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

    /**
     * 将系统默认铃声设置为程序默认铃声并写入pre文件中
     *
     * @param context
     */
    private void setApplicationAlarmRingtoneToPrefs(Context context) {
        Uri uri = getSystemAlarmRingetonUrl(context);
        setApplicationAlarmRingtone(context, uri.toString());
    }

    /**
     * 设置程序的默认铃声
     *
     * @param ringtone
     */
    private void setApplicationAlarmRingtone(Context context, String ringtone) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(KEY_ALARM_RINGTONE, ringtone).apply();
    }

    private static AlarmInstance setupAlarmInstance(Context context, Alarm alarm) {
        ContentResolver resolver = context.getContentResolver();
        AlarmInstance newInstance = alarm.createAlarmInstanceAfter(Calendar.getInstance());
        newInstance = AlarmInstance.addInstance(resolver, newInstance);
        AlarmStateManager.registerInstance(context, newInstance, true);
        return newInstance;
    }

    @Override
    public void onEditClick(int position, Alarm alarm) {
        Intent intent = new Intent(getActivity(), AddAlarmActivity.class);
        intent.putExtra("flags", REQUEST_CODE_EDIT);
        intent.putExtra("alarmId", alarm.mId);
        startActivityForResult(intent, REQUEST_CODE_EDIT);
    }

    // 按下添加按钮
    public void onFloatingActionClick() {
        if (mRecyclerView.isMenuOpen()) {
            mRecyclerView.smoothCloseMenu();
            return;
        }

        Intent intent = new Intent(getActivity(), AddAlarmActivity.class);
        intent.putExtra("flags", REQUEST_CODE_ADD);
        startActivityForResult(intent, REQUEST_CODE_ADD);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_ADD) {
                Alarm alarm = data.getParcelableExtra("alarm");
                LogUtils.e(TAG, alarm.toString());
                asyncAddAlarm(alarm);
            } else if (requestCode == REQUEST_CODE_EDIT) {
                Alarm alarm = data.getParcelableExtra("alarm");
                asyncUpdateAlarm(alarm, alarm.mEnabled);
            }
        }
    }

    @Override
    public void asyncDeleteAlarm(final Alarm alarm) {
        final Context context = AlarmClockFragment.this.getActivity().getApplicationContext();
        final AsyncTask<Void, Void, Void> deleteTask = new AsyncTask<Void, Void, Void>() {

            public synchronized void onPreExecute() {

            }

            @Override
            protected Void doInBackground(Void... params) {
                if (context != null && alarm != null) {
                    ContentResolver resolver = context.getContentResolver();
                    AlarmStateManager.deleteAllInstances(context, alarm.mId);
                    Alarm.deteleAlarm(resolver, alarm.mId);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAlarList.remove(alarm);
                mAlarmAdapter.notifyDataSetChanged();
            }
        };
        deleteTask.execute();
    }

    @Override
    public void asyncAddAlarm(final Alarm alarm) {
        final Context context = AlarmClockFragment.this.getActivity().getApplicationContext();
        final AsyncTask<Void, Void, AlarmInstance> addTask = new AsyncTask<Void, Void, AlarmInstance>() {

            @Override
            public synchronized void onPreExecute() {

            }

            @Override
            protected AlarmInstance doInBackground(Void... params) {
                if (context != null && alarm != null) {
                    ContentResolver reslover = context.getContentResolver();
                    Alarm newAlarm = Alarm.addAlarm(reslover, alarm);
                    if (newAlarm.mEnabled) {
                        return setupAlarmInstance(context, newAlarm);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(AlarmInstance instance) {
                mAlarList.add(alarm);
                mAlarmAdapter.notifyDataSetChanged();
                if (instance != null) {
                    AlarmUtils.popAlarmSetToast(context, instance.getAlarmTime().getTimeInMillis());
                }
            }

        };

        addTask.execute();
    }

    @Override
    public void asyncUpdateAlarm(final Alarm alarm, final boolean popToast) {
        final Context context = AlarmClockFragment.this.getActivity().getApplicationContext();
        final AsyncTask<Void, Void, AlarmInstance> updateTask =
                new AsyncTask<Void, Void, AlarmInstance>() {

                    @Override
                    protected AlarmInstance doInBackground(Void... params) {
                        ContentResolver cr = context.getContentResolver();
                        AlarmStateManager.deleteAllInstances(context, alarm.mId);
                        Alarm.updateAlarm(cr, alarm);

                        if (alarm.mEnabled) {
                            return setupAlarmInstance(context, alarm);
                        }
                        return null;
                    }

                    protected void onPostExecute(AlarmInstance instance) {
                        mAlarList.set(mAlarList.indexOf(alarm), alarm);
                        mAlarmAdapter.notifyDataSetChanged();
                        if (popToast && instance != null) {
                            AlarmUtils.popAlarmSetToast(context, instance.getAlarmTime().getTimeInMillis());
                        }
                    }

                };
        updateTask.execute();
    }

    /**
     * 设置闹钟标签
     */
    public void setLabel(Alarm alarm, String label) {
        alarm.mLabel = label;
        asyncUpdateAlarm(alarm, false);
    }

    private Alarm mSelectedAlarm;

    @Override
    public void showTimeDialog(Alarm alarm) {
        mSelectedAlarm = alarm;
        AlarmUtils.showTimeEditDialog(getChildFragmentManager(), alarm, this, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void showLabelDialog(Alarm alarm) {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final Fragment prev = getFragmentManager().findFragmentByTag(TAG_LABEL_FRAGMENT);
        if (prev != null) {
            ft.remove(prev);
        }

        final LabelDialogFragment newFragment = LabelDialogFragment.newInstance(alarm, alarm.mLabel, getTag(), this);
        ft.add(newFragment, TAG_LABEL_FRAGMENT);
        ft.commitAllowingStateLoss();
        getFragmentManager().executePendingTransactions();
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        if (mSelectedAlarm != null) {
            mSelectedAlarm.mHour = hourOfDay;
            mSelectedAlarm.mMinutes = minute;
            mSelectedAlarm.mEnabled = true;
            asyncUpdateAlarm(mSelectedAlarm, true);
            mSelectedAlarm = null;
        }
    }

    @Override
    public void onDialogLabelSet(Alarm alarm, String label, String tag) {
        setLabel(alarm, label);
    }
}
