package com.shirac.myrecipes;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

public class Recipe extends BaseActivity implements View.OnClickListener {

    private static String CHANNEL1_ID = "channel1";
    private static String CHANNEL1_NAME = "Channel 1 Demo";

    private static final String RECIPES_DB = "recipes.db";
    private SQLiteDatabase recipesDB = null;

    private String name_recipe;
    private ArrayList<TaskObject> tasksList;

    private ArrayList<String> allIngredients;

    private TextToSpeech tts;
    private boolean flag_read_out_loud;
    private ArrayAdapter arrayAdapter;
    private Button btnSpeak, btnDoneTask;
    private TextView txtTaskName;
    private ListView listViewIngredientsForTask;

    private int recipe_task_num = 0;

    private SharedPreferences sp;

    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        flag_read_out_loud = false; //default is not to read out loud
        btnSpeak = findViewById(R.id.btnReadInstructionsId);
        btnDoneTask = findViewById(R.id.btnDoneWithTaskId);
        txtTaskName = findViewById(R.id.txtTaskNameId);
        listViewIngredientsForTask = findViewById(R.id.listViewIngredientsForTaskId);

        tasksList = new ArrayList<>();
        allIngredients = new ArrayList<>();

        btnSpeak.setOnClickListener(this);
        btnDoneTask.setOnClickListener(this);

        sp = getSharedPreferences("file", Context.MODE_PRIVATE);

//        mp = MediaPlayer.create(this,R.raw.applause3);

        // init Text To Speech engine
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                if (status == TextToSpeech.SUCCESS)
                {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA  || result == TextToSpeech.LANG_NOT_SUPPORTED)
                        Log.d("Error:","TextToSpeech Language not supported!");
                    else
                        btnSpeak.setEnabled(true);
                }
                else {
                    Log.d("Error:","TextToSpeech initialization failed!");
                }
            }
        });

        try
        {
            // Opens a current database or creates it
            recipesDB = openOrCreateDatabase(RECIPES_DB, MODE_PRIVATE, null);
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
        String sql = "SELECT * FROM " + name_recipe + ";";
        try {
            Cursor cursor = recipesDB.rawQuery(sql, null);
            Log.d("Error", "*");
            cursor.moveToFirst();
            Log.d("Error", "**");
            if(cursor.getCount()==0){
                Log.d("Error", "***");
                Toast.makeText(getApplicationContext(), "this recipe is empty", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Recipe.this, ChooseActivity.class);
                startActivity(intent);
                return;
            }
            Log.d("Error", "before do");
            do {
                Log.d("Error", "after do");
                //getting info for current task in table in db
//                String task = cursor.getString(cursor.getColumnIndex("task"));
                String ingredients = cursor.getString(cursor.getColumnIndexOrThrow("ingredients"));
                //converting to array of ingredients
                String[] ingredients_array = ingredients.split(",");
                int time = cursor.getInt(2);
                //creating task object for task
//                TaskObject taskObject = new TaskObject(task, ingredients_array, time);
                //adding task object to list
//                tasksList.add(taskObject);
            } while (cursor.moveToNext());
        }
        catch (SQLiteException e){
            Log.d("Error", "oopsssssssssssss");
        }

        displayAllIngredients();
    }

    private void displayAllIngredients() {
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
        Toast.makeText(getApplicationContext(), "timer set for "+minutes+" minutes for " + tasksList.get(recipe_task_num-1).getNameOfTask(), Toast.LENGTH_SHORT).show();
//        startService(new Intent(this, MyService.class));
    }

    public void speakUp(TaskObject taskObject){

        String textToSpeak = taskObject.toString();  //task object in format to be read

        // Speech pitch. 1.0 is the normal pitch, lower values lower the tone of
        // the synthesized voice, greater values increase it.
        tts.setPitch(1.0f);

        // Speech rate. 1.0 is the normal speech rate, lower values slow down
        // the speech (0.5 is half the normal speech rate), greater values
        // accelerate it (2.0 is twice the normal speech rate).
        tts.setSpeechRate(1.0f);

        // speak up the string text
        tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    //*************************display task to user********************************************
    private boolean displayTaskToUser() {
        if(recipe_task_num == tasksList.size()){
            return false;
        }
        if(flag_read_out_loud)
            speakUp(tasksList.get(recipe_task_num));
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
            case R.id.btnReadInstructionsId:
                if(flag_read_out_loud)
                    flag_read_out_loud = false;
                else {
                    flag_read_out_loud = true;
                    if(txtTaskName.getText()!="All ingredients:")//the speak up was clicked after began reading recipe - play current task out loud
                        speakUp(tasksList.get(recipe_task_num-1));
                }
                Toast.makeText(getApplicationContext(), "speak up "+flag_read_out_loud, Toast.LENGTH_SHORT).show();
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
        mp.start();
        Intent intent = new Intent(Recipe.this, ChooseActivity.class);
        startActivity(intent);
    }

}
