package com.shirac.myrecipes;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION_CODES.N;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ChooseActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Button btn_choose, btn_add;
    private ArrayAdapter arrayAdapter;
    private ArrayList<String> recipes;
    private ListView recipes_list_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Debug", "start project?");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        btn_add = findViewById(R.id.btn_addId);
        btn_choose = findViewById(R.id.btn_chooseId);
        btn_add.setOnClickListener(this);
        btn_choose.setOnClickListener(this);
        recipes_list_view = findViewById(R.id.listView_recipesId);
        recipes = new ArrayList<>();
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, recipes);
        recipes_list_view.setAdapter(arrayAdapter);

        recipes_list_view.setOnItemClickListener(this);



        try
        {
            // Opens a current database or creates it
//            recipesDB = openOrCreateDatabase(RECIPES_DB, MODE_PRIVATE, null);
        }

        catch(Exception e){
            Log.d("debug", "Error Creating Database");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
        // TODO Auto-generated method stub
//        String name = adapter1.getItem(position);
        String name = recipes.get(position);

        Intent intent = new Intent(ChooseActivity.this, Recipe.class);
        intent.putExtra("selected_recipe", name);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_addId:
                Intent intent = new Intent(ChooseActivity.this, AddRecipe.class);
                startActivity(intent);
                break;
            case R.id.btn_chooseId:
                getRecipesFromDB();
                recipes_list_view.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void getRecipesFromDB() {
        recipes.clear();
        this.db.collection("Recipes").get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        recipes.add(document.getId());
                        // Access individual documents here
//                        Map<String, Object> data = document.getData();
                        // Process the document data as needed
                    }
                    arrayAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage().contains("no such table")) {
                        btn_add.setClickable(true);//in case that no recipe was added to db will let the user add a recipe
                        return;
                    }
                });

    }

}
