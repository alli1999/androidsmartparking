package com.example.parksmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Payments extends AppCompatActivity {

    private static final String TAG = "Payments";
    TextView tv;
    private ArrayList<PaymentsObject> paylist;
    RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        tv = findViewById(R.id.NoPayments);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.PREFERENCES_USER), MODE_PRIVATE);
        String email = pref.getString("email", null);

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(getString(R.string.PREFERENCES_USER), MODE_PRIVATE);
        String docid = prefs.getString("docid", null);
        //Log.d(TAG,docid);

        createPaymentList();
        createRecyclerView();

        db.collection("Users/"+docid+"/Payments").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                Log.d(TAG, documentSnapshot.getId() + " => " + documentSnapshot.getData());
                                Receipt rpt = documentSnapshot.toObject(Receipt.class);
                                int pay = rpt.getPrice();
                                String duration = rpt.getDuration();
                                String slot = rpt.getSlot();
                                String date = rpt.getDate();
                                Log.d("data",pay+" "+duration+" "+slot+" "+date);
                                insertItem(0, pay, duration, slot, date);
                            }
                            int pays = paylist.size();
                            Log.d("plen",Integer.toString(pays));
                            if(pays <= 0){
                                tv.setText("No Payments Yet");
                            }
                        }
                    }
                });
    }

    public void insertItem(int position, int price, String duration, String slot, String date){
        String cost = String.valueOf(price);
        paylist.add(position, new PaymentsObject(R.drawable.carparklogo, cost+" Rs", duration, slot, date));
        Collections.sort(paylist, new Comparator<PaymentsObject>() {
            @Override
            public int compare(PaymentsObject o1, PaymentsObject o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        adapter.notifyItemInserted(position);
    }

    public void createPaymentList(){
        paylist = new ArrayList<>();
    }

    public void createRecyclerView(){
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        adapter = new PaymentAdapter(paylist);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }
}
