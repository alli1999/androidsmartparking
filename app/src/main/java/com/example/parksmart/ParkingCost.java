package com.example.parksmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ParkingCost extends AppCompatActivity implements PaymentResultListener {

    private static final String TAG = "ParkingCost";
    private final String PREFERENCES_PARK_COST = "parkcost";
    TextView textView, tv1, tv2, tv3;
    EditText et1;
    Button cardpay, counterpay, validate;
    RelativeLayout rel, rel1;
    int pay;
    String start, end, duration;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_cost);
        Checkout.preload(getApplicationContext());

        SharedPreferences preferences = getApplicationContext().getSharedPreferences(PREFERENCES_PARK_COST, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("opened", true);
        editor.apply();

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.PREFERENCES_PARK_TIME_VALS), MODE_PRIVATE);
        pay = pref.getInt("pay", 0);
        start = pref.getString("starttime",null);
        end = pref.getString("endtime",null);
        duration = pref.getString("duration",null);

        rel = (RelativeLayout) findViewById(R.id.layoutpaybuttons);
        rel1 = (RelativeLayout) findViewById(R.id.exitcode);

        textView = (TextView) findViewById(R.id.pay);
        textView.setText(String.valueOf(pay+" Rs"));

        tv1 = (TextView) findViewById(R.id.starttime);
        tv1.setText(start);

        tv2 = (TextView) findViewById(R.id.endtime);
        tv2.setText(end);

        tv3 = (TextView) findViewById(R.id.duration);
        tv3.setText(duration+" (Hours : Minutes)");

        cardpay = (Button) findViewById(R.id.card);
        cardpay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPayment();
            }
        });

        counterpay = (Button) findViewById(R.id.counter);
        counterpay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rel.setVisibility(View.GONE);
                rel1.setVisibility(View.VISIBLE);
            }
        });

        validate = (Button) findViewById(R.id.go);
        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check();
            }
        });

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(getString(R.string.PREFERENCES_USER), MODE_PRIVATE);
        String email = prefs.getString("email", null);

        db.collection("Users").whereEqualTo("email", email).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                String docid = documentSnapshot.getId();
                                SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.PREFERENCES_USER), MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("docid",docid);
                                editor.apply();
                            }
                        }
                    }
                });
    }

    public void check(){
        et1 = (EditText) findViewById(R.id.code);

        final String user_code = et1.getText().toString();
        db.document("Codes/Exit Code").get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Codes c = documentSnapshot.toObject(Codes.class);
                        String code = c.getCode();
                        if(user_code.equals(code)){
                            SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.UID), MODE_PRIVATE);
                            final int uid = pref.getInt("sid",0);
                            db.collection("Slots").whereEqualTo("ID",uid).get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            Map<String, Object> s = new HashMap<>();
                                            s.put("Available",true);
                                            if(task.isSuccessful()){
                                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                                    db.collection("Slots").document(documentSnapshot.getId()).update(s);
                                                    Slots slot = documentSnapshot.toObject(Slots.class);
                                                    boolean avail = slot.isAvailable();
                                                    boolean reverse = slot.isReverse();
                                                    if(avail){
                                                        ImageButton ibtn = (ImageButton) findViewById(uid);
                                                        Log.d(TAG,Integer.toString(uid));
                                                        if(reverse) {
                                                            ibtn.setImageResource(R.drawable.parkingiconreverse);
                                                            ibtn.setEnabled(false);
                                                        }
                                                        else{
                                                            ibtn.setImageResource(R.drawable.parkingicon);
                                                            ibtn.setEnabled(false);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                            Intent intent1 = new Intent(ParkingCost.this, HomePage.class);

                            SharedPreferences prefz = getApplicationContext().getSharedPreferences(PREFERENCES_PARK_COST, MODE_PRIVATE);
                            SharedPreferences.Editor edits = prefz.edit();

                            edits.putBoolean("opened", false);
                            edits.apply();

                            SharedPreferences prefs = getSharedPreferences(getString(R.string.PREFERENCE_REACH_TIMER), MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();

                            editor.putBoolean("cancelled",true);
                            editor.apply();

                            SharedPreferences preference = getSharedPreferences(getString(R.string.PREFERENCES_PARKING_TIMER), MODE_PRIVATE);
                            SharedPreferences.Editor edit = preference.edit();

                            edit.putString("killed", "no");
                            edit.putString("location", " ");

                            edit.apply();

                            SharedPreferences preferences = getSharedPreferences(getString(R.string.PREFERENCES_PARKING_TIMER), MODE_PRIVATE);
                            String dead = preferences.getString("killed", "");
                            String location = preferences.getString("location", "");
                            Log.d(TAG, dead+" "+location);

                            startActivity(intent1);
                        }
                        else{
                            Toast.makeText(ParkingCost.this, "Wrong Code", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String curdate = df.format(date);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.UID), MODE_PRIVATE);
        int uid = pref.getInt("sid",0);

        Map<String, Object> pays = new HashMap<>();
        pays.put("price", pay);
        pays.put("duration", duration);
        pays.put("slot", getResources().getResourceEntryName(uid));
        pays.put("date", curdate);

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(getString(R.string.PREFERENCES_USER), MODE_PRIVATE);
        String docid = prefs.getString("docid", null);

        db.collection("Users/"+docid+"/Payments").add(pays)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Collection added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed");
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        rel.setVisibility(View.VISIBLE);
        rel1.setVisibility(View.GONE);
    }

    public void startPayment() {
        /**
         * Instantiate Checkout
         */
        Checkout checkout = new Checkout();

        /**
         * Set your logo here
         */
        //checkout.setImage(R.drawable.logo);

        /**
         * Reference to current activity
         */
        final Activity activity = this;

        /**
         * Pass your payment options to the Razorpay Checkout as a JSONObject
         */
        try {
            JSONObject options = new JSONObject();

            /**
             * Merchant Name
             * eg: ACME Corp || HasGeek etc.
             */
            options.put("name", "Infinity Mall");

            /**
             * Description can be anything
             * eg: Reference No. #123123 - This order number is passed by you for your internal reference. This is not the `razorpay_order_id`.
             *     Invoice Payment
             *     etc.
             */
            options.put("description", "Parking Payment");

            options.put("currency", "INR");

            /**
             * Amount is always passed in currency subunits
             * Eg: "500" = INR 5.00
             */
            options.put("amount", pay*100);

            checkout.open(activity, options);
        } catch(Exception e) {
            Log.e(TAG, "Error in starting Razorpay Checkout", e);
        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        Toast.makeText(this, "Payment Sucessful", Toast.LENGTH_SHORT).show();
        SharedPreferences prefz = getApplicationContext().getSharedPreferences(PREFERENCES_PARK_COST, MODE_PRIVATE);
        SharedPreferences.Editor edits = prefz.edit();

        edits.putBoolean("opened", false);
        edits.apply();

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.UID), MODE_PRIVATE);
        final int uid = pref.getInt("sid",0);
        db.collection("Slots").whereEqualTo("ID",uid).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Map<String, Object> s = new HashMap<>();
                        s.put("Available",true);
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                db.collection("Slots").document(documentSnapshot.getId()).update(s);
                                Slots slot = documentSnapshot.toObject(Slots.class);
                                boolean avail = slot.isAvailable();
                                boolean reverse = slot.isReverse();
                                if(avail){
                                    ImageButton ibtn = (ImageButton) findViewById(uid);
                                    if(reverse) {
                                        ibtn.setImageResource(R.drawable.parkingiconreverse);
                                        ibtn.setEnabled(false);
                                    }
                                    else{
                                        ibtn.setImageResource(R.drawable.parkingicon);
                                        ibtn.setEnabled(false);
                                    }
                                }
                            }
                        }
                    }
                });

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String curdate = df.format(date);

        Map<String, Object> pays = new HashMap<>();
        pays.put("price", pay);
        pays.put("duration", duration);
        pays.put("slot", getResources().getResourceEntryName(uid));
        pays.put("date", curdate);

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(getString(R.string.PREFERENCES_USER), MODE_PRIVATE);
        String docid = prefs.getString("docid", null);

        db.collection("Users/"+docid+"/Payments").add(pays)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Collection added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed");
                    }
                });

        Intent intent1 = new Intent(ParkingCost.this, HomePage.class);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.PREFERENCE_REACH_TIMER), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("cancelled",true);
        editor.apply();

        SharedPreferences preference = getSharedPreferences(getString(R.string.PREFERENCES_PARKING_TIMER), MODE_PRIVATE);
        SharedPreferences.Editor edit = preference.edit();

        edit.putString("killed", "no");
        edit.putString("location", " ");

        edit.apply();
        SharedPreferences preferencez = getSharedPreferences(getString(R.string.PREFERENCES_PARKING_TIMER), MODE_PRIVATE);
        String dead = preferencez.getString("killed", "");
        String location = preferencez.getString("location", "");
        Log.d(TAG, dead+" "+location);

        startActivity(intent1);
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Payment Not Successful!!!", Toast.LENGTH_SHORT).show();
    }
}
