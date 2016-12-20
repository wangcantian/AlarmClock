package paul.gdaib.com.alarmclock;

import paul.gdaib.com.alarmclock.bean.Alarm;

/**
 * Created by Paul on 2016/10/27.
 */

public interface FragmentPresenter {
    void showTimeDialog(Alarm alarm);
    void showLabelDialog(Alarm alarm);
}
