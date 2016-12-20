package paul.gdaib.com.alarmclock.bean;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import paul.gdaib.com.alarmclock.R;
import paul.gdaib.com.alarmclock.SettingsActivity;

/**
 * Created by Paul on 2016/10/21.
 */

public class AlarmInstance implements ClockContract.InstanceColumns {

    public static final String TABLE_NAME = "alarm_instance";
    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY, "
            + YEAR + "  INTEGER NOT NULL, "
            + MONTH + " INTEGER NOT NULL, "
            + DAY + " INTEGER NOT NULL, "
            + HOUR + " INTEGER NOT NULL, "
            + MINUTES + " INTEGER NOT NULL, "
            + LABEL + " TEXT NOT NULL, "
            + VIBRATE + " INTEGER NOT NULL, "
            + RINGTONE + " TEXT, "
            + ALARM_STATE + " INTEGER NOT NULL, "
            + ALARM_ID + " INTEGER REFERENCES "
            + Alarm.TABLE_NAME + "(" + _ID + ")"
            + "ON UPDATE CASCADE ON DELETE CASCADE, "
            + CLOSE_TYPE + " INTEGER NOT NULL, "
            + STRING_CODE + " TEXT);";

    private static final String[] QUERY_COLUMNS = {
            _ID,
            YEAR,
            MONTH,
            DAY,
            HOUR,
            MINUTES,
            LABEL,
            VIBRATE,
            RINGTONE,
            ALARM_ID,
            ALARM_STATE,
            CLOSE_TYPE,
            STRING_CODE
    };

    /**
     * 距离闹钟开始时间
     */
    public static final int LOW_NOTIFUCATION_HOUR_OFFSET = -2;

    /**
     * 距离闹钟开始时间
     */
    public static final int HIGH_NOTIFICATION_MINUTE_OFFSET = -30;

    /**
     * 错过通知时间（小时）
     */
    private static final int MISSED_TIME_TO_LIVE_HOUR_OFFSET = 12;

    /**
     * 闹钟超时时间（分钟）
     */
    private static final int DEFAULT_ALARM_TIMEOUT_SETTING = 10;

    // 闹钟intance参数
    public long mId;
    public int mYear;
    public int mMonth;
    public int mDay;
    public int mHour;
    public int mMinute;
    public String mLabel;
    public boolean mVibrate;
    public Uri mRingtone;
    public Long mAlarmId;
    public int mAlarmState;
    public int mCloseType;
    public String mStringCode;

    // 字段索引
    private static final int ID_INDEX = 0;
    private static final int YEAR_INDEX = 1;
    private static final int MONTH_INDEX = 2;
    private static final int DAY_INDEX = 3;
    private static final int HOUR_INDEX = 4;
    private static final int MINUTES_INDEX = 5;
    private static final int LABEL_INDEX = 6;
    private static final int VIBRATE_INDEX = 7;
    private static final int RINGTONE_INDEX = 8;
    private static final int ALARM_ID_INDEX = 9;
    private static final int ALARM_STATE_INDEX = 10;
    private static final int CLOSE_TYPE_INDEX = 11;
    private static final int STRING_CODE_INDEX = 12;
    private static final int COLUMN_COUNT = STRING_CODE_INDEX + 1;

    public AlarmInstance(Calendar calendar) {
        mId = INVALID_ID;
        mLabel = "";
        mVibrate = false;
        mRingtone = null;
        mAlarmState = SILENT_STATE;
        this.mCloseType = TYPE_NONE;
        this.mStringCode = "";
        setAlarmTime(calendar);
    }

    public AlarmInstance(Calendar calendar, long alarmId) {
        this(calendar);
        mAlarmId = alarmId;
    }

    public AlarmInstance(Cursor c) {
        mId = c.getLong(ID_INDEX);
        mYear = c.getInt(YEAR_INDEX);
        mMonth = c.getInt(MONTH_INDEX);
        mDay = c.getInt(DAY_INDEX);
        mHour = c.getInt(HOUR_INDEX);
        mMinute = c.getInt(MINUTES_INDEX);
        mLabel = c.getString(LABEL_INDEX);
        mVibrate = c.getInt(VIBRATE_INDEX) == 1;

        if (c.isNull(RINGTONE_INDEX)) {
            mRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        } else {
            mRingtone = Uri.parse(c.getString(RINGTONE_INDEX));
        }

        if (!c.isNull(ALARM_ID_INDEX)) {
            mAlarmId = c.getLong(ALARM_ID_INDEX);
        }

        mAlarmState = c.getInt(ALARM_STATE_INDEX);
        this.mCloseType = c.getInt(CLOSE_TYPE_INDEX);
        if (c.isNull(STRING_CODE_INDEX)) {
            this.mStringCode = "";
        } else {
            this.mStringCode = c.getString(STRING_CODE_INDEX);
        }
    }

    public void setAlarmTime(Calendar calendar) {
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
    }

    public Calendar getAlarmTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, mYear);
        calendar.set(Calendar.MONTH, mMonth);
        calendar.set(Calendar.DAY_OF_MONTH, mDay);
        calendar.set(Calendar.HOUR_OF_DAY, mHour);
        calendar.set(Calendar.MINUTE, mMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AlarmInstance)) return false;
        final AlarmInstance other = (AlarmInstance) o;
        return mId == other.mId;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(mId).hashCode();
    }


    public String toString() {
        return "AlarmInstance{" +
                "mId=" + mId +
                ", mYear=" + mYear +
                ", mMonth=" + mMonth +
                ", mDay=" + mDay +
                ", mHour=" + mHour +
                ", mMinute=" + mMinute +
                ", mLabel=" + mLabel +
                ", mVibrate=" + mVibrate +
                ", mRingtone=" + mRingtone +
                ", mAlarmId=" + mAlarmId +
                ", mAlarmState=" + mAlarmState +
                "}";
    }

    /**
     * 获得闹钟通知时间
     *
     * @return
     */
    public Calendar getLowNotificationTime() {
        Calendar calendar = getAlarmTime();
        calendar.add(Calendar.HOUR_OF_DAY, LOW_NOTIFUCATION_HOUR_OFFSET);
        return calendar;
    }

    /**
     * 获得闹钟通知时间
     *
     * @return
     */
    public Calendar getHighNotificationTime() {
        Calendar calendar = getAlarmTime();
        calendar.add(Calendar.MINUTE, HIGH_NOTIFICATION_MINUTE_OFFSET);
        return calendar;
    }

    /**
     * 获得错过闹钟的通知时间
     *
     * @return
     */
    public Calendar getMissedTimeToLive() {
        Calendar calendar = getAlarmTime();
        calendar.add(Calendar.HOUR, MISSED_TIME_TO_LIVE_HOUR_OFFSET);
        return calendar;
    }

    /**
     * 闹钟响时，无操作的时间
     *
     * @param context
     * @return
     */
    public Calendar getTimeout(Context context) {
        int timeoutMinute = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(SettingsActivity.KEY_AUTO_SILENCE, DEFAULT_ALARM_TIMEOUT_SETTING);

        if (timeoutMinute < 0) {
            return null;
        }

        Calendar calendar = getAlarmTime();
        calendar.add(Calendar.MINUTE, timeoutMinute);
        return calendar;
    }

    public String getLabelOrDefault(Context context) {
        return mLabel.isEmpty() ? context.getString(R.string.default_label) : mLabel;
    }

    public static ContentValues createContentValues(AlarmInstance instance) {
        ContentValues contentValues = new ContentValues(COLUMN_COUNT);
        if (instance.mId != INVALID_ID) {
            contentValues.put(_ID, instance.mId);
        }
        contentValues.put(YEAR, instance.mYear);
        contentValues.put(MONTH, instance.mMonth);
        contentValues.put(DAY, instance.mDay);
        contentValues.put(HOUR, instance.mHour);
        contentValues.put(MINUTES, instance.mMinute);
        contentValues.put(LABEL, instance.mLabel);
        contentValues.put(VIBRATE, instance.mVibrate ? 1 : 0);
        if (instance.mRingtone == null) {
            contentValues.putNull(RINGTONE);
        } else {
            contentValues.put(RINGTONE, instance.mRingtone.toString());
        }
        contentValues.put(ALARM_ID, instance.mAlarmId);
        contentValues.put(ALARM_STATE, instance.mAlarmState);
        contentValues.put(CLOSE_TYPE, instance.mCloseType);
        contentValues.put(STRING_CODE, instance.mStringCode);
        return contentValues;
    }

    public static Uri getUri(long intanceId) {
        return ContentUris.withAppendedId(CONTENT_URI, intanceId);
    }

    public static long getId(Uri contentUri) {
        return ContentUris.parseId(contentUri);
    }

    /**
     * 通过instanceId获取闹钟实例
     */
    public static AlarmInstance getInstanceById(ContentResolver contentResolver, long intanceId) {
        Cursor cursor = contentResolver.query(getUri(intanceId), QUERY_COLUMNS, null, null, null);
        AlarmInstance result = null;
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                result = new AlarmInstance(cursor);
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    /**
     * 指定条件获取闹钟实例
     */
    public static List<AlarmInstance> getInstances(ContentResolver contentResolver, String selection, String... selectionArgs) {
        Cursor cursor = contentResolver.query(CONTENT_URI, QUERY_COLUMNS, selection, selectionArgs, null);
        List<AlarmInstance> result = new LinkedList<>();
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                do {
                    result.add(new AlarmInstance(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    /**
     * 通过alarmId获取闹钟实例
     */
    public static List<AlarmInstance> getInstanceByAlarmId(ContentResolver contentResolver, long alarmId) {
        return getInstances(contentResolver, ALARM_ID + "=" + alarmId);
    }

    /**
     * 添加instance
     *
     * @param contentResolver
     * @param alarmInstance
     * @return
     */
    public static AlarmInstance addInstance(ContentResolver contentResolver, AlarmInstance alarmInstance) {
        for (AlarmInstance instance : getInstanceByAlarmId(contentResolver, alarmInstance.mAlarmId)) {
            if (instance.getAlarmTime().equals(alarmInstance.getAlarmTime())) {
                alarmInstance.mId = instance.mId;
                updateInstance(contentResolver, alarmInstance);
                return instance;
            }
        }

        ContentValues contentValues = createContentValues(alarmInstance);
        Uri uri = contentResolver.insert(CONTENT_URI, contentValues);
        alarmInstance.mId = getId(uri);
        return alarmInstance;
    }

    /**
     * 更新instance
     *
     * @param contentResolver
     * @param alarmInstance
     * @return
     */
    public static boolean updateInstance(ContentResolver contentResolver, AlarmInstance alarmInstance) {
        if (alarmInstance.mId == INVALID_ID) return false;

        ContentValues contentValues = createContentValues(alarmInstance);
        long i = contentResolver.update(getUri(alarmInstance.mId), contentValues, null, null);
        return i == 1;
    }

    /**
     * 删除闹钟
     *
     * @param contentResolver
     * @param instance
     * @return
     */
    public static boolean deleteInstance(ContentResolver contentResolver, AlarmInstance instance) {
        if (instance.mId == INVALID_ID) return false;
        long deleteRows = contentResolver.delete(getUri(instance.mId), null, null);
        return deleteRows == 1;
    }

    public static Intent createIntent(String action, long instanceId) {
        return new Intent(action).setData(getUri(instanceId));
    }

    public static Intent createIntent(Context context, Class<?> cls, long intanceId) {
        return new Intent(context, cls).setData(getUri(intanceId));
    }
}
