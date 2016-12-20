package paul.gdaib.com.alarmclock.view;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Calendar;

import paul.gdaib.com.alarmclock.R;

/**
 * Created by Paul on 2016/10/25.
 */

public class TextTime extends TextView {
    public static final CharSequence DEFAULT_FORMAT_12_HOUR = "h:mm a";
    public static final CharSequence DEFAULT_FORMAT_24_HOUR = "H:mm";

    private CharSequence mFormat12;
    private CharSequence mFormat24;
    private CharSequence mFormat;
    private String mContentDescriptionFormat;

    private boolean mAttached;
    private int mHour;
    private int mMinute;

    private final ContentObserver mFormatChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            chooseFormat();
            updateTime();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            chooseFormat();
            updateTime();
        }
    };

    public TextTime(Context context) {
        this(context, null);
    }

    public TextTime(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextTime(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray styleAttributes = context.obtainStyledAttributes(attrs, R.styleable.TextTime, defStyleAttr, 0);
        mFormat12 = styleAttributes.getText(R.styleable.TextTime_format12Hour);
        mFormat24 = styleAttributes.getText(R.styleable.TextTime_format24Hour);
        styleAttributes.recycle();
        chooseFormat();
    }

    private void chooseFormat() {
        final boolean format24Requested = DateFormat.is24HourFormat(getContext());
        if (format24Requested) {
            mFormat = mFormat24 == null ? DEFAULT_FORMAT_24_HOUR : mFormat24;
        } else {
            mFormat = mFormat12 == null ? DEFAULT_FORMAT_12_HOUR : mFormat12;
        }
        mContentDescriptionFormat = mFormat.toString();
    }

    private void updateTime() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, mHour);
        calendar.set(Calendar.MINUTE, mMinute);
        setText(DateFormat.format(mFormat, calendar));
        if (mContentDescriptionFormat != null) {
            setContentDescription(DateFormat.format(mContentDescriptionFormat, calendar));
        } else {
            setContentDescription(DateFormat.format(mFormat, calendar));
        }
    }

    public CharSequence getFormat12Hour() {
        return mFormat12;
    }

    public void setFormat12Hour(CharSequence format) {
        mFormat12 = format;
        chooseFormat();
        updateTime();
    }

    public CharSequence getFormat24Hour() {
        return mFormat24;
    }

    public void setFormat24Hour(CharSequence format) {
        mFormat24 = format;
        chooseFormat();
        updateTime();
    }

    public void setTime(int hour, int minute) {
        mHour = hour;
        mMinute = minute;
        updateTime();
    }

    private void registerObserver() {
        final ContentResolver contentResolver = getContext().getContentResolver();
        contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, mFormatChangeObserver);
    }

    public void unregisterObserver() {
        final ContentResolver resolver = getContext().getContentResolver();
        resolver.unregisterContentObserver(mFormatChangeObserver);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mAttached) {
            mAttached = true;
            registerObserver();
            updateTime();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            unregisterObserver();
            mAttached = false;
        }
    }
}
