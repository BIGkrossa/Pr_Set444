package com.example.pr_set;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageButton back_btn = findViewById(R.id.back_btn_sa);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this, MainActivity.class));
            }
        });

        EditText search = findViewById(R.id.search);
        LinearLayout acc_layout = findViewById(R.id.acc_layout);
        CircleImageView avatar = findViewById(R.id.profile_image_view);
        TextView name = findViewById(R.id.author_username);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        HashMap<String, HashMap<String, String>> map = new HashMap<>();
                        map = (HashMap<String, HashMap<String,String>>) snapshot.getValue();
                        for(Map.Entry<String, HashMap<String, String>> entry : map.entrySet()){
                            HashMap<String, String> params = entry.getValue();
                            if (Objects.equals(params.get("username"), search.getText().toString())){
                                name.setText(search.getText().toString());
                                if(!Objects.equals(params.get("profileImage"), "")){
                                    Glide.with(SearchActivity.this).load(params.get("profileImage")).into(avatar);
                                }
                                acc_layout.setVisibility(View.VISIBLE);
                                MainActivity.user_id = entry.getKey();
                                break;
                            } else{
                                acc_layout.setVisibility(View.INVISIBLE);
                                MainActivity.user_id = "";
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        acc_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                another_author(MainActivity.user_id);
            }
        });
    }

    void another_author(String id_author){
        MainActivity.user_id = id_author;
        startActivity(new Intent(SearchActivity.this, AnotherProfileActivity.class));
    }
}