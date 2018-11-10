package com.example.klefe.licenseplaterrecognizer;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PlateListActivity extends AppCompatActivity implements PlateListAdapter.ItemClickListener{

    PlateListAdapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid;
    RecyclerView recyclerView;
    List<Plate> plates = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plate_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if( user != null){
            uid = user.getUid();
        }
        recyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);

        getPlateDocumentsForUser();

        // set up the RecyclerView


    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(PlateListActivity.this,PlateViewActivity.class);
        i.putExtra("plateId",adapter.getItem(position));
        startActivity(i);
    }



    private void getPlateDocumentsForUser(){

        db.collection("plates")
                .whereEqualTo("user",uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Plate plate = document.toObject(Plate.class);
                                if(plate.getPlate() != null && plate.getPlateId() != null && plate.getState() != null) {
                                    plates.add(plate);
                                }

                            }
                        } else {
                            Log.w("dddd", "Error getting documents.", task.getException());
                        }
                    }
                }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isComplete()){
                    adapter = new PlateListAdapter(PlateListActivity.this, plates);
                    adapter.setClickListener(PlateListActivity.this);

                    recyclerView.setAdapter(adapter);
                }
            }
        });
    }
}
