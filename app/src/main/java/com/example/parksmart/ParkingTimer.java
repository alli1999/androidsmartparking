package com.example.parksmart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ParkingTimer extends AppCompatActivity {

    private static final String TAG = "ParkingTimer";
    private final String PREFERENCES_PARKING_TIMER = "parktimer";
    private final String PREFERENCES_PARK_TIME_VALS = "timervalues";
    Chronometer chronometer;
    boolean running;
    long pauseOffset;
    Button button;
    TextView text;
    int hourly = 3600000;
    int pay = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_timer);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.UID), MODE_PRIVATE);
        int uid = pref.getInt("sid",0);
        text = (TextView) findViewById(R.id.slottext);
        text.setText("Slot " + getResources().getResourceEntryName(uid));

        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        final String start_time = sdf.format(calendar.getTime());

        button = (Button) findViewById(R.id.done);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                String end_time = sdf.format(cal.getTime());
                Date st = null;
                Date en = null;

                try {
                    st = sdf.parse(start_time);
                    en = sdf.parse(end_time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                long diff = en.getTime() - st.getTime();
                int days = (int) (diff / (1000*60*60*24));
                int hours = (int) ((diff - (1000*60*60*24*days)) / (1000*60*60));
                int mins = (int) (diff - (1000*60*60*24*days) - (1000*60*60*hours)) / (1000*60);
                String h = String.valueOf(hours);
                String m = String.valueOf(mins);
                String duration = h + ":" + m;

                SharedPreferences prefs = getApplicationContext().getSharedPreferences(getString(R.string.PREFERENCES_PARK_COST), MODE_PRIVATE);
                boolean open = prefs.getBoolean("opened", false);
                Log.d(TAG, "open is: "+open);

                if(open == false){
                    Log.d(TAG,"entered");
                    SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFERENCES_PARK_TIME_VALS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();

                    editor.putString("starttime", start_time);
                    editor.apply();
                }
                SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFERENCES_PARK_TIME_VALS, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                Log.d(TAG,start_time + " " + end_time + " " + duration);
                editor.putInt("pay", pay);
                //editor.putString("starttime", start_time);
                editor.putString("endtime", end_time);
                editor.putString("duration", duration);
                editor.apply();
                Intent intent = new Intent(ParkingTimer.this, ParkingCost.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences(PREFERENCES_PARKING_TIMER, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("killed", "yes");
        editor.putString("location","parkingtimer");

        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences(PREFERENCES_PARKING_TIMER, MODE_PRIVATE);
        String kill = prefs.getString("killed", null);
        String loc = prefs.getString("location", null);
        Log.d(TAG,kill+" "+loc);
    }
}
