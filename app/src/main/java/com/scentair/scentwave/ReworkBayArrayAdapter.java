package com.scentair.scentwave;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ReworkBayArrayAdapter extends ArrayAdapter<BayItem> {
    private final Context context;
    private TestRun testRun;
    static class ViewHolder {
        TextView bayNumberField;
    }
    public ReworkBayArrayAdapter(Context context, TestRun _testRun) {
        super(context, R.layout.rework_bay, _testRun.bayItems);
        this.context = context;
        this.testRun = new TestRun();
        this.testRun = _testRun.getTestRun();
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView==null) {
            ViewHolder viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.rework_bay, parent, false);
            viewHolder.bayNumberField = (TextView) rowView.findViewById(R.id.rework_bay_number);
            viewHolder.bayNumberField.setTextColor(Color.BLACK);
            rowView.setTag(viewHolder);
            viewHolder.bayNumberField.setTextSize(20);
        } // end of initializing a new view
        ViewHolder holder = (ViewHolder) rowView.getTag();
        if (testRun.bayItems[position].isActive) {
            if (testRun.bayItems[position].isFailed) {
                // Bay is active, set background color and text accordingly
                holder.bayNumberField.setBackgroundResource(R.drawable.results_cell_failed);
                holder.bayNumberField.setBackgroundColor(Color.RED);
            } else {
                holder.bayNumberField.setBackgroundResource(R.drawable.results_cell_passed);
                holder.bayNumberField.setBackgroundColor(Color.GREEN);
            }
        } else {
            holder.bayNumberField.setBackgroundResource(R.drawable.results_cell_inactive);
            holder.bayNumberField.setBackgroundColor(Color.DKGRAY);
        }
        // Need to update the rows to show the proper 1,3,5... 2,4,6 info
        Integer displayNumber;
        if (position==0) {
            displayNumber=1;
        } else {
            if (position<12) {
                // This is the top/odd row
                displayNumber = (position+1) + (position+1) - 1;
            } else {
                // This is the even/bottom row
                displayNumber = 24 - (24-position+1) - (24-position+1);
            }
        }
        holder.bayNumberField.setText(String.valueOf(displayNumber));
        return rowView;
    }
}