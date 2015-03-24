package com.scentair.scentwave;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class BayItemArrayAdapter extends ArrayAdapter<BayItem> {

    private final Context context;
    private final BayItem[] bayItems;
    private ViewHolder[] viewList;

    customButtonListener customListener;

    public interface customButtonListener {

        void onPassButtonClickListener(int position, int listViewPosition);
        void onFailButtonClickListener(int position, int listViewPosition);
    }

    public void setCustomButtonListener(customButtonListener listener) {
        this.customListener = listener;
    }

    public class ViewHolder {
        TextView bayNumberField;
        TextView mitecBarcodeField;
        TextView scentairBarcodeField;
        TextView unitStateField;
        TextView sensorReadingField;
        Button passButton;
        Button failButton;
    }

    public BayItemArrayAdapter (Context context, BayItem[] bayItems){
        super(context,R.layout.bayitem,bayItems);
        this.context = context;
        this.bayItems = bayItems;
        viewList = new ViewHolder[bayItems.length];
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder viewHolder;


            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.bayitem, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.passButton = (Button) rowView.findViewById(R.id.testpassbutton);
            viewHolder.failButton = (Button) rowView.findViewById(R.id.testfailbutton);

            viewHolder.bayNumberField = (TextView) rowView.findViewById(R.id.baynumber);
            viewHolder.mitecBarcodeField = (TextView) rowView.findViewById(R.id.mitecbarcode);
            viewHolder.scentairBarcodeField = (TextView) rowView.findViewById(R.id.scentairbarcode);
            viewHolder.unitStateField = (TextView) rowView.findViewById(R.id.unitstate);
            viewHolder.sensorReadingField = (TextView) rowView.findViewById(R.id.sensorreading);

            viewHolder.mitecBarcodeField.setText(bayItems[position].mitecBarcode);
            viewHolder.bayNumberField.setText(String.valueOf(bayItems[position].bayNumber));
            viewHolder.scentairBarcodeField.setText(bayItems[position].scentairBarcode);
            viewHolder.unitStateField.setText(bayItems[position].unitState);
            viewHolder.sensorReadingField.setText(String.valueOf(bayItems[position].currentValue));

            if (bayItems[position].stepStatus.equals("Not Tested")){
                //Make sure the button text is set back to default
                viewHolder.passButton.setText("Pass");
                viewHolder.failButton.setText("Fail");
                viewHolder.passButton.setBackgroundColor(Color.parseColor("#99CC00"));
                viewHolder.failButton.setBackgroundColor(Color.parseColor("#FF4444"));
            } else if (bayItems[position].stepStatus.equals("Passed")) {
                // Gray things out and set text to passed
                viewHolder.passButton.setText("Step Complete");
                viewHolder.failButton.setText("");
                viewHolder.passButton.setBackgroundColor(Color.parseColor("#99CC00"));
                viewHolder.failButton.setBackgroundColor(Color.parseColor("#99CC00"));
            } else {
                // Step failed for that bay
                viewHolder.passButton.setText("");
                viewHolder.failButton.setText("Failed");
                viewHolder.passButton.setBackgroundColor(Color.parseColor("#FF4444"));
                viewHolder.failButton.setBackgroundColor(Color.parseColor("#FF4444"));
            }

            viewList[position] = viewHolder;
            rowView.setTag(viewList[position]);

        viewHolder.passButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentRow = (View) v.getParent();
                View parentRow2 = (View) parentRow.getParent();

                ListView listView = (ListView) parentRow2.getParent();
                final int listViewPosition = listView.getPositionForView(parentRow);
                if (customListener != null) {
                    customListener.onPassButtonClickListener(position,listViewPosition);
                }
            }
        });

        viewHolder.failButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentRow = (View) v.getParent();
                ListView listView = (ListView) parentRow.getParent();
                final int listViewPosition = listView.getPositionForView(parentRow);
                if (customListener != null) {
                    customListener.onFailButtonClickListener(position,listViewPosition);
                }
            }
        });

        return rowView;
    }
}