package com.example.pr_set;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AnotherProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_another_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String user_id = MainActivity.user_id;
        TextView username_tv = findViewById(R.id.username_tv);
        CircleImageView avatar = findViewById(R.id.profile_image_view);

        FirebaseDatabase.getInstance().getReference().child("Users").child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("username").getValue().toString();
                username_tv.setText(username);

                if (snapshot.child("profileImage").getValue().toString() != ""){
                    Glide.with(AnotherProfileActivity.this).load(snapshot.child("profileImage").getValue().toString()).into(avatar);
                }

                int count_posts = Integer.parseInt(snapshot.child("postsCount").getValue().toString());
                for(int i = 1; i < count_posts; i++) {
                    snapshot.child("Posts").child(user_id + "_Post"+i).getValue().toString();
                    String name = snapshot.child("Posts").child(user_id + "_Post"+i).child("name").getValue().toString();
                    String img = snapshot.child("Posts").child(user_id + "_Post"+i).child("postImage").getValue().toString();
                    int count_cmp = Integer.parseInt(snapshot.child("Posts").child(user_id + "_Post"+i).child("countCompounds").getValue().toString());
                    String rgb = snapshot.child("Posts").child(user_id + "_Post"+i).child("argb").getValue().toString();
                    String[] originalItems = {};
                    for (int j = 0; j < count_cmp; j++){
                        String comp = snapshot.child("Posts").child(user_id + "_Post"+i).child("Compounds").child("Comp " + j).getValue().toString();

                        String[] newItems = new String[originalItems.length + 1];
                        System.arraycopy(originalItems, 0, newItems, 0, originalItems.length);
                        newItems[originalItems.length] = comp;
                        originalItems = newItems;
                    }

                    draw_post(name, username, img, originalItems, rgb);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Button subscribe_btn = findViewById(R.id.subscribe_btn);
        subscribe_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isSubscribed = false;
                        if (!snapshot.child("Subscribed").getValue().equals("")) {
                            HashMap<String, String> subs = new HashMap<>();
                            subs = (HashMap<String, String>) snapshot.child("Subscribed").getValue();
                            for (String acc : subs.values()) {
                                if (acc.equals(user_id)) {
                                    isSubscribed = true;
                                    break;
                                }
                            }
                        }

                        int sub_count = Integer.parseInt(snapshot.child("subCount").getValue().toString());
                        if (!isSubscribed) {
                            FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Subscribed").child("Sub"+String.valueOf(sub_count)).setValue(user_id);
                            FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("subCount").setValue(String.valueOf(sub_count + 1));
                            subscribe_btn.setText("Unsubscribe");
                        } else{
                            HashMap<String, String> subs = new HashMap<>();
                            subs = (HashMap<String, String>) snapshot.child("Subscribed").getValue();
                            HashMap<String, String> subscribed_new = new HashMap<>();
                            boolean isSomething = false;
                            int count = 1;
                            for(String acc : subs.values()){
                                if (!acc.equals(user_id)){
                                    subscribed_new.put("Sub"+String.valueOf(count), acc);
                                    isSomething = true;
                                    count++;
                                }

                            }
                            FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("subCount").setValue(String.valueOf(sub_count - 1));
                            if (isSomething) {
                                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Subscribed").setValue(subscribed_new);
                            } else{
                                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Subscribed").setValue("");
                            }
                            subscribe_btn.setText("Subscribe");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child("Subscribed").getValue().equals("")) {
                    HashMap<String, String> subs = new HashMap<>();
                    boolean isSubscribed = false;
                    subs = (HashMap<String, String>) snapshot.child("Subscribed").getValue();
                    for (String acc : subs.values()) {
                        if (acc.equals(user_id)) {
                            isSubscribed = true;
                            break;
                        }
                    }
                    if (isSubscribed) {
                        subscribe_btn.setText("Unsubscribe");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ImageButton back_btn = findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AnotherProfileActivity.this, MainActivity.class));
            }
        });
    }

    private void draw_post(String name, String username, String img, String[] items, String rgb){
        LinearLayout parent = findViewById(R.id.parent_ll);

        String[] rgb_arr = rgb.split(" ");
        int red = Integer.parseInt(rgb_arr[0]);
        int green = Integer.parseInt(rgb_arr[1]);
        int blue = Integer.parseInt(rgb_arr[2]);
        int color = Color.rgb(red, green, blue);

        LinearLayout post = new LinearLayout(AnotherProfileActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dpToPx(20);
        post.setLayoutParams(params);
        post.setOrientation(LinearLayout.VERTICAL);
        post.setBackgroundColor(Color.BLACK);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(130);
        post.setBackground(drawable);

        ImageView post_img = new ImageView(AnotherProfileActivity.this);
        post_img.setBackgroundResource(R.drawable.rounded_top_corners);
        Glide.with(AnotherProfileActivity.this).load(img).into(post_img);
        post.addView(post_img);

        TextView nameTextView = new TextView(this);
        nameTextView.setText(name);
        nameTextView.setTextColor(Color.BLACK);
        nameTextView.setTextSize(26);
        nameTextView.setGravity(Gravity.CENTER);
        post.addView(nameTextView);


        Button open_list = new Button(AnotherProfileActivity.this);
        open_list.setText("Compounds");
        open_list.setTextColor(color);
        int leftPadding = open_list.getPaddingLeft();
        int topPadding = open_list.getPaddingTop();
        int rightPadding = open_list.getPaddingRight();
        int bottomPadding = open_list.getPaddingBottom();
        GradientDrawable drawable1 = new GradientDrawable();
        float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        drawable1.setCornerRadius(radius);
        drawable1.setColor(Color.rgb(255-red, 255-blue, 255-green));
        open_list.setBackground(drawable1);
        open_list.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);

        Spinner spinner = new Spinner(AnotherProfileActivity.this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setEnabled(false);
        spinner.setVisibility(View.INVISIBLE);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        open_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.performClick();
            }
        });
        post.addView(open_list);
        post.addView(spinner);

        parent.addView(post);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = AnotherProfileActivity.this.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

}