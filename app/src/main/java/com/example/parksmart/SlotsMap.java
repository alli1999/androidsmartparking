package com.example.parksmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class SlotsMap extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SlotsMap";
    private final String UID = "slotID";
    ImageButton ibtn;
    int moved = 0;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RelativeLayout slotrel;
    TextView wait;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slots_map);

        db.collection("Slots").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                Slots slot = documentSnapshot.toObject(Slots.class);
                                boolean avail = slot.isAvailable();
                                int id = slot.getID();
                                boolean reverse = slot.isReverse();
                                if(!avail){
                                    ImageButton ibtn = (ImageButton) findViewById(id);
                                    if(reverse) {
                                        ibtn.setImageResource(R.drawable.noparkingiconreverse);
                                        ibtn.setEnabled(false);
                                    }
                                    else{
                                        ibtn.setImageResource(R.drawable.noparkingicon);
                                        ibtn.setEnabled(false);
                                    }
                                }
                            }
                            slotrel = (RelativeLayout) findViewById(R.id.slotrel);
                            slotrel.setVisibility(View.VISIBLE);
                            wait = (TextView) findViewById(R.id.wait);
                            wait.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public void onClick(View v1) {
        final int uid = v1.getId();
        SharedPreferences pref = getApplicationContext().getSharedPreferences(UID, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("sid", uid);
        editor.apply();
        db.collection("Slots").whereEqualTo("ID", uid).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Map<String, Object> s = new HashMap<>();
                        s.put("Available",false);
                        if(task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                db.collection("Slots").document(documentSnapshot.getId()).update(s);
                                Slots slot = documentSnapshot.toObject(Slots.class);
                                boolean avail = slot.isAvailable();
                                if(avail == false){
                                    ImageButton ibtn = (ImageButton) findViewById(uid);
                                    ibtn.setImageResource(R.drawable.noparkingicon);
                                    ibtn.setEnabled(false);
                                }
                                Log.d(TAG, documentSnapshot.getId() + " => " + documentSnapshot.getData() + " => " + avail);
                                break;
                            }
                        }
                    }
                });

        Log.d(TAG, String.valueOf(uid));

        Intent intent1 = getIntent();
        String email = intent1.getStringExtra("EMAILID");

        Intent intent = new Intent(this,Duration.class);
        intent.putExtra("EMAILID",email);
        startActivity(intent);
    }

    public void onClickR(View v1){
        final int uid = v1.getId();
        SharedPreferences pref = getApplicationContext().getSharedPreferences(UID, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("sid", uid);
        editor.apply();
        db.collection("Slots").whereEqualTo("ID", uid).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Map<String, Object> s = new HashMap<>();
                        s.put("Available",false);
                        if(task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                db.collection("Slots").document(documentSnapshot.getId()).update(s);
                                Slots slot = documentSnapshot.toObject(Slots.class);
                                boolean avail = slot.isAvailable();
                                if(!avail){
                                    ImageButton ibtn = (ImageButton) findViewById(uid);
                                    ibtn.setImageResource(R.drawable.noparkingiconreverse);
                                    ibtn.setEnabled(false);
                                }
                                Log.d(TAG, documentSnapshot.getId() + " => " + documentSnapshot.getData() + " => " + avail);
                                break;
                            }
                        }
                    }
                });

        Intent intent1 = getIntent();
        String email = intent1.getStringExtra("EMAILID");

        Intent intent = new Intent(this,Duration.class);
        intent.putExtra("EMAILID",email);
        startActivity(intent);
    }
}
