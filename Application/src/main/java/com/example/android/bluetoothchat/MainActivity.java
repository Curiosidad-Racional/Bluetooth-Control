/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.bluetoothchat;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ViewAnimator;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;
import com.example.android.common.logger.LogFragment;
import com.example.android.common.logger.LogWrapper;
import com.example.android.common.logger.MessageOnlyLogFilter;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class MainActivity extends SampleActivityBase implements SensorEventListener {

    public static final String TAG = "MainActivity";

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;

    // Added
    private BluetoothChatFragment fragment;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private EditText editTextThreshold;
    private Button buttonAcc;
    private ViewAnimator viewAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            fragment = new BluetoothChatFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

        editTextThreshold = (EditText) findViewById(R.id.edit_text_threshold);
        buttonAcc = (Button) findViewById(R.id.button_acc);
        viewAnimator = (ViewAnimator) findViewById(R.id.sample_output);


        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem logToggle = menu.findItem(R.id.menu_toggle_log);
        logToggle.setVisible(viewAnimator instanceof ViewAnimator);
        logToggle.setTitle(mLogShown ? R.string.sample_hide_log : R.string.sample_show_log);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_toggle_log:
                mLogShown = !mLogShown;
                //ViewAnimator output = (ViewAnimator) findViewById(R.id.sample_output);
                if (mLogShown) {
                    viewAnimator.setDisplayedChild(1);
                } else {
                    viewAnimator.setDisplayedChild(0);
                }
                supportInvalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Create a chain of targets that will receive log data */
    @Override
    public void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);
        msgFilter.setNext(logFragment.getLogView());

        Log.i(TAG, "Ready");
    }


    ////////////////
    //   Added    //
    ////////////////

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }


    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }


    private void sendLineMessage(String message) {
        fragment.sendMessage(message + "\n");
    }


    private long lastUpdate = 0;
    private boolean acc_on = false;
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long curTime = System.currentTimeMillis();


        if ((curTime - lastUpdate) > (long)Integer.parseInt(editTextThreshold.getText().toString())) {
            lastUpdate = curTime;

            Sensor mySensor = sensorEvent.sensor;

            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                String X = Integer.toString((int) (x * 9));
                String Y = Integer.toString((int) (y * 9));

                if (acc_on) {
                    sendLineMessage(X + "," + Y + ";" + X + Y);
                }
            }

        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private boolean led_on = false;
    public void onButtonLedClicked(View v) {
        if (led_on) {
            sendLineMessage("apagar led");
        } else {
            sendLineMessage("encender led");
        }

        led_on = !led_on;

        Button button = (Button) v;
        if (led_on) {
            button.setTextColor(Color.BLUE);
        } else {
            button.setTextColor(Color.BLACK);
        }
    }


    private void stateAcc(boolean state) {
        acc_on = state;

        if (acc_on) {
            buttonAcc.setTextColor(Color.BLUE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            buttonAcc.setTextColor(Color.BLACK);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }


    public void onButtonAccClicked(View v) {
        stateAcc(!acc_on);
    }


    public void onButtonRotateClockwiseClicked(View v) {
        sendLineMessage("girar horario");
    }


    public void onButtonRotateCounterclockwiseClicked(View v) {
        sendLineMessage("girar antihorario");
    }


    public void onButtonStopClicked(View v) {
        stateAcc(false);
        sendLineMessage("parar");
    }
}
