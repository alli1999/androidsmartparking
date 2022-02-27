package com.example.parksmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Duration extends AppCompatActivity {

    private static final String TAG = "Duration";
    private final String PREFERENCE_DURATION = "duration";
    Button btn;
    TextView curtime, starttime;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duration);

        btn = (Button) findViewById(R.id.but1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                String email = intent.getStringExtra("EMAILID");
                gotoTimer(email);
            }
        });

        int id = getIntent().getIntExtra("ID",0);
        curtime = (TextView) findViewById(R.id.tv1);
        starttime = (TextView) findViewById(R.id.tv3);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        curtime.setText(sdf.format(calendar.getTime()));
        calendar.add(Calendar.HOUR,1);
        starttime.setText(sdf.format(calendar.getTime()));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.UID), MODE_PRIVATE);
        int uid = pref.getInt("sid",0);
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

        Intent intent1 = new Intent(this, HomePage.class);
        startActivity(intent1);
    }

    public void gotoTimer(String email){
        Intent intent = new Intent(this, ReachTimer.class);
        intent.putExtra("EMAILID",email);
        startActivity(intent);
    }

}