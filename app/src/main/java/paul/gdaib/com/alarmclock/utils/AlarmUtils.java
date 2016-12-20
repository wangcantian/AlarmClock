package paul.gdaib.com.alarmclock.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.android.datetimepicker.time.TimePickerDialog;

import java.io.File;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import paul.gdaib.com.alarmclock.R;
import paul.gdaib.com.alarmclock.bean.Alarm;
import paul.gdaib.com.alarmclock.bean.AlarmInstance;

/**
 * Created by Paul on 2016/10/23.
 */

public class AlarmUtils {

    public static final String FRAG_TAG_TIME_PICKER = "time_dialog";

    private static String formatToast(Context context, long timeInMillis) {
        long delta = timeInMillis - System.currentTimeMillis();
        long hours = delta / (1000 * 60 * 60);
        long minutes = delta / (1000 * 60) % 60;
        long days = hours / 24;
        hours = hours % 24;

        String daySeq = (days == 0) ? "" :
                (days == 1) ? context.getString(R.string.day) :
                        context.getString(R.string.days, Long.toString(days));

        String minSeq = (minutes == 0) ? "" :
                (minutes == 1) ? context.getString(R.string.minute) :
                        context.getString(R.string.minutes, Long.toString(minutes));

        String hourSeq = (hours == 0) ? "" :
                (hours == 1) ? context.getString(R.string.hour) :
                        context.getString(R.string.hours, Long.toString(hours));

        boolean dispDays = days > 0;
        boolean dispHour = hours > 0;
        boolean dispMinute = minutes > 0;

        int index = (dispDays ? 1 : 0) |
                (dispHour ? 2 : 0) |
                (dispMinute ? 4 : 0);

        String[] formats = context.getResources().getStringArray(R.array.alarm_set);
        return String.format(formats[index], daySeq, hourSeq, minSeq);
    }

    public static void popAlarmSetToast(Context context, long timeInMillis) {
        String toastText = formatToast(context, timeInMillis);
        ToastUtils.showOnly(context, toastText);
    }

    public static String getAlarmText(Context context, AlarmInstance instance) {
        String date = getFormattedTime(context, instance.getAlarmTime());
        return !TextUtils.isEmpty(instance.mLabel) ? date + " - " + instance.mLabel : date;
    }

    public static String getFormattedTime(Context context, Calendar time) {
        SimpleDateFormat format = new SimpleDateFormat(DateFormat.is24HourFormat(context) ? "yyyy-MM-dd HH:mm" : "yyyy-MM-dd hh:mm");
        return format.format(time.getTime());
    }

    public static boolean isRingtoneExisted(Context context, String ringtone) {
        boolean result = false;
        if (!TextUtils.isEmpty(ringtone)) {
            if (ringtone.contains("internal")) {
                return true;
            }
            String path = getRingtonePath(context, ringtone);
            if (!TextUtils.isEmpty(path)) {
                result = new File(path).exists();
            }
        }
        return result;
    }

    private static String getRingtonePath(final Context mContext, final String alrmRingtone) {
        String filepath = null;
        final ContentResolver resolver = mContext.getContentResolver();

        if (!TextUtils.isEmpty(alrmRingtone)) {
            Cursor c = null;
            try {
                c = resolver.query(Uri.parse(alrmRingtone), null, null, null, null);
                if (c != null && c.moveToFirst()) {
                    filepath = c.getColumnName(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
            }

        }
        return filepath;
    }

    public static void showTimeEditDialog(FragmentManager manager, final Alarm alarm, TimePickerDialog.OnTimeSetListener listener, boolean is24HourMode) {
        int hour, minutes;
        if (alarm == null) {
            hour = 0;
            minutes = 0;
        } else {
            hour = alarm.mHour;
            minutes = alarm.mMinutes;
        }

        TimePickerDialog dialog = TimePickerDialog.newInstance(listener, hour, minutes, is24HourMode);
        dialog.setThemeDark(false);//时间选择框 主题风格选择

        manager.executePendingTransactions();// 确保没有被添加过
        final FragmentTransaction ft = manager.beginTransaction();

        if (dialog != null && !dialog.isAdded()) {
            ft.add(dialog, FRAG_TAG_TIME_PICKER);
            ft.commitAllowingStateLoss();
        }
    }

}
