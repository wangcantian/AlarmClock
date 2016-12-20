package paul.gdaib.com.alarmclock.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Paul on 2016/10/15.
 */
public class ToastUtils {

	private static Toast mToast = null;

	private ToastUtils() {

	}

	public static void showOnly(Context context, String msg) {
		if (mToast != null) {
			mToast.cancel();
		}
		show(context, msg);
	}

	public static void show(Context context, String msg) {
		Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		mToast = toast;
		mToast.show();
	}

}
