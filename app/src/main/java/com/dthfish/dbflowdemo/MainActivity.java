package com.dthfish.dbflowdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.dthfish.dbflowdemo.database.Product;
import com.dthfish.dbflowdemo.database.Product_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void insert(View view) {
        Product product = new Product();
        product.name = "P" + (System.currentTimeMillis() % 10000);
        product.save();
        L.d("Insert: " + product.toString());

//        FlowManager.getModelAdapter(Product.class).insert(product);//如果 Product 没有继承 BaseModel 则采用这种方式
    }

    public void query(View view) {
        List<Product> products = SQLite.select()
                .from(Product.class)
                .where(Product_Table.name.isNotNull(), Product_Table.id.greaterThanOrEq(5L))
                .orderBy(Product_Table.id, true)
                .limit(3)
                .queryList();
        L.d("Query: " + products.toString());
    }

    public void update(View view) {
        Product product = SQLite.select()
                .from(Product.class)
                .querySingle();
        if (product != null) {
            L.d("Update: " + product.name + " update to P0000");
            product.name = "P0000";
            product.update();
        }
    }

    public void delete(View view) {
        Product product = SQLite.select()
                .from(Product.class)
                .querySingle();
        if (product != null) {
            product.delete();
            L.d("Delete: " + product.name);
        }
    }
}
