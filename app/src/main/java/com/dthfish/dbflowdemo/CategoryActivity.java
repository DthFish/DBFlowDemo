package com.dthfish.dbflowdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.dthfish.dbflowdemo.database.Category;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
    }


    public void insert(View view) {
        Category category = new Category();
        category.name = "food";
        category.save();
    }

    public void query(View view) {
        List<Category> categories = SQLite.select()
                .from(Category.class)
                .queryList();
        L.d("Query: " + categories.toString());
    }

    public void update(View view) {
    }

    public void delete(View view) {
    }
}
