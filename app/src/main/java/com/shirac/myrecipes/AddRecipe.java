package com.shirac.myrecipes;

import android.Manifest;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AddRecipe extends BaseActivity implements View.OnClickListener {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri imageUri;
    private Bitmap image;

    Button btnAddTask, btnAddRecipe, btnAddPicure;
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
        btnAddPicure = findViewById(R.id.btnAddPictureId);
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

        // Declare variables

// Set OnClickListener to capture the picture
        btnAddPicure.setOnClickListener(new View.OnClickListener() {
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
                    // Image upload success
                    // You can retrieve the download URL of the uploaded image using taskSnapshot.getDownloadUrl()
                    // and store it in Firestore or perform any other necessary actions.
//                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    // Store the downloadUrl in Firestore or use it as needed
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Image upload failed
                    // Handle the failure scenario as per your requirements
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
//                db.collection(txtRecipeName.getText().toString()).document("Task-1").get()
                db.document("Recipes/" + txtRecipeName.getText().toString() + "/" + txtRecipeName.getText().toString() + "-Tasks/" + txtRecipeName.getText().toString() + "-task-1").get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()) {
                                    Toast.makeText(getApplicationContext(), "This recipe already exists. Please enter another name.", Toast.LENGTH_LONG).show();
                                    txtRecipeName.setText("");
                                }
                                else {
                                    // build an SQL statement to create specific recipe table (if does not exist)
                                    //remove spaces from table name:
                                    String [] table_name_array = txtRecipeName.getText().toString().split(" ");
                                    String table_name = table_name_array[0];
                                    if(table_name_array.length>1){
                                        for (int i = 1; i < table_name_array.length; i++) {
                                            table_name += "_"+table_name_array[i];
                                        }
                                    }

                                    Map<String, Object> taskData = new HashMap<>();

                                    for (int i = 0; i < tasksList.size(); i++) {
                                        taskData.clear();
                                        // Step 3a: Create a new document with an auto-generated ID

                                        // Step 4: Add data to the document
                                        taskData.put("name", tasksList.get(i).getNameOfTask());
                                        taskData.put("ingredients", tasksList.get(i).ingredientsToString());
                                        taskData.put("time", tasksList.get(i).getTime());

                                        // Step 4a: Save the data to Firestore
//                                        db.collection(txtRecipeName.getText().toString()).document("Task-" +(i + 1)).set(taskData)
                                        db.document("Recipes/" + txtRecipeName.getText().toString() + "/" + txtRecipeName.getText().toString() + "-Tasks/" + txtRecipeName.getText().toString() + "-task-" +(i + 1)).set(taskData)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(AddRecipe.this, "Note saved", Toast.LENGTH_LONG).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(AddRecipe.this, "Error!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                    uploadImageToFirestore(txtRecipeName.getText().toString());

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
