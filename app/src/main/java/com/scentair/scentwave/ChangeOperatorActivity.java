package com.scentair.scentwave;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ChangeOperatorActivity extends ListActivity {

    private static String[] operatorList;
    SharedPreferences.Editor editor;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        operatorList = MainActivity.operators.getOperators();

        this.setListAdapter(new ArrayAdapter<String>(this, R.layout.changeoperator,R.id.operator_name,operatorList));
        ListView lv = getListView();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Selected item
                String operator = ((TextView) view).getText().toString();
                // Put the value back into the app preferences for the next test run start to pick up

                editor = getSharedPreferences(MainActivity.MyPreferences,MODE_PRIVATE).edit();
                editor.putString(MainActivity.OPERATOR_NAME,operator);
                editor.commit();

                // Go back to the main menu
 //               Intent intent = new Intent(getApplicationContext(),MainActivity.class);
 //               startActivity(intent);

                // Exit this activity
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}