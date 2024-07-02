package com.example.stepcounter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StepCounterActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView stepCountTextView;
    private SharedPreferences sharedPreferences;
    private ListView pastDaysListView;
    Button resetButton;
    private int stepCount = 0;
    private static final String PREFS_NAME = "StepCounterPrefs";
    private static final String STEP_COUNT_KEY = "step_count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stepCountTextView = findViewById(R.id.step_count_text_view);
        pastDaysListView = findViewById(R.id.past_days_list_view);
        resetButton = findViewById(R.id.resetButton);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        loadPastDaysStepCounts();
        stepCount = sharedPreferences.getInt(STEP_COUNT_KEY, 0); // Load the saved step count
        stepCountTextView.setText("Steps: " + stepCount);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepCount = 0;
                stepCountTextView.setText("Steps: " + stepCount);
                saveTodayStepCount(stepCount);
            }
        });


        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer == null) {
                stepCountTextView.setText("Accelerometer sensor not available");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (accelerometer != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            double magnitude = Math.sqrt(x * x + y * y + z * z);

            // Adjust the threshold to detect steps
            if (magnitude > 20) {
                stepCount++;
                stepCountTextView.setText("Steps: " + stepCount);
                Log.e("step count", "onSensorChanged: " + stepCount);
                saveTodayStepCount(stepCount);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void saveTodayStepCount(int stepCount) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(STEP_COUNT_KEY, stepCount);
        editor.apply();
    }

    private void loadPastDaysStepCounts() {
        List<String> pastDaysStepCounts = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            String day = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
            int stepCount = sharedPreferences.getInt(day, 0);
            pastDaysStepCounts.add(day + ": " + stepCount + " steps");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pastDaysStepCounts);
        pastDaysListView.setAdapter(adapter);
    }
}
