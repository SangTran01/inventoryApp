package com.example.sangtran.abproject7.data;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Sang Tran on 2016-12-23.
 */

public class ItemContract {
    public static final String CONTENT_AUTHORITY = "com.example.sangtran.abproject7";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ITEMS = "inventory";


    public static class ItemEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        //Declare MIME types for contentProvider getType()
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        public static final String TABLE_NAME = "inventory";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_QUANTITY = "quantity";
        public static final String COLUMN_ITEM_SOLD = "sold";
        public static final String COLUMN_ITEM_PRICE = "price";
        public static final String COLUMN_ITEM_SUPPLIER = "supplier";
        public static final String COLUMN_ITEM_SUPPLIER_EMAIL = "email";
        public static final String COLUMN_ITEM_PICTURE = "picture";

    }
}
