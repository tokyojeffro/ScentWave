package com.scentair.scentwave;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PreferencesActivity extends Activity {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    EditText dbEditText;
    EditText phidgetEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);

        // Get the old values and the editor initialized.
        sharedPreferences=getSharedPreferences(MainActivity.TAG_MYPREFS, Context.MODE_PRIVATE);
        editor= getSharedPreferences(MainActivity.TAG_MYPREFS, Context.MODE_PRIVATE).edit();

        final Button saveButton;
        final Button cancelButton;

        dbEditText=(EditText) findViewById(R.id.database_server_address);
        phidgetEditText=(EditText) findViewById(R.id.phidget_server_address);

        saveButton=(Button) findViewById(R.id.save_prefs_button);
        cancelButton=(Button) findViewById(R.id.cancel_prefs_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dbSaveAddress = dbEditText.getText().toString();
                String phidgetSaveAddress = phidgetEditText.getText().toString();
                editor.putString(MainActivity.TAG_DATABASE_SERVER_ADDRESS,dbSaveAddress);
                editor.putString(MainActivity.TAG_PHIDGET_SERVER_ADDRESS,phidgetSaveAddress);
                editor.commit();
                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Update the view and make sure the fields are properly displayed
        updateView();
    }

    private void updateView() {
        String databaseAddress = sharedPreferences.getString(MainActivity.TAG_DATABASE_SERVER_ADDRESS,"");
        String phidgetAddress = sharedPreferences.getString(MainActivity.TAG_PHIDGET_SERVER_ADDRESS,"");

        TextView text = (TextView) findViewById(R.id.database_server_address);
        text.setText(databaseAddress);

        text = (TextView) findViewById(R.id.phidget_server_address);
        text.setText(phidgetAddress);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}