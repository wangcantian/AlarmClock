package paul.gdaib.com.alarmclock.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import paul.gdaib.com.alarmclock.utils.LogUtils;

/**
 * Created by Paul on 2016/10/21.
 * 开关机或者时区发生该改变时重新设置闹钟状态
 */

public class AlarmInitReceiver extends BroadcastReceiver implements LogUtils.LogZX {

    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        LogUtils.e(this, "action = " + action);

        final PendingResult result = goAsync();
        final PowerManager.WakeLock wakeLock = AlarmAlertWakeLock.createPartialWakeLock(context);
        wakeLock.acquire();

        AlarmHandler.post(new Runnable() {
            @Override
            public void run() {
                AlarmStateManager.fixAlarmInstances(context);
                result.finish();
                wakeLock.release();
            }
        });
    }
}
