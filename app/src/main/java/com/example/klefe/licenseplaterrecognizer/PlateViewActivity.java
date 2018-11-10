package com.example.klefe.licenseplaterrecognizer;

import android.content.pm.ActivityInfo;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PlateViewActivity extends AppCompatActivity {

    ImageView imageView;
    private StorageReference mStorageRef;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid;
    String plateId;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plate_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        imageView = findViewById(R.id.plateImage);

        if( user != null){
            uid = user.getUid();
        }
        storageRef = FirebaseStorage.getInstance().getReference();

        if(getIntent().getExtras() != null){
            plateId = getIntent().getStringExtra("plateId");
        }

        storageRef = storageRef.child(uid+"/"+plateId+".jpg");


        loadPlateImage();


    }


    private void loadPlateImage(){
        GlideApp.with(this /* context */)
                .load(storageRef)
                .into(imageView);
    }

}
