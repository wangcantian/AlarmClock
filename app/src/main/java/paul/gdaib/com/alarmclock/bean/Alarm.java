package paul.gdaib.com.alarmclock.bean;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

import paul.gdaib.com.alarmclock.R;
import paul.gdaib.com.alarmclock.utils.LogUtils;

/**
 * Created by Paul on 2016/10/15.
 */
public class Alarm implements Parcelable, ClockContract.AlarmsColumns {

    public static final String TABLE_NAME = "alarm_templates";
    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY, "
            + ENABLED + " INTEGER NOT NULL, "
            + HOUR + " INTEGER NOT NULL, "
            + MINUTES + " INTEGER NOT NULL, "
            + DAYS_OF_WEEK + " INTEGER NOT NULL, "
            + VIBRATE + " INTEGER NOT NULL, "
            + LABEL + " TEXT NOT NULL, "
            + RINGTONE + " TEXT, "
            + DELETE_AFTER_USE + " INTEGER NOT NULL DEFAULT 0, "
            + CLOSE_TYPE + " INTEGER NOT NULL, "
            + STRING_CODE + " TEXT);";
    public static final String DEFAULT_SORT_ORDER = HOUR + "," + MINUTES + " ASC, " + _ID + " DESC";

    public long mId;
    public boolean mEnabled;
    public int mHour;
    public int mMinutes;
    public DaysOfWeak mDaysOfWeak;
    public String mLabel;
    public boolean mVibrate;
    public Uri mAlert;
    public boolean mDeleteAfterUse;
    public int mCloseType;
    public String mStringCode;

    // 字段索引
    private static final int ID_INDEX = 0;
    private static final int ENABLED_INDEX = 1;
    private static final int HOUR_INDEX = 2;
    private static final int MINUTES_INDEX = 3;
    private static final int DAYS_OF_WEEK_INDEX = 4;
    private static final int VIBRATE_INDEX = 5;
    private static final int LABEL_INDEX = 6;
    private static final int RINGTONE_INDEX = 7;
    private static final int DELETE_AFTER_USE_INDEX = 8;
    private static final int CLOSE_TYPE_INDEX = 9;
    private static final int STRING_CODE_INDEX = 10;
    private static final int COLUMN_COUNT = STRING_CODE_INDEX + 1;

    public Alarm() {
        this(0, 0);
    }


    public Alarm(int hour, int minutes) {
        this.mId = INVALID_ID;
        this.mEnabled = false;
        this.mHour = hour;
        this.mMinutes = minutes;
        this.mDaysOfWeak = new DaysOfWeak(DaysOfWeak.NO_DAYS_SET);
        this.mLabel = "";
        this.mVibrate = true;
        this.mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        this.mDeleteAfterUse = false;
        this.mCloseType = TYPE_NONE;
        this.mStringCode = "";
    }

    public Alarm(Cursor cursor) {
        this.mId = cursor.getLong(ID_INDEX);
        this.mEnabled = cursor.getInt(ENABLED_INDEX) == 1;
        this.mHour = cursor.getInt(HOUR_INDEX);
        this.mMinutes = cursor.getInt(MINUTES_INDEX);
        this.mDaysOfWeak = new DaysOfWeak(cursor.getInt(DAYS_OF_WEEK_INDEX));
        this.mVibrate = cursor.getInt(VIBRATE_INDEX) == 1;
        this.mLabel = cursor.getString(LABEL_INDEX);
        this.mDeleteAfterUse = cursor.getInt(DELETE_AFTER_USE_INDEX) == 1;
        if (cursor.isNull(RINGTONE_INDEX)) {
            this.mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        } else {
            this.mAlert = Uri.parse(cursor.getString(RINGTONE_INDEX));
        }
        this.mCloseType = cursor.getInt(CLOSE_TYPE_INDEX);
        if (cursor.isNull(STRING_CODE_INDEX)) {
            this.mStringCode = "";
        } else {
            this.mStringCode = cursor.getString(STRING_CODE_INDEX);
        }
    }

    protected Alarm(Parcel in) {
        this.mId = in.readLong();
        this.mEnabled = in.readByte() != 0;
        this.mHour = in.readInt();
        this.mMinutes = in.readInt();
        this.mDaysOfWeak = new DaysOfWeak(in.readInt());
        this.mLabel = in.readString();
        this.mVibrate = in.readByte() != 0;
        this.mAlert = in.readParcelable(Uri.class.getClassLoader());
        this.mDeleteAfterUse = in.readByte() != 0;
        this.mCloseType = in.readInt();
        this.mStringCode = in.readString();
    }

    public static final Creator<Alarm> CREATOR = new Creator<Alarm>() {
        @Override
        public Alarm createFromParcel(Parcel in) {
            return new Alarm(in);
        }

        @Override
        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(mId);
        parcel.writeByte((byte) (mEnabled ? 1 : 0));
        parcel.writeInt(mHour);
        parcel.writeInt(mMinutes);
        parcel.writeInt(mDaysOfWeak.getBitSet());
        parcel.writeString(mLabel);
        parcel.writeByte((byte) (mVibrate ? 1 : 0));
        parcel.writeParcelable(mAlert, i);
        parcel.writeByte((byte) (mDeleteAfterUse ? 1 : 0));
        parcel.writeInt(mCloseType);
        parcel.writeString(mStringCode);
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "id=" + mId +
                ", enabled=" + mEnabled +
                ", hour=" + mHour +
                ", minutes=" + mMinutes +
                ", daysOfWeek=" + mDaysOfWeak +
                ", vibrate=" + mVibrate +
                ", label='" + mLabel + '\'' +
                ", deleteAfterUse=" + mDeleteAfterUse +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Alarm)) return false;
        final Alarm other = (Alarm) obj;
        return this.mId == other.mId;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(this.mId).hashCode();
    }

    /**
     * 返回标签
     *
     * @param context
     * @return
     */
    public String getDefaultLabelOrDefind(Context context) {
        if (TextUtils.isEmpty(this.mLabel)) {
            return context.getString(R.string.default_label);
        }
        return this.mLabel;
    }

    public static ContentValues createContentValues(Alarm alarm) {
        ContentValues values = new ContentValues(COLUMN_COUNT);
        if (alarm.mId != INVALID_ID) {
            values.put(_ID, alarm.mId);
        }
        values.put(ENABLED, alarm.mEnabled ? 1 : 0);
        values.put(HOUR, alarm.mHour);
        values.put(MINUTES, alarm.mMinutes);
        values.put(DAYS_OF_WEEK, alarm.mDaysOfWeak.getBitSet());
        values.put(VIBRATE, alarm.mVibrate ? 1 : 0);
        values.put(LABEL, alarm.mLabel);
        values.put(DELETE_AFTER_USE, alarm.mDeleteAfterUse ? 1 : 0);
        if (alarm.mAlert == null) {
            values.putNull(RINGTONE);
        } else {
            values.put(RINGTONE, alarm.mAlert.toString());
        }
        values.put(CLOSE_TYPE, alarm.mCloseType);
        values.put(STRING_CODE, alarm.mStringCode);
        return values;
    }

    public static Uri getUri(long alarmId) {
        return ContentUris.withAppendedId(CONTENT_URI, alarmId);
    }

    public static long getId(Uri contentUri) {
        return ContentUris.parseId(contentUri);
    }

    private static final String[] QUERY_COLUMNS = {
            _ID,
            ENABLED,
            HOUR,
            MINUTES,
            DAYS_OF_WEEK,
            VIBRATE,
            LABEL,
            RINGTONE,
            DELETE_AFTER_USE,
            CLOSE_TYPE,
            STRING_CODE
    };

    /**
     * 可以查询所有闹钟的CursorLoader
     *
     * @param context
     * @return
     */
    public static CursorLoader getAlarmsCursorLoader(Context context) {
        return new CursorLoader(context, CONTENT_URI, QUERY_COLUMNS, null, null, DEFAULT_SORT_ORDER);
    }

    /**
     * 通过id查找alarm
     *
     * @param contentResolver
     * @param alarmId
     * @return
     */
    public static Alarm getAlarmById(ContentResolver contentResolver, long alarmId) {
        Cursor cursor = contentResolver.query(getUri(alarmId), QUERY_COLUMNS, null, null, null);
        Alarm alarm = null;
        if (cursor == null) return alarm;
        try {
            if (cursor.moveToFirst()) {
                alarm = new Alarm(cursor);
            }
        } finally {
            cursor.close();
        }
        return alarm;
    }

    /**
     * 通过指定条件查找alarm集合
     *
     * @param contentResolver
     * @param selection
     * @param selectionArgs
     * @return
     */
    public static List<Alarm> getAlarmsBySelection(ContentResolver contentResolver, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = contentResolver.query(CONTENT_URI, QUERY_COLUMNS, selection, selectionArgs, sortOrder);
        List<Alarm> list = new LinkedList<>();
        if (cursor == null) return list;

        try {
            while (cursor.moveToNext()) {
                list.add(new Alarm(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    /**
     * 添加闹钟
     *
     * @param contentResolver
     * @param alarm
     * @return
     */
    public static Alarm addAlarm(ContentResolver contentResolver, Alarm alarm) {
        ContentValues contentValues = createContentValues(alarm);
        Uri uri = contentResolver.insert(CONTENT_URI, contentValues);
        alarm.mId = getId(uri);
        return alarm;
    }

    /**
     * 更新闹钟
     *
     * @param contentResolver
     * @param alarm
     * @return
     */
    public static boolean updateAlarm(ContentResolver contentResolver, Alarm alarm) {
        if (alarm.mId == Alarm.INVALID_ID) return false;

        ContentValues contentValues = createContentValues(alarm);
        long i = contentResolver.update(getUri(alarm.mId), contentValues, null, null);
        return i == 1;
    }

    /**
     * 删除闹钟
     *
     * @param contentResolver
     * @param alarmId
     * @return
     */
    public static boolean deteleAlarm(ContentResolver contentResolver, long alarmId) {
        if (alarmId == Alarm.INVALID_ID) return false;

        int i = contentResolver.delete(getUri(alarmId), null, null);
        return i == 1;
    }

    /**
     * 返回下个闹钟
     * @param calendar
     * @return
     */
    public AlarmInstance createAlarmInstanceAfter(Calendar calendar) {
        Calendar nextInstanceTime = Calendar.getInstance();
        nextInstanceTime.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        nextInstanceTime.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        nextInstanceTime.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
        nextInstanceTime.set(Calendar.HOUR_OF_DAY, this.mHour);
        nextInstanceTime.set(Calendar.MINUTE, this.mMinutes);
        nextInstanceTime.set(Calendar.SECOND, 0);
        nextInstanceTime.set(Calendar.MILLISECOND, 0);

        if (nextInstanceTime.getTimeInMillis() <= calendar.getTimeInMillis()) {
            nextInstanceTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        int addDays = mDaysOfWeak.calculateDaysToNextAlarms(nextInstanceTime);
        if (addDays > 0) {
            nextInstanceTime.add(Calendar.DAY_OF_WEEK, addDays);
        }

        AlarmInstance alarmInstance = new AlarmInstance(nextInstanceTime, this.mId);
        alarmInstance.mVibrate = this.mVibrate;
        alarmInstance.mLabel = this.mLabel;
        alarmInstance.mRingtone = this.mAlert;
        alarmInstance.mCloseType = this.mCloseType;
        alarmInstance.mStringCode = this.mStringCode;
        return alarmInstance;
    }
}
