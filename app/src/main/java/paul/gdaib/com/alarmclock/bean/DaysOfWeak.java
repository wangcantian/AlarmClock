package paul.gdaib.com.alarmclock.bean;

import android.content.Context;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashSet;

import paul.gdaib.com.alarmclock.R;

/**
 * Created by Paul on 2016/10/15.
 */
public class DaysOfWeak {

	public static final int DAYS_OF_WEEK = 7;
	public static final int ALL_DAYS_SET = 127;
	public static final int NO_DAYS_SET = 0;

	private int mBitSet;

	public void setBitSet(int bitSet) {
		this.mBitSet = bitSet;
	}

	public int getBitSet() {
		return mBitSet;
	}

	public DaysOfWeak(int bitSet) {
		this.mBitSet = bitSet;
	}

	/**
	 * 设置具体星期生效或者失败
	 */
	public void setDaysOfWeek(boolean value, int... daysOfWeek) {
		for (int day : daysOfWeek) {
			setDaysOfWeek(value, day);
		}
	}

	/**
	 * 设置具体星期生效或者失败
	 */
	public void setDaysOfWeek(boolean value, int daysOfWeek) {
		setBit(converyDayToBitIndex(daysOfWeek), value);
	}

	/**
	 * 获得设置的星期
	 */
	public HashSet<Integer> getSetDays() {
		final HashSet<Integer> hashSet = new HashSet<Integer>();
		for (int bitIndex = 0; bitIndex < DAYS_OF_WEEK; bitIndex++) {
			if (isBitEnabled(bitIndex)) {
				hashSet.add(converyBitIndexToDay(bitIndex));
			}
		}
		return hashSet;
	}

	/**
	 * 放回当前星期是否被设置
	 */
	public boolean isSetEnbled(int daysOfWeek) {
		return isBitEnabled(converyDayToBitIndex(daysOfWeek));
	}

	/**
	 * 清楚所有设置的星期
	 */
	public void clearAllDays() {
		this.mBitSet = NO_DAYS_SET;
	}

	public boolean isRepeating() {
		return mBitSet != NO_DAYS_SET;
	}

	public int calculateDaysToNextAlarms(Calendar current) {
		if (!isRepeating()) return -1;

		int dayCount = 0;
		int currBitIndex = converyDayToBitIndex(current.get(Calendar.DAY_OF_WEEK));
		for (; dayCount < DAYS_OF_WEEK; dayCount++) {
			int nextAlarmBit = (currBitIndex + dayCount) % DAYS_OF_WEEK;
			if (isBitEnabled(nextAlarmBit)) {
				break;
			}
		}
		return dayCount;
	}

	private static int converyDayToBitIndex(int day) {
		return (day + 5) % DAYS_OF_WEEK;
	}

	private static int converyBitIndexToDay(int bitIndex) {
		return (bitIndex + 1) % DAYS_OF_WEEK + 1;
	}

	private void setBit(int bitIndex, boolean set) {
		if (set) {
			mBitSet |= (1 << bitIndex);
		} else {
			mBitSet &= ~(1 << bitIndex);
		}
	}

	private boolean isBitEnabled(int bitIndex) {
		return (mBitSet & (1 << bitIndex)) > 0;
	}

	@Override
	public String toString() {
		return "DaySOfWeek {" + "mBitSet=" + mBitSet + "}";
	}

	public String toString(Context context, boolean showNever) {
		return toString(context, showNever, false);
	}

	public String toAccessibilityString(Context context) {
		return toString(context, false, true);
	}

	private String toString(Context context, boolean showNever, boolean forAccessibility) {
		if (this.mBitSet == ALL_DAYS_SET) {
			return context.getString(R.string.every_day);
		}

		if (this.mBitSet == NO_DAYS_SET) {
			return showNever ? context.getString(R.string.never) : "";
		}

		int dayCount = 0;
		int bitSet = this.mBitSet;
		while (bitSet > 0) {
			if ((bitSet & 1) == 1) dayCount++;
			bitSet >>= 1;
		}
		DateFormatSymbols dfs = new DateFormatSymbols();
		String[] dayLists = (forAccessibility || dayCount <= 1) ?
				dfs.getWeekdays() : dfs.getShortWeekdays();

		StringBuilder builder = new StringBuilder();
		for (int bitIndex = 0; bitIndex < DAYS_OF_WEEK; bitIndex++) {
			if (isBitEnabled(bitIndex)) {
				builder.append(dayLists[converyBitIndexToDay(bitIndex)]);
				dayCount--;
				if (dayCount > 0) builder.append(context.getString(R.string.day_concat));
			}
		}
		return builder.toString();
	}
}
