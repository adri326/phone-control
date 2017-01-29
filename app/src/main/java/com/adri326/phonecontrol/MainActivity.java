package com.adri326.phonecontrol;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    static EditText url;
    static Switch aSwitch;
    static boolean isChecked = true;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startButton = (Button) findViewById(R.id.run_service);
        startButton.setOnClickListener(startButtonListener);
        url = (EditText)findViewById(R.id.URL);
        sharedPref = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
        String URL = "";
        //URL = sharedPref.getString("URL", URL);
        URL = getString(R.string.addr);
        url.setText(URL, TextView.BufferType.EDITABLE);
        aSwitch = (Switch)findViewById(R.id.background);
    }
    View.OnClickListener startButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.v("========TEST=========", "Starting service");
            Context context = getApplicationContext();
            Intent i = new android.content.Intent("com.adri326.phonecontrol.ListenerService");
            i.setClass(context, ListenerService.class);
            context.startService(i);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("URL", url.getText().toString());
            editor.commit();
        }
    };
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        this.isChecked = isChecked;
    }
}
