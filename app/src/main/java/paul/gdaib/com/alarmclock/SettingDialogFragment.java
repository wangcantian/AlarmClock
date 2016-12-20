package paul.gdaib.com.alarmclock;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Paul on 2016/10/31.
 */

public class SettingDialogFragment extends DialogFragment {

    public static final String KEY_AUTO_SILENCE = "auto_silence";
    public static final String KEY_ALARM_SNOOZE = "snooze_duration";
    public static final String KEY_VOLUME_BUTTONS = "volume_button_settings";

    public static final int DEFAULT_VOLUME_BEHAVIOR = 0;
    public static final int DEFAULT_SNOOZE_MINUTES = 10;

    public static SettingDialogFragment newInstance() {
        SettingDialogFragment settingDialogFragment = new SettingDialogFragment();
        return settingDialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
