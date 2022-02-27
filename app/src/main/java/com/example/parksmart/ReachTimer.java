package com.example.parksmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirestoreRegistrar;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReachTimer extends AppCompatActivity {

    private static final long start_time = 3600000;
    private static final String TAG = "ReachTimer";
    private final String PREFERENCE_REACH_TIMER = "runtimer";
    private CountDownTimer cdt;
    boolean timer_running;
    long time_left;
    long end_time;
    RelativeLayout rel, rel1;
    TextView tv1, tv2;
    Button reached, cancelled, check;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reach_timer);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.PREFERENCES_PARKING_TIMER), MODE_PRIVATE);
        String dead = preferences.getString("killed", "");
        String location = preferences.getString("location", "");
        Log.d(TAG,dead+" "+location);
        if(dead.equals("yes") && location.equals("parkingtimer")){
            gotoParkTime();
        }

        tv2 = (TextView) findViewById(R.id.counter);
        tv2.setText("Go to the entrance counter");

        rel = (RelativeLayout) findViewById(R.id.relative);
        rel1 = (RelativeLayout) findViewById(R.id.auth);

        reached = (Button) findViewById(R.id.arrive);
        reached.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rel.setVisibility(View.INVISIBLE);
                rel1.setVisibility(View.VISIBLE);
            }
        });

        check = (Button) findViewById(R.id.validate);
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });

        cancelled = (Button) findViewById(R.id.cancel);
        cancelled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.UID), MODE_PRIVATE);
                int uid = pref.getInt("sid",0);
                Log.d(TAG, String.valueOf(uid));
                db.collection("Slots").whereEqualTo("ID", uid).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                Map<String, Object> s = new HashMap<>();
                                s.put("Available",true);
                                if(task.isSuccessful()) {
                                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                        db.collection("Slots").document(documentSnapshot.getId()).update(s);
                                        Log.d(TAG, documentSnapshot.getId() + " => " + documentSnapshot.getData());
                                        break;
                                    }
                                }
                            }
                        });
                gotoHome();
            }
        });
    }

    public void StartTimer(){
        end_time = System.currentTimeMillis() + time_left;

        cdt = new CountDownTimer(time_left,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                time_left = millisUntilFinished;
                UpdateTimer();
            }

            @Override
            public void onFinish() {
                timer_running = false;
                SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.UID), MODE_PRIVATE);
                int uid = pref.getInt("sid",0);
                Toast.makeText(ReachTimer.this, "Time Up", Toast.LENGTH_SHORT).show();
                db.collection("Slots").whereEqualTo("ID", uid).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                Map<String, Object> s = new HashMap<>();
                                s.put("Available",true);
                                if(task.isSuccessful()) {
                                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                        db.collection("Slots").document(documentSnapshot.getId()).update(s);
                                        Log.d(TAG, documentSnapshot.getId() + " => " + documentSnapshot.getData());
                                        break;
                                    }
                                }
                            }
                        });
                reached = (Button) findViewById(R.id.arrive);
                reached.setVisibility(View.GONE);
            }
        }.start();
        timer_running = true;
    }

    public void UpdateTimer(){
        int mins = (int) time_left / 60000;
        int secs = (int) (time_left / 1000) % 60;
        String remaining_time = String.format(Locale.getDefault(), "%02d:%02d", mins, secs);
        tv1 = (TextView) findViewById(R.id.timer);
        tv1.setText(remaining_time);
    }

    public void gotoParkTime(){
        SharedPreferences preferences = getSharedPreferences(getString(R.string.PREFERENCES_PARKING_TIMER), MODE_PRIVATE);
        String dead = preferences.getString("killed", "");
        String location = preferences.getString("location", "");
        Intent intent = new Intent(this,ParkingTimer.class);
        if(!dead.equals("yes") && !location.equals("parkingtimer")) {
            cdt.cancel();
            timer_running = false;
            time_left = start_time;
            UpdateTimer();
        }
        startActivity(intent);
    }

    public void gotoHome(){
        Intent intent1 = new Intent(this, HomePage.class);

        SharedPreferences prefs = getSharedPreferences(PREFERENCE_REACH_TIMER, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("cancelled",true);
        editor.apply();

        cdt.cancel();
        timer_running = false;
        time_left = start_time;
        UpdateTimer();
        startActivity(intent1);
    }

    public void validate(){
        EditText et1 = (EditText) findViewById(R.id.code);
        final String user_code = et1.getText().toString();
        db.document("Codes/Entrance Code").get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Codes c = documentSnapshot.toObject(Codes.class);
                        String code = c.getCode();
                        if(user_code.equals(code)){
                            gotoParkTime();
                        }
                        else{
                            Toast.makeText(ReachTimer.this, "Wrong Code", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        rel.setVisibility(View.VISIBLE);
        rel1.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences prefs = getSharedPreferences(PREFERENCE_REACH_TIMER, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("millisLeft", time_left);
        editor.putBoolean("timerRunning", timer_running);
        editor.putLong("endTime", end_time);
        editor.putString("killed", "yes");

        editor.apply();
    }

    @Override
    public void onStart(){
        super.onStart();

        SharedPreferences prefs = getSharedPreferences(PREFERENCE_REACH_TIMER, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("cancelled", false);
        editor.apply();

        time_left = prefs.getLong("millisLeft", start_time);
        timer_running = prefs.getBoolean("timerRunning", false);

        UpdateTimer();

        if(timer_running){
            end_time = prefs.getLong("endTime", 0);
            time_left = end_time - System.currentTimeMillis();

            if(time_left < 0 ){
                time_left = 0;
                timer_running = false;
                UpdateTimer();
            }
            else{
                StartTimer();
            }
        }
        StartTimer();
    }
}