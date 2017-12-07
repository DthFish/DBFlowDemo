package com.dthfish.dbflowdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goToProduct(View view) {
        startActivity(new Intent(this, ProductActivity.class));
    }


    public void goToCategory(View view) {
        startActivity(new Intent(this, CategoryActivity.class));
    }
}
