package paul.gdaib.com.alarmclock.utils;

import android.icu.text.DateFormat;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

import java.util.Locale;

/**
 * Created by Paul on 2016/10/24.
 */

public class Utils {

    /**
     * 判断版本
     */
    public static boolean isKitKatOrLater() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    /***
     * 上下午文字标签大小
     *
     * @param amPmFontSize - size of am/pm label (label removed is size is 0).
     * @return format string for 12 hours mode time
     */
//    public static CharSequence get12ModeFormat(int amPmFontSize) {
//        String skeleton = "hma";
//        String pattern = DateFormat.getPatternInstance(skeleton, Locale.getDefault());
//        if (amPmFontSize <= 0) {
//            pattern.replaceAll("a", "").trim();
//        }
//        pattern = pattern.replaceAll(" ", "\u200A");
//        int amPmPos = pattern.indexOf('a');
//        if (amPmPos == -1) {
//            return pattern;
//        }
//        Spannable sp = new SpannableString(pattern);
//        sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), amPmPos, amPmPos + 1,
//                Spannable.SPAN_POINT_MARK);
//        sp.setSpan(new AbsoluteSizeSpan(amPmFontSize), amPmPos, amPmPos + 1,
//                Spannable.SPAN_POINT_MARK);
//        sp.setSpan(new TypefaceSpan("sans-serif-condensed"), amPmPos, amPmPos + 1,
//                Spannable.SPAN_POINT_MARK);
//        return sp;
//    }
}
