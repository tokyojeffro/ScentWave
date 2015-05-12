package com.scentair.scentwave;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
            viewHolder.calibrationIncrementButton = (Button) rowView.findViewById(R.id.increase_calibration);
            viewHolder.calibrationIncrementButton.setTextColor(Color.BLACK);
            viewHolder.calibrationDecrementButton = (Button) rowView.findViewById(R.id.decrease_calibration);
            viewHolder.calibrationDecrementButton.setTextColor(Color.BLACK);
            viewHolder.toggleActiveButton = (Button) rowView.findViewById(R.id.toggle_active_state);
            viewHolder.toggleActiveButton.setTextColor(Color.YELLOW);
            viewHolder.bayNumberField = (TextView) rowView.findViewById(R.id.calibration_bay_number);
            viewHolder.bayNumberField.setTextColor(Color.BLACK);
            viewHolder.rawValueField = (TextView) rowView.findViewById(R.id.raw_value);
            viewHolder.rawValueField.setTextColor(Color.BLACK);
            viewHolder.calibratedValueField = (TextView) rowView.findViewById(R.id.calibrated_value);
            viewHolder.calibratedValueField.setTextColor(Color.BLACK);
            viewHolder.bayStateField = (TextView) rowView.findViewById((R.id.bay_active_state));
            viewHolder.bayStateField.setTextColor(Color.BLACK);
            rowView.setTag(viewHolder);
        } // end of initializing a new view
        ViewHolder holder = (ViewHolder) rowView.getTag();
        if (bays[position].active) {
            // Bay is active, set background color and text accordingly
            holder.bayStateField.setText("Bay is Active");
            holder.bayStateField.setBackgroundColor(Color.GREEN);
        } else {
            // Bay is inactive due
            holder.bayStateField.setText("Bay is Inactive");
            holder.bayStateField.setBackgroundColor(Color.RED);
        }
        // Load the data from the array into the view
        holder.bayNumberField.setText(String.valueOf(bays[position].bayNumber));
        // Apply calibration.
        holder.rawValueField.setText(bays[position].rawValue.toString());
        Integer calibratedValue = bays[position].rawValue + bays[position].calibrationOffset;
        String plusMinus = " + ";
        if (bays[position].calibrationOffset < 0) plusMinus = " - ";
        Integer offsetValue = Math.abs(bays[position].calibrationOffset);
        String calibrationText = bays[position].rawValue.toString() + plusMinus + offsetValue.toString() + " = " + calibratedValue.toString();
        holder.calibratedValueField.setText(calibrationText);
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