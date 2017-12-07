package com.dthfish.dbflowdemo.database;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description ${产品品类}
 * Author zlz
 * Date 2017/12/7.
 */
@Table(database = AppDatabase.class)
public class Category extends BaseModel {
    @PrimaryKey(autoincrement = true)
    public long id;
    @Column
    public String name;

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
