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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class BayItemArrayAdapter extends ArrayAdapter<BayItem> {

    private final Context context;
    private final BayItem[] bayItems;
    private Integer currentEditPosition = -1;

    customButtonListener customListener;

    public interface customButtonListener {

        void onPassButtonClickListener(int position, int listViewPosition);
        void onFailButtonClickListener(int position, int listViewPosition);
    }

    public void setCustomButtonListener(customButtonListener listener) {
        this.customListener = listener;
    }

    static class ViewHolder {
        TextView bayNumberField;
        EditText mitecBarcodeField;
        EditText scentairBarcodeField;
        EditText softwareVersionField;
        TextView unitStateField;
        TextView sensorReadingField;
        Button passButton;
        Button failButton;
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
            viewHolder.softwareVersionField = (EditText) rowView.findViewById(R.id.software_version);
            viewHolder.unitStateField = (TextView) rowView.findViewById(R.id.unitstate);
            viewHolder.sensorReadingField = (TextView) rowView.findViewById(R.id.sensorreading);

            rowView.setTag(viewHolder);
        } // end of initializing a new view

        ViewHolder holder = (ViewHolder) rowView.getTag();

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

        if (position != currentEditPosition) {
            //Make sure the text entry fields don't have cursors or highlights
            holder.mitecBarcodeField.setBackgroundColor(Color.WHITE);
            holder.mitecBarcodeField.setCursorVisible(false);
            holder.scentairBarcodeField.setBackgroundColor(Color.WHITE);
            holder.scentairBarcodeField.setCursorVisible(false);
            holder.softwareVersionField.setBackgroundColor(Color.WHITE);
            holder.softwareVersionField.setCursorVisible(false);
        }

        holder.passButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentRow = (View) v.getParent();
                View parentRow2 = (View) parentRow.getParent();

                ListView listView = (ListView) parentRow2.getParent();
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
                View parentRow2 = (View) parentRow.getParent();

                ListView listView = (ListView) parentRow2.getParent();
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
                    currentEditPosition = position;
                } else {
                    // Does have focus, lets highlight the field by changing background color
                    editText.setBackgroundColor(Color.GRAY);
                    currentEditPosition = -1;
                }
            }
        });

        holder.mitecBarcodeField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    final EditText editText = (EditText) v;
                    bayItems[position].mitecBarcode = editText.getText().toString();
                    v.setBackgroundColor(Color.WHITE);
                    currentEditPosition = position;
                } else {
                    v.setBackgroundColor(Color.LTGRAY);
                    currentEditPosition = -1;
                }
            }
        });

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(holder.mitecBarcodeField.getWindowToken(), 0);

        return rowView;
    }

    public void passAll(View v) {
        // The operator has scrolled to the end of the bay list and pressed pass all.
        // Update the data to be all 'pass' (do not flag any fails here)
        for (int i = 0; i < currentEditPosition; i++) {
            //reset background colors
            bayItems[i].stepStatus = "Passed";
        }
    }
}