package com.scentair.scentwave;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class CalibrationBayArrayAdapter extends ArrayAdapter<Bay> {

    private final Context context;
    private final Bay[] bays;

    customCalibrationButtonListener customCalibrationListener;

    public interface customCalibrationButtonListener {

        void onToggleButtonClickListener(int position, int listViewPosition);
        void onCalibrationIncrementButtonClickListener(int position, int listViewPosition);
        void onCalibrationDecrementButtonClickListener(int position, int listViewPosition);
    }

    public void setCustomCalibrationButtonListener(customCalibrationButtonListener listener) {
        this.customCalibrationListener = listener;
    }

    static class ViewHolder {
        TextView bayNumberField;
        TextView rawValueField;
        TextView calibratedValueField;
        TextView bayStateField;
        Button calibrationIncrementButton;
        Button calibrationDecrementButton;
        Button toggleActiveButton;
    }

    public CalibrationBayArrayAdapter (Context context, Bay[] bays){
        super(context,R.layout.calibration_row,bays);
        this.context = context;
        this.bays = bays;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView==null) {
            ViewHolder viewHolder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.calibration_row, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.calibrationIncrementButton = (Button) rowView.findViewById(R.id.increase_calibration);
            viewHolder.calibrationDecrementButton = (Button) rowView.findViewById(R.id.decrease_calibration);
            viewHolder.toggleActiveButton = (Button) rowView.findViewById(R.id.toggle_active_state);

            viewHolder.bayNumberField = (TextView) rowView.findViewById(R.id.calibration_bay_number);
            viewHolder.rawValueField = (TextView) rowView.findViewById(R.id.raw_value);
            viewHolder.calibratedValueField = (TextView) rowView.findViewById(R.id.calibrated_value);
            viewHolder.bayStateField = (TextView) rowView.findViewById((R.id.bay_active_state));

            rowView.setTag(viewHolder);
        } // end of initializing a new view

        ViewHolder holder = (ViewHolder) rowView.getTag();

        if (bays[position].active) {
            // Bay is active, set background color and text accordingly
            holder.toggleActiveButton.setText("Set Inactive");
            holder.toggleActiveButton.setBackgroundColor(Color.parseColor("#FF4444"));

            holder.bayStateField.setText("Bay is Active");
            holder.bayStateField.setBackgroundColor(Color.parseColor("#44CC00"));

        } else {
            // Bay is inactive due
            holder.toggleActiveButton.setText("Set Active");
            holder.toggleActiveButton.setBackgroundColor(Color.parseColor("#44CC00"));

            holder.bayStateField.setText("Bay is Inactive");
            holder.bayStateField.setBackgroundColor(Color.parseColor("#FF4444"));
        }

        // Load the data from the array into the view

        holder.bayNumberField.setText(String.valueOf(bays[position].bayNumber));

        // TODO add in the actual sensor readings and apply calibration first
        holder.rawValueField.setText("10");
        //holder.rawValueField.setText(String.valueOf(actual sensor value));
        holder.calibratedValueField.setText(String.valueOf(bays[position].calibrationOffset));
        //holder.rawValueField.setText(String.valueOf(actual sensor value + bays[position].calibrationOffset));

        holder.toggleActiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentRow = (View) v.getParent();
                View parentRow2 = (View) parentRow.getParent();

                ListView listView = (ListView) parentRow2.getParent();
                final int listViewPosition = listView.getPositionForView(parentRow);
                if (customCalibrationListener != null) {
                    customCalibrationListener.onToggleButtonClickListener(position, listViewPosition);
                }
            }
        });

        holder.calibrationIncrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentRow = (View) v.getParent();
                View parentRow2 = (View) parentRow.getParent();

                ListView listView = (ListView) parentRow2.getParent();
                final int listViewPosition = listView.getPositionForView(parentRow);
                if (customCalibrationListener != null) {
                    customCalibrationListener.onCalibrationIncrementButtonClickListener(position, listViewPosition);
                }
            }
        });

        holder.calibrationDecrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentRow = (View) v.getParent();
                View parentRow2 = (View) parentRow.getParent();

                ListView listView = (ListView) parentRow2.getParent();
                final int listViewPosition = listView.getPositionForView(parentRow);
                if (customCalibrationListener != null) {
                    customCalibrationListener.onCalibrationDecrementButtonClickListener(position, listViewPosition);
                }
            }
        });

        return rowView;
    }
}