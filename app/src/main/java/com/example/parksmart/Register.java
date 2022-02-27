package com.example.parksmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private static final String TAG = "Register";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    EditText et1, et2, et3;
    Button btn1;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        et1 = (EditText) findViewById(R.id.fname_et);
        et2 = (EditText) findViewById(R.id.email_et);
        et3 = (EditText) findViewById(R.id.pass_et);
        btn1 = (Button) findViewById(R.id.loginbtn);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = et1.getText().toString().trim();
                final String email = et2.getText().toString().trim();
                final String password = et3.getText().toString().trim();

                if(name.isEmpty()){
                    et1.setError("Please Enter Name");
                    et1.requestFocus();
                }

                if(email.isEmpty()){
                    et2.setError("Please Enter Email");
                    et2.requestFocus();
                }

                else if(password.isEmpty()){
                    et3.setError("Please Enter Password");
                    et3.requestFocus();
                }

                else if(email.isEmpty() && password.isEmpty()){
                    Toast.makeText(Register.this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
                }

                else if(!(email.isEmpty() && password.isEmpty())){
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(Register.this, "Sign Up Unsucessful, Try Again", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                CollectionReference Users = db.collection("Users");
                                addUser user = new addUser(name, email, password);
                                Users.add(user)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(Register.this, "Welcome"+name, Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                gotoLogin();
                            }
                        }
                    });
                }

                else{
                    Toast.makeText(Register.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void gotoLogin(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
