package com.shirac.myrecipes;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class AddRecipe extends BaseActivity implements View.OnClickListener {

    public static final String RECIPES_DB = "recipes.db";

    private SQLiteDatabase recipesDB = null;

    Button btnAddTask, btnAddRecipe;
    EditText txtRecipeName, txtTask, txtIngredient, txtMinutes;
    ListView recipeListView;
    ArrayList<TaskObject> tasksList;
    ArrayAdapter<TaskObject> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);
        btnAddRecipe = findViewById(R.id.btnAddRecipeId);
        btnAddTask = findViewById(R.id.btnAddTaskId);
        btnAddTask.setOnClickListener(this);
        btnAddRecipe.setOnClickListener(this);
        txtIngredient = findViewById(R.id.txtIngredientId);
        txtMinutes = findViewById(R.id.txtMinutesId);
        txtRecipeName = findViewById(R.id.txtRecipeNameId);
        txtTask = findViewById(R.id.txtTaskId);
        recipeListView = findViewById(R.id.listViewRecipeId);
        tasksList = new ArrayList<>();

        adapter = new ArrayAdapter<TaskObject>(this, android.R.layout.simple_list_item_1, tasksList);
        recipeListView.setAdapter(adapter);

        createDB();


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
                String sql = "SELECT * FROM recipe_name_table WHERE name = " + txtRecipeName.getText().toString() + " ;";
                try{
                    Cursor cursor = recipesDB.rawQuery(sql, null);
                    if(cursor.getCount()!=0){
                        Toast.makeText(getApplicationContext(), "This recipe already exists. Please enter another name.", Toast.LENGTH_LONG).show();
                        txtRecipeName.setText("");
                        break;
                    }
                }
                catch (SQLiteException e){
                    Log.d("Error", "getMessage: "+e.getMessage());
                }

                sql = "INSERT INTO recipe_name_table (name) VALUES ('" + txtRecipeName.getText().toString() + "');";
                recipesDB.execSQL(sql);

                // build an SQL statement to create specific recipe table (if does not exist)
                //remove spaces from table name:
                String [] table_name_array = txtRecipeName.getText().toString().split(" ");
                String table_name = table_name_array[0];
                if(table_name_array.length>1){
                    for (int i = 1; i < table_name_array.length; i++) {
                        table_name += "_"+table_name_array[i];
                    }
                }
                sql = "CREATE TABLE IF NOT EXISTS " + table_name + " (task VARCHAR, ingredients VARCHAR, time integer);";
                recipesDB.execSQL(sql);

                // Execute SQL statement to insert each task into table that was created for this specific recipe
                for (int i = 0; i < tasksList.size(); i++) {
                    sql = "INSERT INTO " + table_name + " (task, ingredients, time) VALUES ('" + tasksList.get(i).getNameOfTask() + "', '" + tasksList.get(i).ingredientsToString() + "', '" + tasksList.get(i).getTime() + "');";
                    recipesDB.execSQL(sql);
                }

                Intent intent = new Intent(AddRecipe.this, ChooseActivity.class);
                startActivity(intent);
                break;
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

    public void createDB()
    {
        try
        {
            // Opens a current database or creates it
            // Pass the database name, designate that only this app can use it
            // and a DatabaseErrorHandler in the case of database corruption
            recipesDB = openOrCreateDatabase(RECIPES_DB, MODE_PRIVATE, null);

            // build an SQL statement to create 'recipe_name_table' table (if not exists)
            String sql = "CREATE TABLE IF NOT EXISTS recipe_name_table (name VARCHAR primary key);";
            recipesDB.execSQL(sql);
        }

        catch(Exception e){
            Log.d("debug", "Error Creating Database");
        }

    }

}
