package com.example.sangtran.abproject7;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sangtran.abproject7.data.ItemContract.ItemEntry;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;

/**
 * Created by Sang Tran on 2016-12-26.
 */

public class ItemCursorAdapter extends CursorAdapter {
    private int mRowsAffected;

    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,viewGroup,false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        final int rowId = cursor.getInt(cursor.getColumnIndex(ItemEntry._ID));

        //find views
        final TextView itemName = (TextView) view.findViewById(R.id.list_item_name);
        final TextView itemPrice = (TextView) view.findViewById(R.id.list_item_price);
        final TextView itemQuantity = (TextView) view.findViewById(R.id.list_item_quantity);
        final TextView itemSold = (TextView) view.findViewById(R.id.list_item_sold);
        final ImageView itemImage = (ImageView) view.findViewById(R.id.list_item_image);
        final Button itemSalesBtn = (Button) view.findViewById(R.id.list_item_sale_btn);


        //find column index and extract values
        String strName = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME));
        double price = cursor.getDouble(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE));
        String imageUri = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PICTURE));


        final int mQuantity = cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY));
        final int mSold = cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SOLD));

        //used to view price in currency format
        NumberFormat formatter = NumberFormat.getCurrencyInstance();


        // Update the TextViews with the attributes for the current pet
        itemName.setText(strName);
        itemPrice.setText(formatter.format(price));
        itemQuantity.setText(String.valueOf(mQuantity));
        itemSold.setText(String.valueOf(mSold));
        Uri uri = Uri.parse(imageUri);

        Picasso.with(context).load(uri).resize(100,100).into(itemImage);

        itemSalesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int rowsAffected = productSale(context, itemSold, itemQuantity, rowId);

                if (rowsAffected != 0) {
                    // update text view if db update is successful
                    itemQuantity.setText(String.valueOf(mQuantity));
                    itemSold.setText(String.valueOf(mSold));
                }
            }
        });
    }

    public int productSale(Context context, TextView itemSold, TextView itemQuantity, int rowId) {
        int soldQuantity = Integer.parseInt(itemSold.getText().toString());
        int itemQ = Integer.parseInt(itemQuantity.getText().toString());

        if (itemQ > 0) {
            itemQ--;
            soldQuantity++;

            int quantity = itemQ;
            int sold = soldQuantity;

            ContentValues values = new ContentValues();
            values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
            values.put(ItemEntry.COLUMN_ITEM_SOLD, sold);

            Uri currentProductUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI,
                    rowId);

            String selection = ItemEntry._ID + "=?";
            String[] selectionArgs = new String[] {String.valueOf(ContentUris.parseId(currentProductUri))};
            mRowsAffected = context.getContentResolver().update(currentProductUri, values,
                    selection,selectionArgs);
        }
        return mRowsAffected;
    }
}
