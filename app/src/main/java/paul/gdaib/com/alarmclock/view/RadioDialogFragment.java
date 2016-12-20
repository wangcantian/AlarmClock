package paul.gdaib.com.alarmclock.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import paul.gdaib.com.alarmclock.R;
import paul.gdaib.com.alarmclock.bean.Alarm;

/**
 * Created by Paul on 2016/10/31.
 */

public class RadioDialogFragment extends DialogFragment {

    private OnRadioSelected onRadioSelected;

    public static RadioDialogFragment newInstance(Alarm alarm, OnRadioSelected onRadioSelected) {
        RadioDialogFragment radioDialogFragment = new RadioDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", alarm.mCloseType);
        radioDialogFragment.setArguments(bundle);
        radioDialogFragment.setOnRadioSelected(onRadioSelected);
        return radioDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_close_type, container, false);
        int type = getArguments().getInt("type", Alarm.TYPE_NONE);

        final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.rg_contrain);
        if (type == Alarm.TYPE_NONE) {
            ((RadioButton) radioGroup.getChildAt(0)).setChecked(true);
        } else if (type == Alarm.TYPE_MATH) {
            ((RadioButton) radioGroup.getChildAt(1)).setChecked(true);
        } else if (type == Alarm.TYPE_CODE) {
            ((RadioButton) radioGroup.getChildAt(2)).setChecked(true);
        }
        for (int i = 0; i < 3; i++) {
            radioGroup.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.rb_normal:
                            onRadioSelected.onRadioSelected(Alarm.TYPE_NONE);
                            break;
                        case R.id.rb_math:
                            onRadioSelected.onRadioSelected(Alarm.TYPE_MATH);
                            break;
                        case R.id.rb_code:
                            onRadioSelected.onRadioSelected(Alarm.TYPE_CODE);
                            break;
                    }
                    dismiss();
                }
            });
        }
        return view;
    }

    public void setOnRadioSelected(OnRadioSelected onRadioSelected) {
        this.onRadioSelected = onRadioSelected;
    }

    public interface OnRadioSelected {
        void onRadioSelected(int type);
    }
}
