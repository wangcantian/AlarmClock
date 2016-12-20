package paul.gdaib.com.alarmclock.alarms;

import android.content.Context;
import android.os.PowerManager;

import paul.gdaib.com.alarmclock.utils.LogUtils;

/**
 * Created by Paul on 2016/10/21.
 */

public class AlarmAlertWakeLock {

    private static PowerManager.WakeLock mWakeLock;

    public static PowerManager.WakeLock createPartialWakeLock(Context context) {
        PowerManager manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LogUtils.LOG_TAG);
    }

    public static void acquireCpuWakeLock(Context context) {
        if (mWakeLock == null) {
            mWakeLock = createPartialWakeLock(context);
            mWakeLock.acquire();
        }
    }

    public static void releaseCpuLock() {
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }
}
