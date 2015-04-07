package com.scentair.scentwave;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class BayItemArrayAdapter extends ArrayAdapter<BayItem> {

    private final Context context;
    private final BayItem[] bayItems;
    private EditText currentEditField;

    customButtonListener customListener;

    public interface customButtonListener {

        void onPassButtonClickListener(int position, int listViewPosition);
        void onFailButtonClickListener(int position, int listViewPosition);
        void onScentAirBarCodeClickListener(int position, int listViewPosition);
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

    public BayItemArrayAdapter(Context context, BayItem[] bayItems) {
        super(context, R.layout.bayitem, bayItems);
        this.context = context;
        this.bayItems = bayItems;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            ViewHolder viewHolder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.bayitem, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.passButton = (Button) rowView.findViewById(R.id.testpassbutton);
            viewHolder.failButton = (Button) rowView.findViewById(R.id.testfailbutton);

            viewHolder.bayNumberField = (TextView) rowView.findViewById(R.id.baynumber);
            viewHolder.mitecBarcodeField = (EditText) rowView.findViewById(R.id.mitecbarcode);
            viewHolder.scentairBarcodeField = (EditText) rowView.findViewById(R.id.scentairbarcode);

            viewHolder.unitStateField = (TextView) rowView.findViewById(R.id.unitstate);
            viewHolder.sensorReadingField = (TextView) rowView.findViewById(R.id.sensorreading);

            viewHolder.bayInactive = (ViewGroup) rowView.findViewById(R.id.bay_item_inactive_bay);

            viewHolder.activeRowLayout = (View) rowView.findViewById(R.id.bay_active_row_layout);

            viewHolder.bayItem = (ViewGroup) rowView.findViewById(R.id.bay_item);

            viewHolder.bayInactiveText = (TextView) rowView.findViewById(R.id.bay_item_inactive_bay_1st_line);

            rowView.setTag(viewHolder);
        } // end of initializing a new view

        ViewHolder holder = (ViewHolder) rowView.getTag();

        // Check whether the bay is calibrated active or inactive
        if (!bayItems[position].bay.active || (bayItems[position].stepStatus!="Failed" && bayItems[position].failCause!="")) {
            // Try to overlay a message that says 'Bay Inactive' and limit touch
            holder.bayInactive.setVisibility(View.VISIBLE);
            holder.bayInactive.bringToFront();
            holder.activeRowLayout.setVisibility(View.INVISIBLE);

            // This changes the height of the inactive row to 100 from 300
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,100);
            holder.bayItem.setLayoutParams(params);
            String text = new String();
            Integer visiblePosition = position+1;

            if (!bayItems[position].bay.active) {
                text = "Bay " + visiblePosition + " is Inactive.  Change in calibration";
            } else {
                text = "Defect Unit at " + visiblePosition + ":" + bayItems[position].failCause;
            }

            holder.bayInactiveText.setBackgroundColor(Color.YELLOW);
            holder.bayInactiveText.setText(text);

            currentEditField=null;

        } else {
            holder.bayInactive.setVisibility(View.INVISIBLE);
            holder.activeRowLayout.bringToFront();
            holder.activeRowLayout.setVisibility(View.VISIBLE);

            // This changes the row height back to normal
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,500);
            holder.bayItem.setLayoutParams(params);
        }

        if (bayItems[position].stepStatus.equals("Not Tested")) {
            //Make sure the button text is set back to default
            holder.passButton.setText("Pass");
            holder.failButton.setText("Fail");
            holder.passButton.setBackgroundColor(Color.parseColor("#99CC00"));
            holder.failButton.setBackgroundColor(Color.parseColor("#FF4444"));
        } else if (bayItems[position].stepStatus.equals("Passed")) {
            // Gray things out and set text to passed
            holder.passButton.setText("Step Complete");
            holder.failButton.setText("");
            holder.passButton.setBackgroundColor(Color.parseColor("#99CC00"));
            holder.failButton.setBackgroundColor(Color.parseColor("#99CC00"));
        } else {
            // Step failed for that bay
            holder.passButton.setText("Failed");
            holder.failButton.setText(bayItems[position].failCause);
            holder.passButton.setBackgroundColor(Color.parseColor("#FF4444"));
            holder.failButton.setBackgroundColor(Color.parseColor("#FF4444"));
        }

        // Load the data from the array into the view
        holder.mitecBarcodeField.setText(bayItems[position].mitecBarcode);
        holder.bayNumberField.setText(String.valueOf(bayItems[position].bay.bayNumber));
        holder.scentairBarcodeField.setText(bayItems[position].scentairBarcode);
        holder.unitStateField.setText(bayItems[position].unitState);
        holder.sensorReadingField.setText(String.valueOf(bayItems[position].currentValue));

        if (holder.mitecBarcodeField==currentEditField) {
            holder.mitecBarcodeField.setBackgroundColor(Color.GRAY);
            holder.mitecBarcodeField.setCursorVisible(true);
        } else if (holder.scentairBarcodeField==currentEditField) {
            holder.scentairBarcodeField.setBackgroundColor(Color.GRAY);
            holder.scentairBarcodeField.setCursorVisible(true);
        } else {
            // Not editing this row
            holder.mitecBarcodeField.setBackgroundColor(Color.WHITE);
            holder.mitecBarcodeField.setCursorVisible(false);
            holder.scentairBarcodeField.setBackgroundColor(Color.WHITE);
            holder.scentairBarcodeField.setCursorVisible(false);
        }

        holder.passButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentRow = (View) v.getParent();
                View grandParent = (View) parentRow.getParent();
                View greatGrantParent = (View) grandParent.getParent();
                View greatGreatGrandParent = (View) greatGrantParent.getParent();

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
                View greatGrantParent = (View) grandParent.getParent();
                View greatGreatGrandParent = (View) greatGrantParent.getParent();

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
                    bayItems[position].scentairBarcode = editText.getText().toString();
                    editText.setBackgroundColor(Color.WHITE);
                    currentEditField=null;
                } else {
                    // Does have focus, lets highlight the field by changing background color
                    editText.setBackgroundColor(Color.LTGRAY);
                    editText.setCursorVisible(true);
                    currentEditField = editText;
                }
            }
        });

        holder.scentairBarcodeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Need to move the focus and cursor to the next viable Mitec bar code field
                // Need to skip inactive bays
                    View parentRow = (View) v.getParent();
                    View grandParent = (View) parentRow.getParent();
                    View greatGrantParent = (View) grandParent.getParent();
                    View greatGreatGrandParent = (View) greatGrantParent.getParent();

                    ListView listView = (ListView) greatGreatGrandParent.getParent();
                    final int listViewPosition = listView.getPositionForView(parentRow);
                    if (customListener != null) {
                        customListener.onScentAirBarCodeClickListener(position,listViewPosition);
                    }
                }
        });

        holder.mitecBarcodeField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final EditText editText = (EditText) v;
                if (!hasFocus) {
                    bayItems[position].mitecBarcode = editText.getText().toString();
                    editText.setBackgroundColor(Color.WHITE);
                    currentEditField=null;
                } else {
                    v.setBackgroundColor(Color.LTGRAY);
                    editText.setCursorVisible(true);
                    currentEditField = editText;
                }
            }
        });
        return rowView;
    }

    public void passAll(View v) {
        // The operator has scrolled to the end of the bay list and pressed pass all.
        // Update the data to be all 'pass' (do not flag any fails here)
        for (int i = 0; i < bayItems.length; i++) {
            //reset background colors
            bayItems[i].stepStatus = "Passed";
        }
    }
}