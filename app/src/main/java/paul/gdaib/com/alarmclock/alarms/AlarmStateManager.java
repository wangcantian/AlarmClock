package paul.gdaib.com.alarmclock.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import paul.gdaib.com.alarmclock.SettingsActivity;
import paul.gdaib.com.alarmclock.bean.Alarm;
import paul.gdaib.com.alarmclock.bean.AlarmInstance;
import paul.gdaib.com.alarmclock.utils.LogUtils;
import paul.gdaib.com.alarmclock.utils.Utils;

/**
 * Created by Paul on 2016/10/23.
 */

public class AlarmStateManager extends BroadcastReceiver implements LogUtils.LogZX {

    private static final String CHANGE_STATE_ACTION = "com.hs.myclock.CHANGE_STATE";
    public static final String ALARM_STATE_EXTRA = "intent.extra.alarm.state";
    private static final String ALARM_MANAGER_TAG = "ALARM_MANAGER";

    // 避免设置闹钟时间正好是闹钟触发时间，添加15s缓冲时间
    private static final int ALARM_FIRE_BUFFER = 15;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final PendingResult pendingResult = goAsync();
        AlarmAlertWakeLock.acquireCpuWakeLock(context);
        AlarmHandler.post(new Runnable() {
            @Override
            public void run() {
                handleIntent(context, intent);
                pendingResult.finish();
                AlarmAlertWakeLock.releaseCpuLock();
            }
        });
    }

    private void handleIntent(Context context, Intent intent) {
        if (intent.getAction().equals(CHANGE_STATE_ACTION)) {
            Uri uri = intent.getData();
            AlarmInstance instance = AlarmInstance.getInstanceById(context.getContentResolver(), AlarmInstance.getId(uri));
            if (instance == null) return;

            int globalId = getGlobalIntentId(context);
            int intentId = intent.getIntExtra(ALARM_GLOBAL_ID_EXTRA, -1);
            int alarmState = intent.getIntExtra(ALARM_STATE_EXTRA, -1);
            if (alarmState > 0) {
                setAlarmState(context, instance, alarmState);
            } else {
                registerInstance(context, instance, true);
            }
        }
    }

    private void setAlarmState(Context context, AlarmInstance instance, int state) {
        switch (state) {
            case AlarmInstance.SILENT_STATE:
                setSilentState(context, instance);
                break;
            case AlarmInstance.LOW_NOTIFICATION_STATE:
                setLowNotificationState(context, instance);
                break;
            case AlarmInstance.HIGH_NOTIFICATION_STATE:
                setHighNotificationState(context, instance);
                break;
            case AlarmInstance.HIDE_NOTIFICATION_STATE:
                setHideNotificationState(context, instance);
                break;
            case AlarmInstance.FIRED_STATE:
                setFiredState(context, instance);
                break;
            case AlarmInstance.SNOOZE_STATE:
                setSnoozeState(context, instance);
                break;
            case AlarmInstance.MISSED_STATE:
                setMissedState(context, instance);
                break;
            case AlarmInstance.DISMISSED_STATE:
                setDismissState(context, instance);
                break;
        }
    }

    public static void setSilentState(Context context, AlarmInstance instance) {
        ContentResolver contentResolver = context.getContentResolver();
        instance.mAlarmState = AlarmInstance.SILENT_STATE;
        AlarmInstance.updateInstance(contentResolver, instance);

        AlarmNotification.clearNotification(context, instance);
        scheduleInstanceStateChange(context, instance.getLowNotificationTime(), instance, AlarmInstance.LOW_NOTIFICATION_STATE);
    }

    public static void setLowNotificationState(Context context, AlarmInstance instance) {
        ContentResolver resolver = context.getContentResolver();
        instance.mAlarmState = AlarmInstance.LOW_NOTIFICATION_STATE;
        AlarmInstance.updateInstance(resolver, instance);

        AlarmNotification.showLowPriorityNotification(context, instance);
        scheduleInstanceStateChange(context, instance.getHighNotificationTime(), instance, AlarmInstance.HIGH_NOTIFICATION_STATE);
    }

    public static void setHighNotificationState(Context context, AlarmInstance instance) {
        ContentResolver resolver = context.getContentResolver();
        instance.mAlarmState = AlarmInstance.HIGH_NOTIFICATION_STATE;
        AlarmInstance.updateInstance(resolver, instance);

        AlarmNotification.showHighPriorityNofification(context, instance);
        scheduleInstanceStateChange(context, instance.getAlarmTime(), instance, AlarmInstance.FIRED_STATE);
    }

    public static void setHideNotificationState(Context context, AlarmInstance instance) {
        ContentResolver resolver = context.getContentResolver();
        instance.mAlarmState = AlarmInstance.HIDE_NOTIFICATION_STATE;
        AlarmInstance.updateInstance(resolver, instance);

        AlarmNotification.clearNotification(context, instance);
        scheduleInstanceStateChange(context, instance.getHighNotificationTime(), instance, AlarmInstance.HIGH_NOTIFICATION_STATE);
    }

    public static void setFiredState(Context context, AlarmInstance instance) {
        ContentResolver resolver = context.getContentResolver();
        instance.mAlarmState = AlarmInstance.FIRED_STATE;
        AlarmInstance.updateInstance(resolver, instance);

        AlarmService.startAlarm(context, instance);

        Calendar timeout = instance.getTimeout(context);
        if (timeout != null) {
            scheduleInstanceStateChange(context, timeout, instance, AlarmInstance.MISSED_STATE);
        }
        updateNextAlarm(context);
    }

    public static void setSnoozeState(Context context, AlarmInstance instance) {
        AlarmService.stopAlarm(context, instance);

        ContentResolver resolver = context.getContentResolver();
        instance.mAlarmState = AlarmInstance.SNOOZE_STATE;

        int snoozeMinutes = PreferenceManager.getDefaultSharedPreferences(context).getInt(SettingsActivity.KEY_ALARM_SNOOZE, SettingsActivity.DEFAULT_SNOOZE_MINUTES);
        Calendar newAlarmTime = Calendar.getInstance();
        newAlarmTime.add(Calendar.MINUTE, snoozeMinutes);
        instance.setAlarmTime(newAlarmTime);
        AlarmInstance.updateInstance(resolver, instance);

        AlarmNotification.showSnoozeNotification(context, instance);
        scheduleInstanceStateChange(context, newAlarmTime, instance, AlarmInstance.FIRED_STATE);

        updateNextAlarm(context);
    }

    public static void setMissedState(Context context, AlarmInstance instance) {
        AlarmService.stopAlarm(context, instance);
        if (instance.mAlarmId != null) {
            updateParentAlarm(context, instance);
        }

        ContentResolver resolver = context.getContentResolver();
        instance.mAlarmState = AlarmInstance.MISSED_STATE;
        AlarmInstance.updateInstance(resolver, instance);

        AlarmNotification.showMissedNotification(context, instance);
        scheduleInstanceStateChange(context, instance.getMissedTimeToLive(), instance, AlarmInstance.DISMISSED_STATE);
        updateNextAlarm(context);
    }

    public static void setDismissState(Context context, AlarmInstance instance) {
        unregisterInstance(context, instance);

        if (instance.mAlarmId != null) {
            updateParentAlarm(context, instance);
        }
        AlarmInstance.deleteInstance(context.getContentResolver(), instance);
        updateNextAlarm(context);
    }

    public static void scheduleInstanceStateChange(Context context, Calendar time, AlarmInstance instance, int state) {
        long timeInMillis = time.getTimeInMillis();
        Intent stateChangeIntent = createStateChangeIntent(context, ALARM_MANAGER_TAG, instance, state);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, instance.hashCode(), stateChangeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Utils.isKitKatOrLater()) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }

    private static void cancelScheduledInstance(Context context, AlarmInstance instance) {
        Intent intent = createStateChangeIntent(context, ALARM_MANAGER_TAG, instance, null);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, instance.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }

    public static void updateNextAlarm(Context context) {
        AlarmInstance nextAlarm = getNearestAlarm(context);
        AlarmNotification.broadcastNextAlarm(context, nextAlarm);
    }

    public static AlarmInstance getNearestAlarm(Context context) {
        AlarmInstance nextAlarm = null;
        ContentResolver cr = context.getContentResolver();
        String activeAlarmQuery = AlarmInstance.ALARM_STATE + "<" + AlarmInstance.FIRED_STATE;
        for (AlarmInstance instance : AlarmInstance.getInstances(cr, activeAlarmQuery)) {
            if (nextAlarm == null || instance.getAlarmTime().before(nextAlarm.getAlarmTime())) {
                nextAlarm = instance;
            }
        }
        return nextAlarm;
    }

    private static void updateParentAlarm(Context context, AlarmInstance instance) {
        ContentResolver contentResolver = context.getContentResolver();
        Alarm alarm = Alarm.getAlarmById(contentResolver, instance.mAlarmId);

        if (alarm == null) return;
        if (!alarm.mDaysOfWeak.isRepeating()) {
            if (alarm.mDeleteAfterUse) {
                Alarm.deteleAlarm(contentResolver, alarm.mId);
            } else {
                alarm.mEnabled = false;
                Alarm.updateAlarm(contentResolver, alarm);
            }
        } else {
            Calendar currentTime = Calendar.getInstance();
            AlarmInstance nextRepeatedInstance = alarm.createAlarmInstanceAfter(currentTime);
            AlarmInstance.addInstance(contentResolver, nextRepeatedInstance);
            registerInstance(context, nextRepeatedInstance, true);
        }
    }

    public static Intent createStateChangeIntent(Context context, String tag, AlarmInstance instance, Integer state) {
        Intent intent = AlarmInstance.createIntent(context, AlarmStateManager.class, instance.mId);
        intent.setAction(CHANGE_STATE_ACTION);
        intent.addCategory(tag);
        intent.putExtra(ALARM_GLOBAL_ID_EXTRA, getGlobalIntentId(context));
        if (state != null) {
            intent.putExtra(ALARM_STATE_EXTRA, state.intValue());
        }
        return intent;
    }

    public static void registerInstance(Context context, AlarmInstance instance, boolean updateNextAlarm) {
        Calendar currentTime = Calendar.getInstance();
        Calendar alarmTime = instance.getAlarmTime();
        Calendar timeoutTime = instance.getTimeout(context);// 响铃超时
        Calendar lowNotificationTime = instance.getLowNotificationTime();
        Calendar highNotificationTime = instance.getHighNotificationTime();
        Calendar missedTTL = instance.getMissedTimeToLive();

        if (instance.mAlarmState == AlarmInstance.DISMISSED_STATE) {
            setDismissState(context, instance);
            return;
        } else if (instance.mAlarmState == AlarmInstance.FIRED_STATE) {
            boolean hasTimeOut = timeoutTime != null && currentTime.after(timeoutTime);
            if (!hasTimeOut) {
                AlarmNotification.updateAlarmNotification(context, instance);
                setFiredState(context, instance);
                return;
            }
        } else if (instance.mAlarmState == AlarmInstance.MISSED_STATE) {
            //闹钟miss，但时间又发生了改变；
            if (currentTime.before(alarmTime)) {
                if (instance.mAlarmId == null) {
                    setDismissState(context, instance);
                    return;
                }

                ContentResolver cr = context.getContentResolver();
                Alarm alarm = Alarm.getAlarmById(cr, instance.mAlarmId);
                alarm.mEnabled = true;
                Alarm.updateAlarm(cr, alarm);
            }
        }

        if (currentTime.after(missedTTL)) {// 当前时间已经过了错过时间
            setDismissState(context, instance);
        } else if (currentTime.after(alarmTime)) {
            Calendar alarmBuffer = Calendar.getInstance();
            alarmBuffer.setTime(alarmTime.getTime());
            alarmBuffer.add(Calendar.SECOND, ALARM_FIRE_BUFFER);
            if (currentTime.before(alarmBuffer)) {
                setFiredState(context, instance);
            } else {
                setMissedState(context, instance);
            }
        } else if (instance.mAlarmState == AlarmInstance.SNOOZE_STATE) {
            AlarmNotification.showSnoozeNotification(context, instance);
            scheduleInstanceStateChange(context, instance.getAlarmTime(), instance, AlarmInstance.FIRED_STATE);
        } else if (currentTime.after(highNotificationTime)) {
            setHighNotificationState(context, instance);
        } else if (currentTime.after(lowNotificationTime)) {
            if (instance.mAlarmState == AlarmInstance.HIDE_NOTIFICATION_STATE) {
                setHideNotificationState(context, instance);
            } else {
                setLowNotificationState(context, instance);
            }
        } else {
            setSilentState(context, instance);
        }

        if (updateNextAlarm) {
            updateNextAlarm(context);
        }
    }

    public static void unregisterInstance(Context context, AlarmInstance instance) {
        AlarmService.stopAlarm(context, instance);
        AlarmNotification.clearNotification(context, instance);
        cancelScheduledInstance(context, instance);
    }

    /**
     * 删除并解注册 alarmId相关的所有alarmInstance；
     */
    public static void deleteAllInstances(Context context, long alarmId) {
        ContentResolver resolver = context.getContentResolver();
        List<AlarmInstance> instances = AlarmInstance.getInstanceByAlarmId(resolver, alarmId);

        for (AlarmInstance instance : instances) {
            unregisterInstance(context, instance);
            AlarmInstance.deleteInstance(resolver, instance);
        }
        updateNextAlarm(context);
    }

    public static void fixAlarmInstances(Context context) {
        HashMap<Long, AlarmInstance> duplicatedInstance = new HashMap<Long, AlarmInstance>();
        final ContentResolver resolver = context.getContentResolver();

        for (AlarmInstance instance : AlarmInstance.getInstances(resolver, null)) {
            if (duplicatedInstance.get(instance.mAlarmId) == null) {
                duplicatedInstance.put(instance.mAlarmId, instance);
            } else {
                AlarmInstance.deleteInstance(resolver, instance);
            }
            getFixedAlarmInstance(context, instance);
            AlarmStateManager.registerInstance(context, instance, false);
        }
        AlarmStateManager.updateNextAlarm(context);
    }

    private static AlarmInstance getFixedAlarmInstance(Context context, AlarmInstance instance) {
        ContentResolver resolver = context.getContentResolver();
        Alarm alarm = Alarm.getAlarmById(resolver, instance.mAlarmId);
        Calendar currentTime = Calendar.getInstance();// the system's current time
        AlarmInstance newInstance = alarm.createAlarmInstanceAfter(currentTime);
        Calendar newTime = newInstance.getAlarmTime();

        instance.setAlarmTime(newTime);
        AlarmInstance.updateInstance(resolver, instance);

        return instance;
    }

    private static final String ALARM_GLOBAL_ID_EXTRA = "intent.extra.alarm.global.id";

    private static int getGlobalIntentId(Context context) {
        SharedPreferences spfs = PreferenceManager.getDefaultSharedPreferences(context);
        return spfs.getInt(ALARM_GLOBAL_ID_EXTRA, -1);
    }

    public static void updateGlobalIntentId(Context context) {
        SharedPreferences spfs = PreferenceManager.getDefaultSharedPreferences(context);
        int globalId = spfs.getInt(ALARM_GLOBAL_ID_EXTRA, -1) + 1;
        spfs.edit().putInt(ALARM_GLOBAL_ID_EXTRA, globalId).commit();
    }
}
