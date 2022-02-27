package com.example.parksmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    Button but1, btn2;
    TextView tv1;
    EditText et1, et2;
    FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    RelativeLayout rellay1;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            rellay1.setVisibility(View.VISIBLE);
        }
    };
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        et1 = (EditText) findViewById(R.id.username_et1);
        et2 = (EditText) findViewById(R.id.password_et2);
        rellay1 = (RelativeLayout) findViewById(R.id.rellay1);
        handler.postDelayed(runnable,2000);
        but1 = (Button) findViewById(R.id.registerbtn);
        but1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoRegister();
            }
        });

        btn2 = (Button) findViewById(R.id.login_btn);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseuser = mAuth.getCurrentUser();
                if(mFirebaseuser != null){
                    Toast.makeText(MainActivity.this, "Logged In", Toast.LENGTH_SHORT).show();
                    String email = mFirebaseuser.getEmail();
                    gotoHome(email);
                    //Log.d(TAG, mFirebaseuser.getDisplayName());
                }

                else{
                    Toast.makeText(MainActivity.this, "Please LogIn", Toast.LENGTH_SHORT).show();
                }
            }
        };

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = et1.getText().toString().trim();
                String password = et2.getText().toString().trim();

                if(email.isEmpty()){
                    et1.setError("Please Enter Email");
                    et1.requestFocus();
                }

                else if(password.isEmpty()){
                    et2.setError("Please Enter Password");
                    et2.requestFocus();
                }

                else if(email.isEmpty() && password.isEmpty()){
                    Toast.makeText(MainActivity.this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
                }

                else if(!(email.isEmpty() && password.isEmpty())){
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                            }

                            else{
                                gotoHome(email);
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(MainActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tv1 = (TextView) findViewById(R.id.forgot_password);
        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ForgotPassword.class));
            }
        });
    }

    public void gotoRegister(){
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }

    public void gotoHome(String email){
        Intent intent = new Intent(this, HomePage.class);
        intent.putExtra("EMAILID",email);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
