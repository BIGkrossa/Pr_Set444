package com.example.pr_set;

import static android.provider.MediaStore.Images.Media.getBitmap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.ArrayList;
import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {
    private Uri filePath;
    String uri_ans = "";
    private boolean wait = false;
    private int count_comp_fields = 0;
    private ArrayList<Integer> id_compats_list = new ArrayList<>();
    private HashMap<String, String> compounds = new HashMap<>();
    private String rgb;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        add_compound_field();
        //элементы
        Button createBtn = findViewById(R.id.createBtn);
        Button add_comp= findViewById(R.id.add_copm_btn);
        Button pickImg = findViewById(R.id.selectImageButton);
        EditText name = findViewById(R.id.name);
        //создать пост
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!name.getText().toString().isEmpty() & !name.getText().toString().isEmpty() & uri_ans != "" & wait){
                    FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int count_posts = Integer.parseInt(snapshot.child("postsCount").getValue().toString());
                            HashMap<String, String> postinfo = new HashMap<>();
                            create_hash();
                            postinfo.put("name", name.getText().toString());
                            postinfo.put("postImage", uri_ans);
                            postinfo.put("argb", rgb);
                            postinfo.put("countCompounds", String.valueOf(count_comp_fields));
                            String post = FirebaseAuth.getInstance().getCurrentUser().getUid() + "_Post" + count_posts;
                            FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser()
                                    .getUid()).child("Posts").child(post).setValue(postinfo);
                            FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser()
                                    .getUid()).child("Posts").child(post).child("Compounds").setValue(compounds);
                            String new_count = String.valueOf(count_posts + 1);
                            FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("postsCount").setValue(new_count);
                            startActivity(new Intent(AddPostActivity.this, MainActivity.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });



        //выбрать картинку
        pickImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.add_postt);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home) {
                    // Переход на MainActivity
                    startActivity(new Intent(AddPostActivity.this, MainActivity.class));
                } else if (item.getItemId() == R.id.add_postt) {
                    // Переход на AddPostActivity
                    startActivity(new Intent(AddPostActivity.this, AddPostActivity.class));
                } else if (item.getItemId() == R.id.profile) {
                    // Переход на ProfileActivity
                    startActivity(new Intent(AddPostActivity.this, Profile.class));
                }
                return true;
            }
        });


        add_comp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_compound_field();
            }
        });
    }

    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> pickImageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode()== Activity.RESULT_OK && result.getData()!=null && result.getData().getData()!=null){
                        filePath = result.getData().getData();
                        try{
                            ContentResolver contentResolver = getContentResolver();
                            Bitmap bitmap = getBitmap(contentResolver, filePath);
                            ImageView postImageView = findViewById(R.id.imageView3);
                            rgb = getAverageColor(bitmap);
                            postImageView.setImageBitmap(bitmap);
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                        uploadImage();
                    }
                }
            }
    );

    private void uploadImage(){
        if (filePath!=null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count_posts = Integer.parseInt(snapshot.child("postsCount").getValue().toString());
                    String post = uid + "_Post" + count_posts;

                    FirebaseStorage.getInstance().getReference().child("images/"+post)
                            .putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(AddPostActivity.this, "Photo upload complete", Toast.LENGTH_SHORT).show();
                                    wait = true;

                                    FirebaseStorage.getInstance().getReference().child("images/"+post).getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    uri_ans = uri.toString();
                                                    System.out.println(uri_ans);
                                                }
                                            });
                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        }

        private void add_compound_field(){
            if (count_comp_fields <= 5) {
                LinearLayout compounds = findViewById(R.id.compounds);
                EditText comp = new EditText(this);
                comp.setHint("Element " + (count_comp_fields+1));
                int editTextId = View.generateViewId();
                comp.setId(editTextId);
                compounds.addView(comp);
                id_compats_list.add(editTextId);
                count_comp_fields++;
            } else{
                Toast.makeText(AddPostActivity.this, "Not more 5", Toast.LENGTH_SHORT).show();
            }
        }

        private void create_hash(){
            for (int i = 0; i < count_comp_fields; i++){
                EditText comp = findViewById(id_compats_list.get(i));
                if (comp.getText() != null)
                    compounds.put("Comp " + i, comp.getText().toString());
            }
        }

    public static String getAverageColor(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int pixelCount = width * height;
        int totalRed = 0, totalGreen = 0, totalBlue = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = bitmap.getPixel(x, y);
                totalRed += (color >> 16) & 0xFF;
                totalGreen += (color >> 8) & 0xFF;
                totalBlue += color & 0xFF;
            }
        }

        int avgRed = totalRed / pixelCount;
        int avgGreen = totalGreen / pixelCount;
        int avgBlue = totalBlue / pixelCount;

        return avgRed + " " + avgGreen + " " + avgBlue;
    }

    }



