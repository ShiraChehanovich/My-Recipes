package com.shirac.myrecipes;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class Recipe extends BaseActivity implements View.OnClickListener {

    private static String CHANNEL1_ID = "channel1";
    private static String CHANNEL1_NAME = "Channel 1 Demo";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String name_recipe;
    private ArrayList<TaskObject> tasksList;

    private ArrayList<String> allIngredients;

    private TextToSpeech tts;
//    private boolean flag_read_out_loud;
    private ArrayAdapter arrayAdapter;
    private Button btnDoneTask;
    private TextView txtTaskName;
    private ListView listViewIngredientsForTask;
    private ImageView imageViewRecipeId;

    private int recipe_task_num = 0;

    private SharedPreferences sp;

    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
//        flag_read_out_loud = false; //default is not to read out loud
//        btnSpeak = findViewById(R.id.btnReadInstructionsId);
        btnDoneTask = findViewById(R.id.btnDoneWithTaskId);
        txtTaskName = findViewById(R.id.txtTaskNameId);
        listViewIngredientsForTask = findViewById(R.id.listViewIngredientsForTaskId);
        imageViewRecipeId = findViewById(R.id.imageViewRecipeId);

        tasksList = new ArrayList<>();
        allIngredients = new ArrayList<>();

//        btnSpeak.setOnClickListener(this);
        btnDoneTask.setOnClickListener(this);

        sp = getSharedPreferences("file", Context.MODE_PRIVATE);

        //mp = MediaPlayer.create(this,R.raw.applause3);

        // init Text To Speech engine
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
//                if (status == TextToSpeech.SUCCESS)
//                {
//                    int result = tts.setLanguage(Locale.US);
//                    if (result == TextToSpeech.LANG_MISSING_DATA  || result == TextToSpeech.LANG_NOT_SUPPORTED)
//                        Log.d("Error:","TextToSpeech Language not supported!");
//                    else
//                        btnSpeak.setEnabled(true);
//                }
//                else {
//                    Log.d("Error:","TextToSpeech initialization failed!");
//                }
            }
        });

        try
        {
            // Opens a current database or creates it
//            recipesDB = openOrCreateDatabase(RECIPES_DB, MODE_PRIVATE, null);
        }
        catch(Exception e){
            Log.d("debug", "Error Creating Database");
        }
        Bundle  extras = getIntent().getExtras();
        name_recipe = extras.getString("selected_recipe");
        String [] table_name_array = name_recipe.split(" ");
        String table_name = table_name_array[0];
        if(table_name_array.length>1){
            for (int i = 1; i < table_name_array.length; i++) {
                table_name += "_"+table_name_array[i];
            }
        }
        name_recipe = table_name;
        this.db.collection("Recipes/" + name_recipe + "/" + name_recipe + "-tasks").get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        // Access individual documents here
                        Map<String, Object> data = document.getData();
                        String ingredients = data.get("ingredients").toString();
                        //converting to array of ingredients
                        String[] ingredients_array = ingredients.split(",");
                        TaskObject taskObject = new TaskObject(data.get("name").toString(), ingredients_array, Integer.parseInt(data.get("time").toString()));
                        // Process the document data as needed
                        tasksList.add(taskObject);
                    }
                    displayAllIngredients();

                    String imagePath = "images/image_" + name_recipe + ".jpg";
                    try {
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference(imagePath);
//        StorageReference imageRef = storageReference.child(imagePath);
                        File  loc = File.createTempFile("tempfile", ".jpg");
                        storageRef.getFile(loc)
                                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        Bitmap image = BitmapFactory.decodeFile(loc.getAbsolutePath());
                                        imageViewRecipeId.setImageBitmap(image);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("debug", "Recipe does not contain any picture");
                                    }
                                });
                    } catch(Exception e) {
                        Log.d("debug", "failed to get pic ");
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage().contains("no such table")) {
//                        btn_add.setClickable(true);//in case that no recipe was added to db will let the user add a recipe
                        return;
                    }
                });

    }

    private void displayAllIngredients() {
        if (tasksList.size() == 0) {
            return;
        }
        for (int i = 0; i < tasksList.size() ; i++) {
            String [] ingredients = tasksList.get(i).getIngredients();
            for (int j = 0; j < ingredients.length; j++) {
                allIngredients.add(ingredients[j]);
            }
        }
        txtTaskName.setText("All ingredients:");
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, allIngredients);
        listViewIngredientsForTask.setAdapter(arrayAdapter);
    }


    private void countMinutes(final int minutes){
        if(minutes == 0)
            return;
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("numMinutes", minutes);
        editor.putString("nameTask", tasksList.get(recipe_task_num-1).getNameOfTask() );
        editor.commit();
        Log.d("debug", "timer set for "+minutes+" minutes for " + tasksList.get(recipe_task_num-1).getNameOfTask());
        Toast.makeText(getApplicationContext(), "timer set for "+minutes+" minutes for " + tasksList.get(recipe_task_num-1).getNameOfTask(), Toast.LENGTH_SHORT).show();
        startService(new Intent(this, MyService.class));
    }


    //*************************display task to user********************************************
    private boolean displayTaskToUser() {
        if(recipe_task_num == tasksList.size()){
            return false;
        }

        txtTaskName.setText(tasksList.get(recipe_task_num).getNameOfTask());
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, tasksList.get(recipe_task_num).getIngredients());
        listViewIngredientsForTask.setAdapter(arrayAdapter);
        if(recipe_task_num>0)
            countMinutes(tasksList.get(recipe_task_num-1).getTime());//set timer for previous task
        recipe_task_num++;
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnDoneWithTaskId:
                if(!displayTaskToUser()) {//if was last done of recipe- displayTaskToUser returns false
                    doneRecipe();
                    break;
                }
                break;
        }
    }


    private void doneRecipe() {
        //set timer for last tsk if necessary :
        countMinutes(tasksList.get(recipe_task_num-1).getTime());
        //finish recipe
        String [] table_name_array = name_recipe.split("_");//dealing w case where name of recipe has spaces
        String table_name = table_name_array[0];
        if(table_name_array.length>1){
            for (int i = 1; i < table_name_array.length; i++) {
                table_name += " "+table_name_array[i];
            }
        }
        Toast.makeText(getApplicationContext(), "Done with recipe "+table_name+"!", Toast.LENGTH_LONG).show();
//        mp.start();
        Intent intent = new Intent(Recipe.this, ChooseActivity.class);
        startActivity(intent);
    }

}
