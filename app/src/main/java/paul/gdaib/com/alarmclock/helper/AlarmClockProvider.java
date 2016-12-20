package paul.gdaib.com.alarmclock.helper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.util.CircularArray;
import android.text.TextUtils;

import paul.gdaib.com.alarmclock.bean.Alarm;
import paul.gdaib.com.alarmclock.bean.AlarmInstance;
import paul.gdaib.com.alarmclock.bean.ClockContract;

/**
 * Created by Paul on 2016/10/22.
 */

public class AlarmClockProvider extends ContentProvider {

    private AlarmClockDatabaseHelper mHelper;

    private static final int ALARMS = 1;
    private static final int ALARMS_ID = 2;
    private static final int INSTANCES = 3;
    private static final int INSTANCES_ID = 4;

    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        mUriMatcher.addURI(ClockContract.AUTHORITY, "alarms", ALARMS);
        mUriMatcher.addURI(ClockContract.AUTHORITY, "alarms/#", ALARMS_ID);
        mUriMatcher.addURI(ClockContract.AUTHORITY, "instances", INSTANCES);
        mUriMatcher.addURI(ClockContract.AUTHORITY, "instances/#", INSTANCES_ID);
    }

    @Override
    public boolean onCreate() {
        mHelper = new AlarmClockDatabaseHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        switch (mUriMatcher.match(uri)) {
            case ALARMS:
                sqLiteQueryBuilder.setTables(Alarm.TABLE_NAME);
                break;
            case ALARMS_ID:
                sqLiteQueryBuilder.setTables(Alarm.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(Alarm._ID + "=");
                sqLiteQueryBuilder.appendWhere(uri.getLastPathSegment());
                break;
            case INSTANCES:
                sqLiteQueryBuilder.setTables(AlarmInstance.TABLE_NAME);
                break;
            case INSTANCES_ID:
                sqLiteQueryBuilder.setTables(AlarmInstance.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(AlarmInstance._ID + "=");
                sqLiteQueryBuilder.appendWhere(uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException();
        }

        SQLiteDatabase sqLiteDatabase = mHelper.getReadableDatabase();
        Cursor cursor = sqLiteQueryBuilder.query(sqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case ALARMS:
                return "vnd.android.cursor.dir/alarms";
            case ALARMS_ID:
                return "vnd.android.cursor.item/alarms";
            case INSTANCES:
                return "vnd.android.cursor.dir/instances";
            case INSTANCES_ID:
                return "vnd.android.cursor.item/instances";
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowId;
        SQLiteDatabase wdb = mHelper.getWritableDatabase();
        switch (mUriMatcher.match(uri)) {
            case ALARMS:
                rowId = wdb.insert(Alarm.TABLE_NAME, Alarm.RINGTONE, values);
                break;
            case INSTANCES:
                rowId = wdb.insert(AlarmInstance.TABLE_NAME, AlarmInstance.RINGTONE, values);
                break;
            default:
                throw new IllegalArgumentException("Cannot insert from URL : " + uri);
        }
        Uri resultUri = ContentUris.withAppendedId(uri, rowId);
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;
        SQLiteDatabase database = mHelper.getWritableDatabase();
        switch (mUriMatcher.match(uri)) {
            case ALARMS_ID:
                String primaryKey = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = Alarm._ID + "=" + primaryKey;
                } else {
                    selection = Alarm._ID + "=" + primaryKey + " AND (" + selection + ")";//加括号避免可能破坏原来运算顺序
                }
                count = database.delete(Alarm.TABLE_NAME, selection, selectionArgs);
                break;
            case INSTANCES_ID:
                primaryKey = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = AlarmInstance._ID + "=" + primaryKey;
                } else {
                    selection = AlarmInstance._ID + "=" + primaryKey + " AND (" + selection + ")";//加括号避免可能破坏原来运算顺序
                }
                count = database.delete(AlarmInstance.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete from URL :" + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;
        SQLiteDatabase database = mHelper.getWritableDatabase();
        switch (mUriMatcher.match(uri)) {
            case ALARMS_ID:
                String id = uri.getLastPathSegment();
                count = database.update(Alarm.TABLE_NAME, values, Alarm._ID + "=" + id, null);
                break;
            case INSTANCES_ID:
                id = uri.getLastPathSegment();
                count = database.update(AlarmInstance.TABLE_NAME, values, AlarmInstance._ID + "=" + id, null);
                break;
            default:
                throw new IllegalArgumentException("Cannot update from URL :" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
