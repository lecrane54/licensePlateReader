package com.example.klefe.licenseplaterrecognizer;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Button captureButton, listButton;
    private CameraView cameraView;
    FirebaseVisionCloudDetectorOptions options;
    List<FirebaseVisionCloudLabel> labelList;
    FirebaseVisionTextRecognizer textRecognizer;
    ImageView imageView;
    Bitmap bitmap;
    String plateString,stateString;
    Integer plateInt;
    Rect state;
    Rect plate;
    List<String> list = new ArrayList<>();
    List<String> elementList = new ArrayList<>();
    List<String> blockList = new ArrayList<>();
    List<String> lineList = new ArrayList<>();
    List<Rect> rectList = new ArrayList<>();
    private StorageReference mStorageRef;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid;
    String uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        list.addAll(Arrays.asList(getResources().getStringArray(R.array.states)));
        imageView = findViewById(R.id.image);
        cameraView = findViewById(R.id.cameraView);
        listButton = findViewById(R.id.toList);
        cameraView.setCropOutput(true);

        if( user != null){
            uid = user.getUid();
        }

        captureButton = findViewById(R.id.btnDetectObject);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        options = new FirebaseVisionCloudDetectorOptions.Builder()
                .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                .setMaxResults(30)
                .build();
        textRecognizer = FirebaseVision.getInstance().getCloudTextRecognizer();


        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                rectList.clear();
                elementList.clear();
                blockList.clear();
                lineList.clear();
                bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap, 600, 600, false);


                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

                getLabelsForImage(image);


            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,PlateListActivity.class);
                startActivity(i);
            }
        });
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    public void getLabelsForImage(final FirebaseVisionImage image) {
        FirebaseVisionCloudLabelDetector detector = FirebaseVision.getInstance().getVisionCloudLabelDetector(options);
        Task<List<FirebaseVisionCloudLabel>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionCloudLabel>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionCloudLabel> labels) {
                                        labelList = labels;
                                        checkIfListContains(image);

                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
    }


    public void checkIfListContains(FirebaseVisionImage image) {
        for (int i = 0; i < labelList.size(); i++) {
            if (labelList.get(i).getLabel().contains("plate") || labelList.get(i).getLabel().contains("registration") || labelList.get(i).getLabel().contains("vehicle")) {
                getLicensePlateString(image);
            }
        }


    }

    public void getLicensePlateString(FirebaseVisionImage image) {
        textRecognizer.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText result) {
                        getText(result);
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("dddd",e.getLocalizedMessage());
                            }
                        });
    }


    private void getText(FirebaseVisionText result){
        Canvas canvas = new Canvas(bitmap);

        // Initialize a new Paint instance to draw the Rectangle
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(6);
        paint.setAntiAlias(true);
        String resultText = result.getText();
      //  Log.d("dddd","Result:  " + resultText);
        for (FirebaseVisionText.TextBlock block: result.getTextBlocks()) {
            String blockText = block.getText();
            Log.d("dddd","block: " +blockText);
            blockText = blockText.replaceAll("[^A-Za-z0-9]", "");
            blockText = blockText.trim();

            blockList.add(blockText);

            Float blockConfidence = block.getConfidence();
            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            for (FirebaseVisionText.Line line: block.getLines()) {
                String lineText = line.getText();
                lineText = lineText.replaceAll("[^A-Za-z0-9]", "");
                lineText = lineText.trim();
                lineList.add(lineText);
                Log.d("dddd","Line: " +lineText);
                Float lineConfidence = line.getConfidence();
                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();
                for (FirebaseVisionText.Element element: line.getElements()) {
                    String elementText = element.getText();
                    elementText = elementText.replaceAll("[^A-Za-z0-9]", "");
                    elementText = elementText.trim();
                    elementList.add(elementText);
                    Float elementConfidence = element.getConfidence();
                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                    Point[] elementCornerPoints = element.getCornerPoints();
                    Rect elementFrame = element.getBoundingBox();
                    rectList.add(elementFrame);


                }
            }
        }

       if(checkIfState()){
           if(getStateText()){
               Log.d("dddd","STATE: "+ stateString);

               blockList.remove(stateString);
                removeBlocks();
                addPlateRect(canvas);

               canvas.drawRect(state,paint);
               Paint text = new Paint();
               text.setTypeface(Typeface.SERIF);
               text.setColor(Color.YELLOW);
               text.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));

               canvas.drawText(stateString,state.left,state.top,text);
               imageView.setImageBitmap(bitmap);
               uploadImage();
           }else{
               Log.d("dddd","Nope");
           }


       }else{
           Log.d("dddd","Not a state");
       }

    }

    private void addPlateRect(Canvas canvas){
        Rect plateRect = null;
        for(int i = 0; i < elementList.size(); i++){

            if(elementList.get(i).equalsIgnoreCase(plateString)){

                plateRect = rectList.get(i);
                break;
            }
        }
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(6);
        paint.setAntiAlias(true);

        if(plateRect != null){
            canvas.drawRect(plateRect,paint);
            Paint text = new Paint();
            text.setTypeface(Typeface.SERIF);
            text.setColor(Color.YELLOW);
            text.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));

            canvas.drawText(plateString,plateRect.centerX(),plateRect.bottom,text);
        }



    }

    public boolean getStateText(){ //TODO: switch up loops

        for(int i = 0; i < list.size(); i++){
            for(int j = 0; j < elementList.size(); j++){
                if(elementList.get(j).equalsIgnoreCase(list.get(i))){
                    stateString = elementList.get(j);
                    state = rectList.get(j);
                    return true;
                }

            }
        }

        return false;

    }


    public void removeBlocks(){

        for(int i = 0; i < blockList.size(); i++){

            if(checkIfValidLicensePlate(blockList.get(i))){
                Log.d("dddd","PLATE:  " + blockList.get(i));
                plateString = blockList.get(i);
                plateInt = i;
                break;
            }

        }

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
                                Log.d("dddd", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w("dddd", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void addToDocuments(){
        Map<String, Object> plateMap = new HashMap<>();
        plateMap.put("user",uid);
        plateMap.put("plate",uuid);
        plateMap.put("state",stateString);
        plateMap.put("plateId",plateString);


// Add a new document with a generated ID
        db.collection("plates")
                .add(plateMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("dddd", "DocumentSnapshot added with ID: " + documentReference.getId());
                        getPlateDocumentsForUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("dddd", "Error adding document", e);
                    }
                });
    }

    private void uploadImage(){

        uuid = UUID.randomUUID().toString();
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mStorageRef.child(uid+"/"+uuid+".jpg").putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this,"Image Uploaded",Toast.LENGTH_LONG).show();
                addToDocuments();

            }
        });


    }


    public boolean checkIfState(){
        if(Collections.disjoint(list, elementList)){
            return true;
        }else{
            return false;
        }
    }

    public boolean checkIfValidLicensePlate(String text) {

        if (text.matches("^.{1,8}$")) {
            return true;
        } else {
            return false;
        }
    }
}



