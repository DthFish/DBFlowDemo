package com.dthfish.dbflowdemo.database;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

/**
 * Description ${Desc}
 * Author DthFish
 * Date 2017/12/6.
 */

@Table(database = AppDatabase.class)
public class Product extends BaseModel {
    @PrimaryKey(autoincrement = true)
    public long id;
    @Column
    public String name;
    @Column//(defaultValue = "10000")// 设置默认 100 块钱，即使忘了录入价格，我们卖出去也不吃亏
    public long price = 10000L;// 分
    @Column
    public String manufacturer;
    @ForeignKey(stubbedRelationship = true, saveForeignKeyModel = true, deleteForeignKeyModel = false)
    public Category category;
    public List<Product> present;
    public List<Product> getPresent() {
        if (present == null || present.isEmpty()) {
            present = SQLite.select()
                    .from(Product.class)
                    .where(Product_Table.name.like("PX%"))
                    .queryList();
        }
        return present;
    }
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", manufacturer='" + manufacturer + '\'' +
                ", category=" + category +
                ", present=" + present +
                '}';
    }
}
