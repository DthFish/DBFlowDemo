package com.dthfish.dbflowdemo.database;

import android.support.annotation.NonNull;

import com.dthfish.dbflowdemo.L;
import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.sql.migration.UpdateTableMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description ${Desc}
 * Author DthFish
 * Date 2017/12/6.
 */
@Database(version = AppDatabase.VERSION)
public class AppDatabase {
    public static final int VERSION = 4;

    @Migration(version = 4, database = AppDatabase.class)
    public static class Migration4 extends UpdateTableMigration<Product> {

        public Migration4(@NonNull Class<Product> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            where(Product_Table.price.eq(0L));
            set(Product_Table.price.eq(10000L));
        }
    }

    @Migration(version = 3, priority = 0, database = AppDatabase.class)
    public static class Migration3Zero extends BaseMigration {

        @Override
        public void onPreMigrate() {
            L.d("Migration3Zero onPreMigrate: ");
        }

        @Override
        public void migrate(DatabaseWrapper database) {
            L.d("Migration3Zero migrate: ");
        }

        @Override
        public void onPostMigrate() {
            L.d("Migration3Zero onPostMigrate: ");
        }

    }

    @Migration(version = 3, priority = 1, database = AppDatabase.class)
    public static class Migration3One extends BaseMigration {

        @Override
        public void onPreMigrate() {
            L.d("Migration3One onPreMigrate: ");
        }

        @Override
        public void migrate(DatabaseWrapper database) {
            L.d("Migration3One migrate: ");
        }

        @Override
        public void onPostMigrate() {
            L.d("Migration3One onPostMigrate: ");
        }
    }

    @Migration(version = 3, priority = 2, database = AppDatabase.class)
    public static class Migration3 extends AlterTableMigration<Product> {
        public Migration3(Class<Product> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            addColumn(SQLiteType.INTEGER, "price");
            addColumn(SQLiteType.TEXT, "manufacturer");
        }
    }
}
