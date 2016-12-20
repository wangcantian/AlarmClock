package paul.gdaib.com.alarmclock.utils;

import android.util.Log;

/**
 * Created by Paul on 2016/10/15.
 */
public class LogUtils {

	public static final boolean DEBUG_ENABLE = false;
	public static final String LOG_TAG = "";

	public static void d(String tag, String msg) {
		if (DEBUG_ENABLE) {
			Log.d(LOG_TAG + "-----" + tag, "------------" + msg);
		}
	}

	public static void i(String tag, String msg) {
		if (DEBUG_ENABLE) {
			Log.i(LOG_TAG + "-----" + tag, "------------" + msg);
		}
	}

	public static void w(String tag, String msg) {
		if (DEBUG_ENABLE) {
			Log.w(LOG_TAG + "-----" + tag, "------------" + msg);
		}
	}

	public static void e(String tag, String msg) {
		if (DEBUG_ENABLE) {
			Log.e(LOG_TAG + "-----" + tag, "------------" + msg);
		}
	}

	public static void d(LogZX tag, String msg) {
		d(tag.getClass().getSimpleName(), msg);
	}

	public static void i(LogZX tag, String msg) {
		i(tag.getClass().getSimpleName(), msg);
	}

	public static void w(LogZX tag, String msg) {
		w(tag.getClass().getSimpleName(), msg);
	}

	public static void e(LogZX tag, String msg) {
		e(tag.getClass().getSimpleName(), msg);
	}

	public interface LogZX {
	}
}
