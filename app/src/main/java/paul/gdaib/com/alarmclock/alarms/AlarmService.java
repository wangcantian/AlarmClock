package paul.gdaib.com.alarmclock.alarms;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.util.Currency;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import paul.gdaib.com.alarmclock.bean.AlarmInstance;

/**
 * Created by Paul on 2016/10/24.
 */

public class AlarmService extends Service {

    public static final String PRE_SHUT_DOWN = "android.intent.action.ACTION_PRE_SHUTDOWN";

    public static final String ALARM_DONE_ACTION = "com.android.deskclock.ALARM_DONE";
    public static final String ALARM_ALERT_ACTION="com.android.deskclock.ALARM_ALERT";
    public static final String START_ALARM_ACTION = "com.hs.myClock.START_ALARM";
    public static final String STOP_ALARM_ACTION = "com.hs.myClock.STOP_ALARM";

    private TelephonyManager mTelephonyManager;
    private int mInitialCallState;
    private AlarmInstance mCurrentAlarm = null;

    private final BroadcastReceiver mStopPlayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mCurrentAlarm == null) return;
            AlarmStateManager.setDismissState(context, mCurrentAlarm);
        }
    };

    public static void startAlarm(Context context, AlarmInstance instance) {
        Intent intent = AlarmInstance.createIntent(context, AlarmService.class, instance.mId);
        intent.setAction(START_ALARM_ACTION);
        AlarmAlertWakeLock.acquireCpuWakeLock(context);
        context.startService(intent);
    }

    public static void stopAlarm(Context context, AlarmInstance instance) {
        Intent intent = AlarmInstance.createIntent(context, AlarmService.class, instance.mId);
        intent.setAction(STOP_ALARM_ACTION);
        context.startService(intent);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long instanceId = AlarmInstance.getId(intent.getData());
        AlarmInstance instance = null;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PRE_SHUT_DOWN);
        registerReceiver(mStopPlayReceiver, intentFilter);

        if (intent.getAction().equals(START_ALARM_ACTION)) {
            ContentResolver contentResolver = getContentResolver();
            instance = AlarmInstance.getInstanceById(contentResolver, instanceId);
            if (instance == null) {
                AlarmAlertWakeLock.releaseCpuLock();
                return Service.START_NOT_STICKY;
            } else if (mCurrentAlarm != null) {
                if (mCurrentAlarm.mId == instance.mId) {
                    return Service.START_NOT_STICKY;
                } else if (mCurrentAlarm.getAlarmTime() == instance.getAlarmTime()) {
                    AlarmStateManager.setMissedState(this, instance);
                    return Service.START_NOT_STICKY;
                }
            }
            startAlarmKlaxon(instance);
        } else if (intent.getAction().equals(STOP_ALARM_ACTION)) {
            stopSelf();
        }

        return Service.START_NOT_STICKY;
    }

    private void startAlarmKlaxon(AlarmInstance instance) {
        if (mCurrentAlarm != null) {
            AlarmStateManager.setMissedState(this, mCurrentAlarm);
            stopCurrentAlarm();
        }

        AlarmAlertWakeLock.acquireCpuWakeLock(this);
        mCurrentAlarm = instance;
        initTelephonyService();
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        boolean inCall = mInitialCallState != TelephonyManager.CALL_STATE_IDLE;
        if (inCall) {
            AlarmNotification.updateAlarmNotification(this, mCurrentAlarm);
        } else {
            AlarmNotification.showAlarmNotification(this, mCurrentAlarm);
        }
        AlarmKlaxon.start(this, mCurrentAlarm, inCall);
        sendBroadcast(new Intent(ALARM_ALERT_ACTION));
    }

    private void stopCurrentAlarm() {
        if (mCurrentAlarm == null) {
            sendBroadcast(new Intent(ALARM_DONE_ACTION));
            return;
        }

        AlarmKlaxon.stop(this);
        sendBroadcast(new Intent(ALARM_DONE_ACTION));
        mCurrentAlarm = null;
        AlarmAlertWakeLock.releaseCpuLock();
    }

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            int newPhoneState = mInitialCallState;
            newPhoneState = mTelephonyManager.getCallState();

            if (mCurrentAlarm == null) return;
            if (state != TelephonyManager.CALL_STATE_IDLE && mInitialCallState == TelephonyManager.CALL_STATE_IDLE) {
                sendBroadcast(AlarmStateManager.createStateChangeIntent(AlarmService.this, "AlarmSerivce", mCurrentAlarm, AlarmInstance.MISSED_STATE));
            }
            if (newPhoneState == TelephonyManager.CALL_STATE_IDLE && state == TelephonyManager.CALL_STATE_IDLE && state != mInitialCallState) {
                if (mCurrentAlarm.mAlarmState == AlarmInstance.FIRED_STATE) {
                    startAlarm(AlarmService.this, mCurrentAlarm);
                }
            }
        }
    };

    private void initTelephonyService() {
        mInitialCallState = mTelephonyManager.getCallState();
    }

    @Override
    public void onDestroy() {
        stopCurrentAlarm();
        unregisterReceiver(mStopPlayReceiver);
        super.onDestroy();
    }
}
