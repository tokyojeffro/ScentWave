package com.scentair.scentwave;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

public class BayItemArrayAdapter extends ArrayAdapter<BayItem> {

    private final Context context;
    private final BayItem[] bayItems;

    public BayItemArrayAdapter (Context context, BayItem[] bayItems){
        super(context,R.layout.bayitem,bayItems);
        this.context = context;
        this.bayItems = bayItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.bayitem,parent,false);
        TextView bayNumberField = (TextView) rowView.findViewById(R.id.baynumber);
        TextView mitecBarcodeField = (TextView) rowView.findViewById(R.id.mitecbarcode);
        TextView scentairBarcodeField = (TextView) rowView.findViewById(R.id.scentairbarcode);
        TextView unitStateField = (TextView) rowView.findViewById(R.id.unitstate);
        TextView sensorReadingField = (TextView) rowView.findViewById(R.id.sensorreading);

        mitecBarcodeField.setText(bayItems[position].mitecBarcode);
        bayNumberField.setText(String.valueOf(bayItems[position].bayNumber));
        scentairBarcodeField.setText(bayItems[position].scentairBarcode);
        unitStateField.setText(bayItems[position].unitState);
        sensorReadingField.setText(String.valueOf(bayItems[position].currentValue));

        return rowView;
    }
}