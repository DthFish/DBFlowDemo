package com.dthfish.dbflowdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.dthfish.base.L;
import com.dthfish.dbflowdemo.database.Category;
import com.dthfish.dbflowdemo.database.Category_Table;
import com.dthfish.dbflowdemo.database.Product;
import com.dthfish.dbflowdemo.database.Product_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

/**
 * Description ${Desc}
 * Author DthFish
 * Date 2017/12/7.
 */

public class ProductActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
    }

    public void insert(View view) {
        Product product = new Product();
        product.name = "P" + (System.currentTimeMillis() % 10000);
        product.save();
        L.d("Insert: " + product.toString());
        // 另一种插入
        //FlowManager.getModelAdapter(Product.class).insert(product);//如果 Product 没有继承 BaseModel 则采用这种方式
        // 又一种插入
        /*
        SQLite.insert(Product.class)
                .columnValues(Product_Table.name.eq("P" + (System.currentTimeMillis() % 10000)))
                .execute();
        */
    }

    public void query(View view) {
        List<Product> products = SQLite.select()
                .from(Product.class)
                .where(Product_Table.name.isNotNull()/*, Product_Table.id.greaterThanOrEq(5L)*/)
                .orderBy(Product_Table.id, true)
//                .limit(3)
                .queryList();
        L.d("Query: " + products.toString());

    }

    public void update1(View view) {
        // 第一种方式 先查后改
        Product product = SQLite.select()
                .from(Product.class)
                .querySingle();
        if (product != null) {
            L.d("Update: " + product.name + " update to P0000");
            product.name = "P0000";
            product.update();
        }

    }

    public void update2(View view) {
        L.d("Update: P0000 update to PXXXX");
        // 第二种方式
        SQLite.update(Product.class)
                .set(Product_Table.name.eq("PXXXX"))
                .where(Product_Table.name.eq("P0000"))
                .execute();
    }

    public void delete1(View view) {
        // 第一种方式 先查后删
        Product product = SQLite.select()
                .from(Product.class)
                .querySingle();
        if (product != null) {
            product.delete();
            L.d("Delete: " + product.name);
        }


    }

    public void delete2(View view) {
        L.d("Delete: where name equals PXXXX");
        // 第二种方式
        SQLite.delete(Product.class)
                .where(Product_Table.name.eq("PXXXX"))
                .execute();
    }

    public void foreignKeyInsert(View view) {
        Category category = SQLite.select()
                .from(Category.class)
                .where(Category_Table.name.eq("meat"))
                .querySingle();
        if (category == null) {
            category = new Category();
            category.name = "meat";
        }
        Product product = new Product();
        product.name = "P" + (System.currentTimeMillis() % 10000);
        product.category = category;
        product.save();
        L.d("Insert: " + product.toString());
        mLastInsertId = product.id;
    }

    private long mLastInsertId;

    public void foreignKeyQuery(View view) {
        List<Product> products = SQLite.select()
                .from(Product.class)
                .where(Product_Table.id.eq(mLastInsertId))
                .queryList();
        for (Product product : products) {
            product.category.load();
        }
        L.d("Query: " + products.toString());
    }

    public void foreignKeyMulti(View view) {
        final Product product = SQLite.select()
                .from(Product.class)
                .where(Product_Table.name.notEq("PXXXX"))
                .querySingle();

        if (product != null) {
            L.d("Query:"+product+"`s Present: " + product.getPresent());
        }
    }
}
