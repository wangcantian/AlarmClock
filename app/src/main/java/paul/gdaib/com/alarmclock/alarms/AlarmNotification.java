package paul.gdaib.com.alarmclock.alarms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import paul.gdaib.com.alarmclock.AlarmActivity;
import paul.gdaib.com.alarmclock.R;
import paul.gdaib.com.alarmclock.bean.Alarm;
import paul.gdaib.com.alarmclock.bean.AlarmInstance;
import paul.gdaib.com.alarmclock.utils.AlarmUtils;

/**
 * Created by Paul on 2016/10/23.
 */

public class AlarmNotification {

    public static void showLowPriorityNotification(Context context, AlarmInstance instance) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.alarm_alert_predismiss_title))
                .setContentText(AlarmUtils.getAlarmText(context, instance))
                .setSmallIcon(R.mipmap.stat_notify_alarm)
                .setOngoing(false)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_DEFAULT);

        Intent hideIntent = AlarmStateManager.createStateChangeIntent(context, "DELETE_TAG", instance, AlarmInstance.HIDE_NOTIFICATION_STATE);
        builder.setDeleteIntent(PendingIntent.getBroadcast(context, instance.hashCode(), hideIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        nm.cancel(instance.hashCode());
        nm.notify(instance.hashCode(), builder.build());
    }

    public static void showHighPriorityNofification(Context context, AlarmInstance instance) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.alarm_alert_predismiss_title))
                .setContentText(AlarmUtils.getAlarmText(context, instance))
                .setSmallIcon(R.mipmap.stat_notify_alarm)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_HIGH);

        nm.cancel(instance.hashCode());
        nm.notify(instance.hashCode(), builder.build());
    }

    public static void showSnoozeNotification(Context context, AlarmInstance instance) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(instance.getLabelOrDefault(context))
                .setContentText(context.getString(R.string.alarm_alert_snooze_until, AlarmUtils.getFormattedTime(context, instance.getAlarmTime())))
                .setSmallIcon(R.mipmap.stat_notify_alarm)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_MAX);

        nm.cancel(instance.hashCode());
        nm.notify(instance.hashCode(), builder.build());
    }

    public static void showMissedNotification(Context context, AlarmInstance instance) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String label = instance.mLabel;
        String alarmTime = AlarmUtils.getFormattedTime(context, instance.getAlarmTime());
        String contentTextString = TextUtils.isEmpty(instance.mLabel) ? alarmTime : context.getString(R.string.alarm_missed_text, alarmTime, label);
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.alarm_missed_title))
                .setContentText(contentTextString)
                .setSmallIcon(R.mipmap.stat_notify_alarm)
                .setPriority(Notification.PRIORITY_MAX);

        nm.cancel(instance.hashCode());
        nm.notify(instance.hashCode(), builder.build());
    }

    public static void showAlarmNotification(Context context, AlarmInstance instance) {
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));//关闭当前打开窗口

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(instance.getLabelOrDefault(context))
                .setContentText(AlarmUtils.getFormattedTime(context, instance.getAlarmTime()))
                .setSmallIcon(R.mipmap.stat_notify_alarm)
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setPriority(Notification.PRIORITY_MAX);

        // 多睡一会儿
        Intent snoozeIntent = AlarmStateManager.createStateChangeIntent(context, "SNOOZE_TAG", instance, AlarmInstance.SNOOZE_STATE);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, instance.hashCode(), snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.mipmap.stat_notify_alarm, context.getString(R.string.alarm_alert_snooze_text), snoozePendingIntent);

        // 取消
        Intent dismissIntent = AlarmStateManager.createStateChangeIntent(context, "DISMISS_TAG", instance, AlarmInstance.DISMISSED_STATE);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, instance.hashCode(), dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.alarm_alert_dismiss_text), dismissPendingIntent);

        Intent fullScreenIntent = AlarmInstance.createIntent(context, AlarmActivity.class, instance.mId);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        builder.setFullScreenIntent(PendingIntent.getActivity(context,
                instance.hashCode(), fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT), true);

        nm.cancel(instance.hashCode());
        nm.notify(instance.hashCode(), builder.build());
    }

    public static void updateAlarmNotification(Context context, AlarmInstance instance) {
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(instance.getLabelOrDefault(context))
                .setContentText(AlarmUtils.getFormattedTime(context, instance.getAlarmTime()))
                .setSmallIcon(R.mipmap.stat_notify_alarm)
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setPriority(Notification.PRIORITY_MAX);

        Intent snoozeIntent = AlarmStateManager.createStateChangeIntent(context, AlarmStateManager.ALARM_STATE_EXTRA, instance, AlarmInstance.SNOOZE_STATE);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, instance.hashCode(), snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.mipmap.stat_notify_alarm, context.getString(R.string.alarm_alert_snooze_text), snoozePendingIntent);

        Intent dismissIntent = AlarmStateManager.createStateChangeIntent(context, AlarmStateManager.ALARM_STATE_EXTRA, instance, AlarmInstance.DISMISSED_STATE);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, instance.hashCode(), dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.alarm_alert_dismiss_text), dismissPendingIntent);

        nm.cancel(instance.hashCode());
        nm.notify(instance.hashCode(), builder.build());
    }

    public static void clearNotification(Context context, AlarmInstance instance) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(instance.hashCode());
    }

    public static final String SYSTEM_ALARM_CHANGE_ACTION = "android.intent.action.ALARM_CHANGED";

    public static void broadcastNextAlarm(Context context, AlarmInstance instance) {
        String timeString = "";
        boolean showStatusIcon = false;
        if (instance != null) {
            timeString = AlarmUtils.getFormattedTime(context, instance.getAlarmTime());
            showStatusIcon = true;
        }

        Settings.System.putString(context.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED, timeString);
        Intent alarmChanged = new Intent(SYSTEM_ALARM_CHANGE_ACTION);
        alarmChanged.putExtra("alarmSet", showStatusIcon);//在systemUI里接收这个广播
        context.sendBroadcast(alarmChanged);
    }
}
