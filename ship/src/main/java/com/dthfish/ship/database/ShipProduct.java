package com.dthfish.ship.database;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description ${Desc}
 * Author DthFish
 * Date 2017/12/7.
 */
@Table(database = ShipDatabase.class)
public class ShipProduct extends BaseModel{
    @PrimaryKey(autoincrement = true)
    public long id;
    @Column
    public String name;

    @Override
    public String toString() {
        return "ShipProduct{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
