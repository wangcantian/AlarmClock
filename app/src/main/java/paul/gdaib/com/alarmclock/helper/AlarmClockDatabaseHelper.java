package paul.gdaib.com.alarmclock.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import paul.gdaib.com.alarmclock.bean.Alarm;
import paul.gdaib.com.alarmclock.bean.AlarmInstance;
import paul.gdaib.com.alarmclock.bean.ClockContract;
import paul.gdaib.com.alarmclock.utils.LogUtils;

/**
 * Created by Paul on 2016/10/21.
 */

public class AlarmClockDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = AlarmClockDatabaseHelper.class.getSimpleName();
    private static final String DB_NAME = "alarms.db";
    private static final int DB_VERSION = 1;

    public AlarmClockDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtils.e(TAG, "datebase onCreate");
        createAlarmsTable(db);
        createAlarmInstancesTable(db);

        // 程序第一运行默认加入一个闹钟
        String alarm1 = "(0, 8, 30, 31, 0, '', NULL, 0, 1, '');";
        String alarm2 = "(0, 9, 00, 96, 0, '', NULL, 0, 0, '');";
        String insertMe = "INSERT INTO " + Alarm.TABLE_NAME + "( " +
                Alarm.ENABLED + ", " +
                Alarm.HOUR + ", " +
                Alarm.MINUTES + ", " +
                Alarm.DAYS_OF_WEEK + ", " +
                Alarm.VIBRATE + ", " +
                Alarm.LABEL + ", " +
                Alarm.RINGTONE + ", " +
                Alarm.DELETE_AFTER_USE + ", " +
                Alarm.CLOSE_TYPE + ", " +
                Alarm.STRING_CODE + ") VALUES";
        db.execSQL(insertMe + alarm1);
        db.execSQL(insertMe + alarm2);
    }

    private void createAlarmsTable(SQLiteDatabase db) {
        db.execSQL(Alarm.CREATE_TABLE);
    }

    private void createAlarmInstancesTable(SQLiteDatabase db) {
        db.execSQL(AlarmInstance.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
