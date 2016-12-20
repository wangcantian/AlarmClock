package paul.gdaib.com.alarmclock.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import net.robinx.lib.blur.widget.BlurDrawable;
import net.robinx.lib.blur.widget.BlurRelativeLayout;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import paul.gdaib.com.alarmclock.OperationAlarm;
import paul.gdaib.com.alarmclock.R;
import paul.gdaib.com.alarmclock.bean.Alarm;
import paul.gdaib.com.alarmclock.bean.DaysOfWeak;
import paul.gdaib.com.alarmclock.utils.LogUtils;
import paul.gdaib.com.alarmclock.FragmentPresenter;
import paul.gdaib.com.alarmclock.view.SquareToggleButton;
import paul.gdaib.com.alarmclock.view.TextTime;
import paul.gdaib.com.alarmclock.view.recycleView.SwipeMenu;
import paul.gdaib.com.alarmclock.view.recycleView.SwipeMenuItem;
import paul.gdaib.com.alarmclock.view.recycleView.SwipeMenuLayout;
import paul.gdaib.com.alarmclock.view.recycleView.SwipeMenuView;

/**
 * Created by Paul on 2016/10/16.
 */
public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> implements SwipeMenuView.OnMenuItemClickListener {
    private static final String TAG = AlarmAdapter.class.getSimpleName();

    private final int[] DAY_ORDER = new int[]{
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
    };

    private Context mContext;
    private OperationAlarm mOperationAlarm;
    private FragmentPresenter mFragmentPresenter;
    private RecyclerView mView;
    private LayoutInflater mInflater;
    private OnEditClickListener mListener;
    private List<Alarm> mDataLists;
    private long mScrollAlarmId = -1;

    public AlarmAdapter(@Nullable Context context, OperationAlarm operationAlarm, FragmentPresenter fragmentPresenter, RecyclerView view, List<Alarm> list, @NonNull OnEditClickListener listener) {
        this.mContext = context;
        this.mOperationAlarm = operationAlarm;
        this.mFragmentPresenter = fragmentPresenter;
        this.mView = view;
        this.mInflater = LayoutInflater.from(context);
        this.mDataLists = list;
        if (listener == null) {
            throw new IllegalArgumentException();
        }
        this.mListener = listener;
    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SwipeMenuItem swipeMenuItem = new SwipeMenuItem(mContext);
        swipeMenuItem.setBackground(new ColorDrawable(mContext.getResources().getColor(R.color.red)));
        swipeMenuItem.setIcon(R.mipmap.ice_detele);
        swipeMenuItem.setWidth(mContext.getResources().getDimensionPixelOffset(R.dimen.swipe_delete_width));
        SwipeMenu swipeMenu = new SwipeMenu(mContext);
        swipeMenu.addMenuItem(swipeMenuItem);
        SwipeMenuView swipeMenuView = new SwipeMenuView(swipeMenu);
        View contentView = mInflater.inflate(R.layout.listitem_alarm, parent, false);
        final SwipeMenuLayout view = new SwipeMenuLayout(contentView, swipeMenuView, new BounceInterpolator(), new LinearInterpolator());
        view.setClickable(true);
        swipeMenuView.setOnMenuItemClickListener(this);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                BlurDrawable blurDrawable = new BlurDrawable((AppCompatActivity) mContext);
                blurDrawable.setBlurRadius(100);
                blurDrawable.setOverlayColor(Color.parseColor("#64ffffff"));
                view.setBackground(blurDrawable);
            }
        });
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AlarmViewHolder holder, int position) {
        holder.alarm = mDataLists.get(position);
        holder.onoff.setClickable(true);
        holder.onoff.setChecked(holder.alarm.mEnabled);
        holder.onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked != holder.alarm.mEnabled) {
                    LogUtils.e(TAG, "闹钟状态=" + isChecked);
                    holder.alarm.mEnabled = isChecked;
                    mOperationAlarm.asyncUpdateAlarm(holder.alarm, holder.alarm.mEnabled);
                }
            }
        });

        holder.clock.setTime(holder.alarm.mHour, holder.alarm.mMinutes);
        holder.clock.setClickable(true);
        holder.clock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentPresenter.showTimeDialog(holder.alarm);
            }
        });

        updateDaysOfWeekButton(holder, holder.alarm.mDaysOfWeak);
        for (int i = 0; i < 7; i++) {
            final int index = i;
            holder.dayButtons[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    holder.alarm.mDaysOfWeak.setDaysOfWeek(isChecked, DAY_ORDER[index]);
                    mOperationAlarm.asyncUpdateAlarm(holder.alarm, false);
                }
            });
        }

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onEditClick(holder.getAdapterPosition(), holder.alarm);
                }
            }
        });

        holder.label.setText(holder.alarm.getDefaultLabelOrDefind(mContext));
        holder.label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentPresenter.showLabelDialog(holder.alarm);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataLists.size();
    }

    @Override
    public void onMenuItemClick(int position, SwipeMenu menu, int index) {
        mOperationAlarm.asyncDeleteAlarm(mDataLists.get(position));
    }

    private void updateDaysOfWeekButton(AlarmViewHolder holder, DaysOfWeak daysOfWeak) {
        HashSet<Integer> setDays = daysOfWeak.getSetDays();
        for (int i = 0; i < 7; i++) {
            if (setDays.contains(DAY_ORDER[i])) {
                holder.dayButtons[i].setChecked(true);
            } else {
                holder.dayButtons[i].setChecked(false);
            }
        }
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        Alarm alarm;

        TextTime clock;
        TextView label;
        ImageView edit;
        LinearLayout dayOfWeeks;
        SwitchCompat onoff;
        SquareToggleButton[] dayButtons = new SquareToggleButton[7];

        public AlarmViewHolder(View itemView) {
            super(itemView);
            clock = (TextTime) itemView.findViewById(R.id.tt_clock_time);
            label = (TextView) itemView.findViewById(R.id.tv_label);
            edit = (ImageView) itemView.findViewById(R.id.iv_edit);
            onoff = (SwitchCompat) itemView.findViewById(R.id.s_on_off);
            dayOfWeeks = (LinearLayout) itemView.findViewById(R.id.ll_dayOfWeeks);
            for (int i = 0; i < 7; i++) {
                dayButtons[i] = (SquareToggleButton) dayOfWeeks.getChildAt(i + 1);
            }
        }
    }

    //滑动条目，使其完全显示在屏幕上
    private final Runnable mScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (mScrollAlarmId != -1) {
                View v = getViewById(mScrollAlarmId);
                if (v != null) {
                    Rect rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getRight());
                    mView.requestChildRectangleOnScreen(v, rect, false);
                }
                mScrollAlarmId = -1;
            }
        }

    };

    private View getViewById(long id) {
        for (int i = 0; i < getItemCount(); i++) {
            View v = mView.getChildAt(i);
            if (v != null) {
                AlarmViewHolder h = (AlarmViewHolder) (v.getTag());
                if (h != null && h.alarm.mId == id) {
                    return v;
                }
            }
        }
        return null;
    }

    public interface OnEditClickListener {
        void onEditClick(int position, Alarm alarm);
    }
}
