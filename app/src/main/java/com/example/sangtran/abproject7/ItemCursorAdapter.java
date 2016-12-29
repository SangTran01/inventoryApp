package com.example.sangtran.abproject7;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sangtran.abproject7.data.ItemContract;
import com.example.sangtran.abproject7.data.ItemContract.ItemEntry;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;

/**
 * Created by Sang Tran on 2016-12-26.
 */

public class ItemCursorAdapter extends CursorAdapter {


    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,viewGroup,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //TODO ADD rest of the attributes
        //find views
        TextView itemName = (TextView) view.findViewById(R.id.list_item_name);
        TextView itemPrice = (TextView) view.findViewById(R.id.list_item_price);
        TextView itemQuantity = (TextView) view.findViewById(R.id.list_item_quantity);
        TextView itemSales = (TextView) view.findViewById(R.id.list_item_sales);
        ImageView itemImage = (ImageView) view.findViewById(R.id.list_item_image);
        Button itemSalesBtn = (Button) view.findViewById(R.id.list_item_salesBtn);

        String strName = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME));
        double price = cursor.getDouble(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE));
        int quantity = cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY));
        byte[] image = cursor.getBlob(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PICTURE));
        NumberFormat formatter = NumberFormat.getCurrencyInstance();

        itemName.setText(strName);
        itemPrice.setText(formatter.format(price / 100));
        itemQuantity.setText(String.valueOf(quantity));
        itemImage.setImageBitmap(new DbBitmapUtility().getImage(image));
    }

    //Helper method to convert bitmap image to byte[] and back
    public class DbBitmapUtility {

        // convert from bitmap to byte array
        public byte[] getBytes(Bitmap bitmap) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            return stream.toByteArray();
        }

        // convert from byte array to bitmap
        public Bitmap getImage(byte[] image) {
            return BitmapFactory.decodeByteArray(image, 0, image.length);
        }
    }
}
