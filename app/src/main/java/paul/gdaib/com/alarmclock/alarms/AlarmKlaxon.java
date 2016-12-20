package paul.gdaib.com.alarmclock.alarms;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

import java.io.IOException;

import paul.gdaib.com.alarmclock.bean.Alarm;
import paul.gdaib.com.alarmclock.bean.AlarmInstance;
import paul.gdaib.com.alarmclock.utils.AlarmUtils;

import static android.content.ContentValues.TAG;

/**
 * Created by Paul on 2016/10/24.
 */

public class AlarmKlaxon {

    private static final long[] VIBRATE_PATTERN = new long[]{500, 500};
    private static boolean mStarted = false;
    private static MediaPlayer mMediaPlayer = null;

    /**
     * 开始铃声和震动
     *
     * @param context
     * @param instance
     * @param isTelephoneCall
     */
    public static void start(final Context context, AlarmInstance instance, boolean isTelephoneCall) {
        stop(context);

        if (isTelephoneCall) return;

        if (!Alarm.NO_RINGTONE_URI.equals(instance.mRingtone)) {
            Uri alarmNoise = instance.mRingtone;
            if (alarmNoise == null || !AlarmUtils.isRingtoneExisted(context, alarmNoise.toString())) {
                alarmNoise = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }

            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    AlarmKlaxon.stop(context);
                    return false;
                }
            });

            try {
                mMediaPlayer.setDataSource(context, alarmNoise);
                startAlarm(context, mMediaPlayer);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (instance.mVibrate) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_PATTERN, 0);
        }

        mStarted = true;
    }

    public static void stop(Context context) {
        if (mStarted) {
            mStarted = false;
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                am.abandonAudioFocus(null);
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.cancel();
        }
    }

    private static void startAlarm(Context context, MediaPlayer player) throws IllegalStateException, IOException {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setLooping(true);
            player.prepare();
            am.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            player.start();
        }
    }
}
