package com.dthfish.dbflowdemo.database;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

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

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
