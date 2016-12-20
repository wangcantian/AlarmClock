package paul.gdaib.com.alarmclock;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Paul on 2016/10/21.
 */

public class SettingsActivity extends AppCompatActivity {

    public static final String KEY_AUTO_SILENCE = "auto_silence";
    public static final String KEY_ALARM_SNOOZE = "snooze_duration";
    public static final String KEY_VOLUME_BUTTONS = "volume_button_settings";

    public static final int DEFAULT_VOLUME_BEHAVIOR = 0;
    public static final int DEFAULT_SNOOZE_MINUTES = 10;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }
}
