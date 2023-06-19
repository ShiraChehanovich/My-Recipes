package com.shirac.myrecipes;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AddRecipe extends BaseActivity implements View.OnClickListener {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Bitmap image;

    Button btnAddTask, btnAddRecipe, btnaddpicure;
    EditText txtRecipeName, txtTask, txtIngredient, txtMinutes;
    ListView recipeListView;

    ArrayList<TaskObject> tasksList;
    ArrayAdapter<TaskObject> adapter;
    ImageView iconCheckId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);
        btnAddRecipe = findViewById(R.id.btnAddRecipeId);
        btnAddTask = findViewById(R.id.btnAddTaskId);
        btnaddpicure = findViewById(R.id.btnAddPictureId);
        btnAddTask.setOnClickListener(this);
        btnAddRecipe.setOnClickListener(this);
        txtIngredient = findViewById(R.id.txtIngredientId);
        txtMinutes = findViewById(R.id.txtMinutesId);
        txtRecipeName = findViewById(R.id.txtRecipeNameId);
        txtTask = findViewById(R.id.txtTaskId);
        recipeListView = findViewById(R.id.listViewRecipeId);
        iconCheckId = findViewById(R.id.iconCheckId);
        tasksList = new ArrayList<>();

        adapter = new ArrayAdapter<TaskObject>(this, android.R.layout.simple_list_item_1, tasksList);
        recipeListView.setAdapter(adapter);

        // Set OnClickListener to capture the picture
        btnaddpicure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askCameraPermission();
        }});


        txtIngredient.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (hasFocus) {
                    txtIngredient.setError("enter the different ingredients with a comma between them");
                }
            }
        });

    }

    private void askCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != getPackageManager().PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 101);
        } else {
            openCamera();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, 102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 102) {
            assert data != null;
            image = (Bitmap) data.getExtras().get("data");
            iconCheckId.setVisibility(View.VISIBLE);
        }

    }
    public void uploadImageToFirestore(String recName) {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();
            String fileName = "image_" + recName + ".jpg";
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("images").child(fileName);
            UploadTask uploadTask = storageRef.putBytes(imageData);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d("debug", "onSuccess: Success");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Upload image fail", Toast.LENGTH_SHORT).show();
                }
            });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddTaskId:
                if (checkInputs()) {
                    TaskObject taskObject;
                    int time = txtMinutes.getText().toString().isEmpty()? 0: Integer.parseInt(txtMinutes.getText().toString());
                    taskObject = new TaskObject(txtTask.getText().toString(), txtIngredient.getText().toString().split(","), time);
                    addTaskToRecipe(taskObject);
                    clearTexts();
                }
                break;
            case R.id.btnAddRecipeId:
                if(txtRecipeName.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter recipe name", Toast.LENGTH_LONG).show();
                    break;
                }
                //check if name already exist if not - add to table of recipes and create table for recipe
                db.collection("Recipes").document("txtRecipeName.getText().toString()").get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()) {
                                    Toast.makeText(getApplicationContext(), "This recipe already exists. Please enter another name.", Toast.LENGTH_LONG).show();
                                    txtRecipeName.setText("");
                                }
                                else {

                                    Map<String, Object> taskData = new HashMap<>();
                                    Map<String, Object> referencesData = new HashMap<>();
                                    int totalCount = tasksList.size(); // Total number of tasks
                                    final AtomicInteger completedCount = new AtomicInteger(0);

                                    for (int i = 0; i < tasksList.size(); i++) {
                                        final int index = i;
                                        taskData.clear();
                                        // Step 3a: Create a new document with an auto-generated ID

                                        // Step 4: Add data to the document
                                        taskData.put("name", tasksList.get(i).getNameOfTask());
                                        taskData.put("ingredients", tasksList.get(i).ingredientsToString());
                                        taskData.put("time", tasksList.get(i).getTime());

                                        // Step 4a: Save the data to Firestore
                                        db.collection("Tasks").document(txtRecipeName.getText().toString() + "-task-" +(i + 1)).set(taskData)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("debug", "task saved");
                                                        DocumentReference taskRef = db.collection("Tasks")
                                                                .document(txtRecipeName.getText().toString() + "-task-" + (index + 1));
                                                        referencesData.put("task-" + (index + 1), taskRef);
                                                        int count = completedCount.incrementAndGet(); // Increment and get the updated count

                                                        // Check if all tasks have been retrieved
                                                        if (count == totalCount) {
                                                            DocumentReference recipeRef = db.collection("Recipes").document(txtRecipeName.getText().toString());
                                                            recipeRef.set(referencesData);
                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(AddRecipe.this, "Error!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                    if(image != null) {
                                        uploadImageToFirestore(txtRecipeName.getText().toString());
                                    }

                                    Intent intent = new Intent(AddRecipe.this, ChooseActivity.class);
                                    startActivity(intent);
//                                    break;
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddRecipe.this, "Error!", Toast.LENGTH_SHORT).show();
                            }
                        });
        }
    }




    public boolean checkInputs(){
        if(txtTask.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter task", Toast.LENGTH_LONG).show();
            return false;
        }
        if(txtIngredient.getText().toString().isEmpty()){
            Toast.makeText(getApplicationContext(), "Please enter ingredient", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void addTaskToRecipe(TaskObject taskObject){
        //add recipe to list
        tasksList.add(taskObject);

        //refresh the list view
        adapter.notifyDataSetChanged();
    }

    private void clearTexts(){
        txtTask.setText("");
        txtIngredient.setText("");
        txtMinutes.setText("");
    }


}
