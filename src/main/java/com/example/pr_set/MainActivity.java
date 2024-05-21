package com.example.pr_set;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MenuItem;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    public static String user_id = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            ImageButton search_btn = findViewById(R.id.search_btn);
            search_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, SearchActivity.class));
                }
            });

            LinearLayout subs_layout = findViewById(R.id.subs_layout);
            FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Subscribed").getValue().toString().equals("")) {
                        LinearLayout hor_layout = new LinearLayout(MainActivity.this);
                        hor_layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(200)));
                        hor_layout.setOrientation(LinearLayout.HORIZONTAL);
                        int count = 0;

                        HashMap<String, String> subs = new HashMap<>();
                        subs = (HashMap<String, String>) snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Subscribed").getValue();
                        for (String acc : subs.values()) {
                            if (count == 2) {
                                subs_layout.addView(hor_layout);
                                hor_layout = new LinearLayout(MainActivity.this);
                                hor_layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(200)));
                                hor_layout.setOrientation(LinearLayout.HORIZONTAL);
                                count = 0;
                            }
                            String username = snapshot.child(acc).child("username").getValue().toString();
                            String profileImg = snapshot.child(acc).child("profileImage").getValue().toString();
                            draw_subscribe(username, profileImg, hor_layout, acc);
                            count++;

                        }
                        subs_layout.addView(hor_layout);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            bottomNavigationView = findViewById(R.id.bottom_nav);
            bottomNavigationView.setSelectedItemId(R.id.home);
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId() == R.id.home) {
                        // Переход на MainActivity
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                    } else if (item.getItemId() == R.id.add_postt) {
                        // Переход на AddPostActivity
                        startActivity(new Intent(MainActivity.this, AddPostActivity.class));
                    } else if (item.getItemId() == R.id.profile) {
                        // Переход на ProfileActivity
                        startActivity(new Intent(MainActivity.this, Profile.class));
                    }
                    return true;
                }
            });
        }

    }

    void another_author(String id_author){
        user_id = id_author;
        startActivity(new Intent(MainActivity.this, AnotherProfileActivity.class));
    }

    void draw_subscribe(String username, String img, LinearLayout load, String id){
        LinearLayout sub = new LinearLayout(MainActivity.this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dpToPx(150), dpToPx(200));
        sub.setOrientation(LinearLayout.VERTICAL);
        int leftMargin = 100;
        int topMargin = 16;
        int rightMargin = 16;
        int bottomMargin = 16;
        layoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
        sub.setLayoutParams(layoutParams);

        CircleImageView avatar = new CircleImageView(MainActivity.this);
        avatar.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(150), dpToPx(150)));
        if (!img.equals("")) {
            Glide.with(MainActivity.this).load(img).into(avatar);
        } else {
            avatar.setImageResource(R.drawable.baseline_person_24);
        }
        sub.addView(avatar);

        TextView nameTextView = new TextView(this);
        nameTextView.setText(username);
        nameTextView.setTextSize(26);
        nameTextView.setGravity(Gravity.CENTER);
        sub.addView(nameTextView);

        sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                another_author(id);
            }
        });
        load.addView(sub);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = MainActivity.this.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

}