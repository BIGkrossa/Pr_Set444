package com.example.pr_set;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private int count_us;
    private boolean ans1 = true;
    private boolean sec = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //элементы (байндинг не работает)
        Button button_log = findViewById(R.id.buttonLog);
        Button button_register = findViewById(R.id.buttonRegM);
        EditText email = findViewById(R.id.editTextTextEmailAddress);
        EditText username = findViewById(R.id.editTextText);
        EditText pass = findViewById(R.id.editTextTextPassword);

        //логин активити
        button_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });



        //сама регистрация
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("Usernames").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int count_us1 = Integer.parseInt(snapshot.child("UsCount").getValue().toString());
                        count_us = count_us1;
                        for(int i = 1; i < count_us1+1; i++){
                            System.out.println(snapshot.child(String.valueOf(i)).getValue().toString());
                            System.out.println(username.getText().toString());
                            if (snapshot.child(String.valueOf(i)).getValue().toString().equals(username.getText().toString())){
                                System.out.println("NO");
                                ans1 = false;
                            }
                        }

                        if (email.getText().toString().isEmpty() || pass.getText().toString().isEmpty() || username.getText().toString().isEmpty() || !ans1){
                            Toast.makeText(getApplicationContext(), "Поля не могут быть пустыми. Или имя уже занято", Toast.LENGTH_SHORT).show();
                        } else{
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.getText().toString(),  pass.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        HashMap<String, String> userinfo = new HashMap<>();
                                        userinfo.put("email", email.getText().toString());
                                        userinfo.put("username", username.getText().toString());
                                        userinfo.put("profileImage", "");
                                        userinfo.put("postsCount", "1");
                                        userinfo.put("subCount", "1");
                                        userinfo.put("Subscribed", "");
                                        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser()
                                                .getUid()).setValue(userinfo);
                                        FirebaseDatabase.getInstance().getReference().child("Usernames").child(String.valueOf((count_us+1))).setValue(username.getText().toString());
                                        FirebaseDatabase.getInstance().getReference().child("Usernames").child("UsCount").setValue(String.valueOf(count_us+1));

                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    }
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }


}