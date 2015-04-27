package com.scentair.scentwave;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class BayItemArrayAdapter extends ArrayAdapter<BayItem> {

    private final Context context;
    
    private TestRun testRun=new TestRun();
    
    customButtonListener customListener;

    public interface customButtonListener {

        void onPassButtonClickListener(int position, int listViewPosition);
        void onFailButtonClickListener(int position, int listViewPosition);
        void onScentAirBarCodeClickListener(int position, String candidateText);
        void onMitecBarCodeClickListener(int position, String candidateText);
        void onScentAirBarCodeFocusChangeListener(int position, boolean touchFocusSelect);
        void onMitecBarCodeFocusChangeListener(int position, boolean touchFocusSelect);
    }

    public void setCustomButtonListener(customButtonListener listener) {
        this.customListener = listener;
    }

    static class ViewHolder {
        ViewGroup bayInactive;
        TextView bayNumberField;
        EditText mitecBarcodeField;
        EditText scentairBarcodeField;
        TextView unitStateField;
        TextView sensorReadingField;
        Button passButton;
        Button failButton;
        View activeRowLayout;
        ViewGroup bayItem;
        TextView bayInactiveText;
    }

    public BayItemArrayAdapter(Context context, TestRun _testRun) {
        super(context, R.layout.bayitem, _testRun.bayItems);
        this.context = context;
        this.testRun = new TestRun();
        this.testRun = _testRun.getTestRun();
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            ViewHolder viewHolder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.bayitem, parent, false);

            viewHolder.passButton = (Button) rowView.findViewById(R.id.testpassbutton);
            viewHolder.failButton = (Button) rowView.findViewById(R.id.testfailbutton);

            viewHolder.bayNumberField = (TextView) rowView.findViewById(R.id.baynumber);
            viewHolder.mitecBarcodeField = (EditText) rowView.findViewById(R.id.mitecbarcode);
            viewHolder.scentairBarcodeField = (EditText) rowView.findViewById(R.id.scentairbarcode);

            viewHolder.unitStateField = (TextView) rowView.findViewById(R.id.unitstate);
            viewHolder.sensorReadingField = (TextView) rowView.findViewById(R.id.sensorreading);

            viewHolder.bayInactive = (ViewGroup) rowView.findViewById(R.id.bay_item_inactive_bay);

            viewHolder.activeRowLayout = rowView.findViewById(R.id.bay_active_row_layout);

            viewHolder.bayItem = (ViewGroup) rowView.findViewById(R.id.bay_item);

            viewHolder.bayInactiveText = (TextView) rowView.findViewById(R.id.bay_item_inactive_bay_1st_line);

            rowView.setTag(viewHolder);
        } // end of initializing a new view

        ViewHolder holder = (ViewHolder) rowView.getTag();

        // Check whether the bay is calibrated active or inactive
        // Check whether the bay has failed a previous step
        // and check whether the phidget is connected for this bay by checking the current value
        if ((!testRun.bayItems[position].isActive) ||
                (testRun.bayItems[position].stepStatus.equals("Failed previous step")) ||
                (testRun.bayItems[position].currentValue<-40))
        {
            // Try to overlay a message that says 'Bay Inactive' and limit touch
            holder.bayInactive.setVisibility(View.VISIBLE);
            holder.bayInactive.bringToFront();
            holder.activeRowLayout.setVisibility(View.INVISIBLE);

            // This changes the height of the inactive row to 100 from 300
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,100);
            holder.bayItem.setLayoutParams(params);
            String text;
            Integer visiblePosition = position+1;

            if (!testRun.bayItems[position].isActive) {
                text = "Bay " + visiblePosition + " is Inactive.  Change in calibration";
                holder.bayInactiveText.setBackgroundColor(Color.YELLOW);
            } else if (testRun.bayItems[position].stepStatus.equals("Failed previous step")) {
                text = "Bay " + visiblePosition + ":Fail step:" + testRun.bayItems[position].failStep + ":"+ testRun.bayItems[position].failCause;
                holder.bayInactiveText.setBackgroundColor(Color.RED);
            } else {
                text = "Calibration Issue - no input - check Phidget";
                holder.bayInactiveText.setBackgroundColor(Color.RED);
            }
            holder.bayInactiveText.setTextSize(15);
            holder.bayInactiveText.setText(text);
        } else {
            holder.bayInactive.setVisibility(View.INVISIBLE);
            holder.activeRowLayout.bringToFront();
            holder.activeRowLayout.setVisibility(View.VISIBLE);

            // This changes the row height back to normal
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,600);
            holder.bayItem.setLayoutParams(params);
        }

        // This is the pass button logic
        switch (testRun.bayItems[position].stepStatus) {
            case "Not Tested":
                //Make sure the button text is set back to default
                holder.failButton.setText("Fail");
                holder.failButton.setBackgroundColor(Color.parseColor("#FF4444"));
                String returnValue = testRun.bayItems[position].isPassReady(testRun.currentTestStep);
                holder.passButton.setText(returnValue);
                switch (returnValue) {
                    case "Pass":
                        // The criteria for pass are met, enable the button and set the color to green
                        holder.passButton.setBackgroundColor(Color.parseColor("#99CC00"));
                        break;
                    case "Passed":
                        // Little bit of a hack to mark the row as pass if the test for this bay is done
                        holder.passButton.setText("Step Complete");
                        holder.failButton.setText("");
                        holder.passButton.setBackgroundColor(Color.parseColor("#99CC00"));
                        holder.failButton.setBackgroundColor(Color.parseColor("#99CC00"));
                        break;
                    default:
                        // criteria not met yet, turn gray and show the return message
                        holder.passButton.setBackgroundColor(Color.LTGRAY);
                        break;
                }
                break;
            case "Passed":
                // Gray things out and set text to passed
                holder.passButton.setText("Step Complete");
                holder.failButton.setText("");
                holder.passButton.setBackgroundColor(Color.parseColor("#99CC00"));
                holder.failButton.setBackgroundColor(Color.parseColor("#99CC00"));
                break;
            default:
                // Step failed for that bay
                holder.passButton.setText("Failed");
                holder.failButton.setText(testRun.bayItems[position].failCause);
                holder.passButton.setBackgroundColor(Color.parseColor("#FF4444"));
                holder.failButton.setBackgroundColor(Color.parseColor("#FF4444"));
                break;
        }

        // Load the data from the array into the view
        holder.mitecBarcodeField.setText(testRun.bayItems[position].mitecBarcode);
        holder.bayNumberField.setText(String.valueOf(testRun.bayItems[position].bayNumber));
        holder.scentairBarcodeField.setText(testRun.bayItems[position].scentairBarcode);
        holder.unitStateField.setText(testRun.bayItems[position].unitState);
        holder.sensorReadingField.setText(String.valueOf(testRun.bayItems[position].currentValue));

        // Lock the barcode fields unless you are in step 1.
        if (testRun.currentTestStep.equals(1)) {
            // Here is where we manage the editing of the barcode fields magically
            if (testRun.bayItems[position].isEditMitec) {
                holder.mitecBarcodeField.setBackgroundColor(Color.LTGRAY);
                holder.mitecBarcodeField.setCursorVisible(true);
                holder.mitecBarcodeField.requestFocus();
            } else if (testRun.bayItems[position].isEditScentair) {
                holder.scentairBarcodeField.setBackgroundColor(Color.LTGRAY);
                holder.scentairBarcodeField.setCursorVisible(true);
                holder.scentairBarcodeField.requestFocus();
            } else {
                // Not editing this row
                holder.mitecBarcodeField.setBackgroundColor(Color.WHITE);
                holder.mitecBarcodeField.setCursorVisible(false);
                holder.scentairBarcodeField.setBackgroundColor(Color.WHITE);
                holder.scentairBarcodeField.setCursorVisible(false);
            }
        } else {
            // Turn the barcode edit text fields into text views to protect them from edits.
            holder.mitecBarcodeField.setEnabled(false);
            holder.mitecBarcodeField.setCursorVisible(false);
            holder.mitecBarcodeField.setKeyListener(null);
            holder.mitecBarcodeField.setBackgroundColor(Color.WHITE);
            holder.scentairBarcodeField.setEnabled(false);
            holder.scentairBarcodeField.setKeyListener(null);
            holder.scentairBarcodeField.setBackgroundColor(Color.WHITE);
            holder.scentairBarcodeField.setCursorVisible(false);
        }

        holder.passButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Only enable the pass button if the criteria have been met.
                View parentRow = (View) v.getParent();
                View grandParent = (View) parentRow.getParent();
                View greatGrandParent = (View) grandParent.getParent();
                View greatGreatGrandParent = (View) greatGrandParent.getParent();

                ListView listView = (ListView) greatGreatGrandParent.getParent();
                final int listViewPosition = listView.getPositionForView(parentRow);
                if (customListener != null) {
                    customListener.onPassButtonClickListener(position, listViewPosition);
                }
            }
        });

        holder.failButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentRow = (View) v.getParent();
                View grandParent = (View) parentRow.getParent();
                View greatGrandParent = (View) grandParent.getParent();
                View greatGreatGrandParent = (View) greatGrandParent.getParent();

                ListView listView = (ListView) greatGreatGrandParent.getParent();
                final int listViewPosition = listView.getPositionForView(parentRow);
                if (customListener != null) {
                    customListener.onFailButtonClickListener(position, listViewPosition);
                }
            }
        });

        holder.scentairBarcodeField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final EditText editText = (EditText) v;
                boolean touchFocusSelect = editText.didTouchFocusSelect();

                if (!hasFocus) {
                    editText.setBackgroundColor(Color.WHITE);
                } else {
                    // Does have focus, lets highlight the field by changing background color
                    editText.setBackgroundColor(Color.LTGRAY);
                    editText.setCursorVisible(true);
                    // Clear the text stored in the edit field
                    editText.setText("");
                    if (customListener != null) {
                        customListener.onScentAirBarCodeFocusChangeListener(position, touchFocusSelect);
                    }
                }
            }
        });

        holder.scentairBarcodeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Need to move the focus and cursor to the next viable Mitec bar code field
                // Need to skip inactive bays
                EditText editText = (EditText) v;

                String candidateText = editText.getText().toString();

                    if (customListener != null) {
                        customListener.onScentAirBarCodeClickListener(position,candidateText);
                    }
                }
        });

        holder.mitecBarcodeField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final EditText editText = (EditText) v;
                Boolean touchFocusSelect = editText.didTouchFocusSelect();

                if (!hasFocus) {
                    editText.setBackgroundColor(Color.WHITE);
                } else {
                    v.setBackgroundColor(Color.LTGRAY);
                    editText.setCursorVisible(true);
                    editText.setText("");
                    if (customListener != null) {
                        customListener.onMitecBarCodeFocusChangeListener(position, touchFocusSelect);
                    }
                }
            }
        });
        holder.mitecBarcodeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Need to move the focus and cursor to the next viable Mitec bar code field
                // Need to skip inactive bays
                EditText editText= (EditText) v;
                String candidateText = editText.getText().toString();

                if (customListener != null) {
                    customListener.onMitecBarCodeClickListener(position,candidateText);
                }
            }
        });
        return rowView;
    }
}