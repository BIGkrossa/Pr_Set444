package com.example.pr_set;

import static android.provider.MediaStore.Images.Media.getBitmap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {
    private Uri filePath;
    private HashMap<String, String> post_info_download = new HashMap<>();
    private BottomNavigationView bottomNavigationView;


    //кнопки и загрузка данных из сети
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //клик на картинку
        CircleImageView profileImageView = findViewById(R.id.profile_image_view);
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        //кнопка выхода из аккаунта
        ImageButton logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Profile.this, LoginActivity.class));
            }
        });

        // переключение между вкладками
        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home) {
                    // Переход на MainActivity
                    startActivity(new Intent(Profile.this, MainActivity.class));
                } else if (item.getItemId() == R.id.add_postt) {
                    // Переход на AddPostActivity
                    startActivity(new Intent(Profile.this, AddPostActivity.class));
                } else if (item.getItemId() == R.id.profile) {
                    // Переход на ProfileActivity
                    startActivity(new Intent(Profile.this, Profile.class));
                }
                return true;
            }
        });

        //загрузка данных при входе на страницу
        TextView un = findViewById(R.id.username_tv);
       FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               String username = snapshot.child("username").getValue().toString();
               String profileImage = snapshot.child("profileImage").getValue().toString();
               un.setText(username);
               if (!profileImage.isEmpty()){
                   Glide.with(Profile.this).load(profileImage).into(profileImageView);
               }
               int count_posts = Integer.parseInt(snapshot.child("postsCount").getValue().toString());
               for(int i = 1; i < count_posts; i++) {
                   snapshot.child("Posts").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString() + "_Post"+i).getValue().toString();
                   String name = snapshot.child("Posts").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString() + "_Post"+i).child("name").getValue().toString();
                   String img = snapshot.child("Posts").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString() + "_Post"+i).child("postImage").getValue().toString();
                   int count_cmp = Integer.parseInt(snapshot.child("Posts").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString() + "_Post"+i).child("countCompounds").getValue().toString());
                   String rgb = snapshot.child("Posts").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString() + "_Post"+i).child("argb").getValue().toString();
                   String[] originalItems = {};
                   for (int j = 0; j < count_cmp; j++){
                       String comp = snapshot.child("Posts").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString() + "_Post"+i).child("Compounds").child("Comp " + j).getValue().toString();

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
    }

    private void draw_post(String name, String username, String img, String[] items, String rgb){
        LinearLayout parent = findViewById(R.id.parent_ll);

        String[] rgb_arr = rgb.split(" ");
        int red = Integer.parseInt(rgb_arr[0]);
        int green = Integer.parseInt(rgb_arr[1]);
        int blue = Integer.parseInt(rgb_arr[2]);
        int color = Color.rgb(red, green, blue);

        LinearLayout post = new LinearLayout(Profile.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dpToPx(20);
        post.setLayoutParams(params);
        post.setOrientation(LinearLayout.VERTICAL);
        post.setBackgroundColor(Color.BLACK);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(130);
        post.setBackground(drawable);

        ImageView post_img = new ImageView(Profile.this);
        post_img.setBackgroundResource(R.drawable.rounded_top_corners);
        Glide.with(Profile.this).load(img).into(post_img);
        post.addView(post_img);

        TextView nameTextView = new TextView(this);
        nameTextView.setText(name);
        nameTextView.setTextColor(Color.BLACK);
        nameTextView.setTextSize(26);
        nameTextView.setGravity(Gravity.CENTER);
        post.addView(nameTextView);


        Button open_list = new Button(Profile.this);
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

        Spinner spinner = new Spinner(Profile.this);
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




    //выбор картинки(аватарки)
    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageActivityResultLauncher.launch(intent);
    }

    //через bitmap берём картинку из хранилища данных, и выводим в circleView
    ActivityResultLauncher<Intent> pickImageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode()==Activity.RESULT_OK && result.getData()!=null && result.getData().getData()!=null){
                        filePath = result.getData().getData();
                        try{
                            ContentResolver contentResolver = getContentResolver();
                            Bitmap bitmap = getBitmap(contentResolver, filePath);
                            CircleImageView profileImageView = findViewById(R.id.profile_image_view);
                            profileImageView.setImageBitmap(bitmap);
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                        uploadImage();
                    }
                }
            }
    );

    //загрузка картинки в firebase storage
    private void uploadImage(){
        if (filePath!=null){
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseStorage.getInstance().getReference().child("images/"+uid)
                    .putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(Profile.this, "Photo upload complete", Toast.LENGTH_SHORT).show();

                            //в поле пользователя меняем ссылку на аватар
                            FirebaseStorage.getInstance().getReference().child("images/"+uid).getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .child("profileImage").setValue(uri.toString());
                                        }
                                    });
                        }
                    });
        }

    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = Profile.this.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }



}