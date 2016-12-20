package paul.gdaib.com.alarmclock;

import paul.gdaib.com.alarmclock.bean.Alarm;

/**
 * Created by Paul on 2016/10/27.
 */

public interface OperationAlarm {
    void asyncDeleteAlarm(final Alarm alarm);
    void asyncAddAlarm(final Alarm alarm);
    void asyncUpdateAlarm(final Alarm alarm, final boolean popToast);
}
