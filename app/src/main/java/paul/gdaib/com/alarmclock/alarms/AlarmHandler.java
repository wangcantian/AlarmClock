package paul.gdaib.com.alarmclock.alarms;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by Paul on 2016/10/20.
 */
public class AlarmHandler {

	public static final String NAME = "alarmhandler";
	private static final HandlerThread mHandlerThread = new HandlerThread(NAME);
	private static final Handler mHandler;

	static {
		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper());
	}

	private AlarmHandler() {

	}

	public static void post(Runnable r) {
		mHandler.post(r);
	}

	public static Handler getAlarmHandler() {
		return mHandler;
	}
}
