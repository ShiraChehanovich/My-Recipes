package com.shirac.myrecipes;

import java.util.ArrayList;

public class TaskObject {

    private String nameOfTask;
    private String[] ingredients;
    private int time;

    public TaskObject(String nameOfTask, String[] ingredients, int time) {
        this.nameOfTask = nameOfTask;
        this.ingredients = ingredients;
        this.time = time;
    }


    public String getNameOfTask() {
        return nameOfTask;
    }

    public String[] getIngredients(){
        return ingredients;
    }

    public int getTime() {
        return time;
    }

    @Override
    public String toString() {
        String time = this.time ==0 ? "" : " for " + this.time + " minutes.";
        String ingredients = this.ingredients[0];
        for (int i = 1; i < this.ingredients.length; i++)
            ingredients = ingredients + " and " + this.ingredients[i];
        return this.nameOfTask + ": " + ingredients + " " + time;
    }

    public String ingredientsToString(){
        String ingredients = this.ingredients[0];
        for (int i = 1; i < this.ingredients.length; i++)
            ingredients = ingredients + ", " + this.ingredients[i];
        return ingredients;
    }

}
