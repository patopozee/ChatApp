package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Register extends AppCompatActivity {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://chatapp-650ad-default-rtdb.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText name = findViewById(R.id.r_name);
        final EditText mobile = findViewById(R.id.r_mobile);
        final EditText email = findViewById(R.id.r_email);
        final AppCompatButton registerBtn = findViewById(R.id.r_registerBtn);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");


        //check if the user is already logged in
        if (!MemoryData.getData(this).isEmpty()){
            Intent i = new Intent(Register.this,MainActivity.class);
            i.putExtra("mobile", MemoryData.getData(this));
            i.putExtra("name", MemoryData.getName(this));
            i.putExtra("email", "");
            startActivity(i);
            finish();
        }
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();

                final String nameTxt = name.getText().toString();
                final String mobileTxt = mobile.getText().toString();
                final String emailTxt = email.getText().toString();

                if (nameTxt.isEmpty()||mobileTxt.isEmpty() || emailTxt.isEmpty())
                {
                    Toast.makeText(Register.this, "All field required", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                else
                {

                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            progressDialog.dismiss();

                            if (dataSnapshot.child("users").hasChild(mobileTxt)){
                                Toast.makeText(Register.this,"Mobile already exist",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                databaseReference.child("users").child(mobileTxt).child("email").setValue(emailTxt);
                                databaseReference.child("users").child(mobileTxt).child("name").setValue(nameTxt);
                                databaseReference.child("users").child(mobileTxt).child("profile_pic").setValue("https://firebasestorage.googleapis.com/v0/b/chatapp-650ad.appspot.com/o/profile.jpg?alt=media&token=642c461b-de80-453e-a8b5-d4128e11a372");

                                //save mobile to memory
                                MemoryData.saveData(mobileTxt, Register.this);

                                //save name to memory
                                MemoryData.saveName(nameTxt, Register.this);
                                Toast.makeText(Register.this,"Success",Toast.LENGTH_SHORT).show();

                                Intent i = new Intent(Register.this,MainActivity.class);
                                i.putExtra("mobile", mobileTxt);
                                i.putExtra("name", nameTxt);
                                i.putExtra("email", emailTxt);
                                startActivity(i);
                                finish();
                            }



                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            progressDialog.dismiss();
                        }
                    });


                }
            }
        });
    }
}