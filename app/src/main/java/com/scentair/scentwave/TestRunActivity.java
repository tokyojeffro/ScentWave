package com.scentair.scentwave;

import android.app.FragmentManager;
import android.os.Bundle;
import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class TestRunActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testrun);

        //Need to build out the bay list here.
        //The bay list is a set of fragments attached to a special adapter
        ListView listView = (ListView) findViewById(R.id.list_view);

        BayItem[] bayItems= new BayItem[24];

        for(int i=0;i<24;i++){
            bayItems[i]=new BayItem(i+1,"<empty>","<empty>","Unplugged",0);
        }

        BayItemArrayAdapter aa= new BayItemArrayAdapter(this, bayItems);

        listView.setAdapter(aa);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
