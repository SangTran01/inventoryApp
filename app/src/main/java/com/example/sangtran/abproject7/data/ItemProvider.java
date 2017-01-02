package com.example.sangtran.abproject7.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.sangtran.abproject7.data.ItemContract.ItemEntry;

/**
 * Created by Sang Tran on 2016-12-25.
 */

public class ItemProvider extends ContentProvider {
    private static final String LOG_TAG = ItemProvider.class.getSimpleName();

    //matcher code for inventory table
    private static final int ITEMS = 100;

    //matcher code for single item in inventory table
    private static final int ITEM_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS, ITEMS);

        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    ItemDbHelper mItemDbHelper;

    //Initializes the provider and the database helper object.
    @Override
    public boolean onCreate() {
        mItemDbHelper = new ItemDbHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mItemDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match) {
            case ITEMS:
                cursor = db.query(ItemEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case ITEM_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = db.query(ItemEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }


    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ITEMS:
                return insertItem(uri,contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {
        sanityCheck(values);
        SQLiteDatabase db = mItemDbHelper.getWritableDatabase();
        long newRowId = db.insert(ItemEntry.TABLE_NAME,null,values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (newRowId == -1) {
            return null;
        }

        //notify all listeners that the data has changed for the item content URI
        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri,newRowId);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, contentValues,selection,selectionArgs);
            case ITEM_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateItem(uri, contentValues,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        sanityCheck(values);
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = mItemDbHelper.getWritableDatabase();
        int updatedRows = db.update(ItemEntry.TABLE_NAME,values,selection,selectionArgs);

        //notify all listeners that the data has changed for the item content URI
        getContext().getContentResolver().notifyChange(uri,null);

        return updatedRows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mItemDbHelper.getWritableDatabase();
        int deletedRows;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                deletedRows = db.delete(ItemEntry.TABLE_NAME,selection,selectionArgs);

                //notify all listeners that the data has changed for the item content URI
                getContext().getContentResolver().notifyChange(uri,null);

                return deletedRows;
            case ITEM_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                deletedRows = db.delete(ItemEntry.TABLE_NAME,selection,selectionArgs);

                //notify all listeners that the data has changed for the item content URI
                getContext().getContentResolver().notifyChange(uri,null);

                return deletedRows;
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
    }



    //SanityCheck helper method catches null inputs
    public void sanityCheck(ContentValues values) {
        // check that the name value is not null.
        if (values.containsKey(ItemEntry.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(ItemEntry.COLUMN_ITEM_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }

        // check that the price value is valid.
        if (values.containsKey(ItemEntry.COLUMN_ITEM_PRICE)) {
            // Check that the weight is greater than or equal to 0 kg
            Double price = values.getAsDouble(ItemEntry.COLUMN_ITEM_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Item requires valid price");
            }
        }

        // check that the quantity value is valid.
        if (values.containsKey(ItemEntry.COLUMN_ITEM_QUANTITY)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer quantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Item requires valid quantity");
            }
        }

        // check that the image value is not null.
        if (values.containsKey(ItemEntry.COLUMN_ITEM_PICTURE)) {
            String image = values.getAsString(ItemEntry.COLUMN_ITEM_PICTURE);
            if (image == null) {
                throw new IllegalArgumentException("Item requires a picture");
            }
        }

        //check that the supplier is not null
        if (values.containsKey(ItemEntry.COLUMN_ITEM_SUPPLIER)) {
            String supplier = values.getAsString(ItemEntry.COLUMN_ITEM_SUPPLIER);
            if (supplier == null) {
                throw new IllegalArgumentException("Item requires a supplier");
            }
        }

        //check number is not null
        if (values.containsKey(ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL)) {
            String email = values.getAsString(ItemEntry.COLUMN_ITEM_SUPPLIER);
            if (email == null) {
                throw new IllegalArgumentException("Item requires a supplier email");
            }
        }
    }


}
