package com.example.parksmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.razorpay.Checkout;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

public class HomePage extends AppCompatActivity {

    private static final String TAG = "HomePage";
    private final String PREFERENCES_USER = "userdetails";
    Context context;
    Button btn1;
    Button btn2;
    Button btn3;
    ImageButton ibtn;
    FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Boolean wifi = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Intent intent1 = getIntent();
        final String email = intent1.getStringExtra("EMAILID");
        //Log.d(TAG,email);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFERENCES_USER, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("email", email);
        editor.apply();

        db.collection("Users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                UserDetails ud = document.toObject(UserDetails.class);
                                String dbemail = ud.getEmail();
                                if(dbemail.equals(email)){
                                    String docid = document.getId();
                                    SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFERENCES_USER, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("docid", docid);
                                    editor.apply();
                                    Log.d(TAG, document.getId());
                                }
                            }
                        }
                        else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        btn1 = (Button) findViewById(R.id.button);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
                if(activeNetwork != null) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        wifi = true;
                    }
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        wifi = true;
                    }
                }
                else {
                    wifi = false;
                }
                if(wifi) {
                    ChooseSlot();
                }
                else {
                    Toast.makeText(HomePage.this, "Switch on Internet to Book Slot", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn2 = (Button) findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, Map.class);
                startActivity(intent);
            }
        });

        btn3 = (Button) findViewById(R.id.button3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, Payments.class);
                startActivity(intent);
            }
        });

        ibtn = (ImageButton) findViewById(R.id.imageButton2);
        ibtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Logout();
            }
        });
    }

    public void ChooseSlot(){
        Intent intent1 = getIntent();
        String email = intent1.getStringExtra("EMAILID");

        Intent intent = new Intent(this,SlotsMap.class);
        intent.putExtra("EMAILID",email);
        startActivity(intent);
    }

    public void Logout(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = getSharedPreferences(getString(R.string.PREFERENCES_PARKING_TIMER), MODE_PRIVATE);
        String dead = preferences.getString("killed", "");
        String location = preferences.getString("location", "");
        Log.d(TAG,dead+" "+location);
        //slotcancel = prefs.getBoolean("cancelled", false);

        if(location == "parkingtimer") {
            if (dead != null && dead.equals("yes")) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("killed", "no");

                editor.apply();

                Intent intent1 = new Intent(this, ParkingTimer.class);
                startActivity(intent1);
            }
        }

        SharedPreferences prefs = getSharedPreferences(getString(R.string.PREFERENCE_REACH_TIMER), MODE_PRIVATE);
        String kill = prefs.getString("killed", null);
        Boolean slotcancel = prefs.getBoolean("cancelled", false);

        if(!slotcancel) {
            if (kill != null && kill.equals("yes")) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("killed", "no");

                editor.apply();

                Intent intent1 = new Intent(this, ReachTimer.class);
                startActivity(intent1);
            }
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
