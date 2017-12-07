package com.dthfish.ship;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.dthfish.base.L;
import com.dthfish.ship.database.ShipProduct;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

/**
 * Description ${Desc}
 * Author DthFish
 * Date 2017/12/7.
 */

public class ShipActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ship);
    }

    public void insert(View view) {
        ShipProduct shipProduct = new ShipProduct();
        shipProduct.name = "Ship" + (System.currentTimeMillis() % 10000);
        shipProduct.save();
    }

    public void query(View view) {
        final List<ShipProduct> shipProducts = SQLite.select()
                .from(ShipProduct.class)
                .queryList();
        L.d("Query: " + shipProducts.toString());

    }

    public void update(View view) {
    }

    public void delete(View view) {
    }
}
