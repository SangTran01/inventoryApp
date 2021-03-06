package com.example.sangtran.abproject7.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.sangtran.abproject7.data.ItemContract.ItemEntry;

/**
 * Created by Sang Tran on 2016-12-23.
 */

public class ItemDbHelper extends SQLiteOpenHelper {

    static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ItemEntry.TABLE_NAME +
                    " (" +
                    ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL," +
                    ItemEntry.COLUMN_ITEM_PRICE + " DOUBLE NOT NULL," +
                    ItemEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL," +
                    ItemEntry.COLUMN_ITEM_SOLD + " INTEGER DEFAULT 0," +
                    ItemEntry.COLUMN_ITEM_PICTURE + " TEXT NOT NULL," +
                    ItemEntry.COLUMN_ITEM_SUPPLIER + " TEXT NOT NULL," +
                    ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL + " TEXT NOT NULL" +
                    ");";

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Inventory.db";

    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
