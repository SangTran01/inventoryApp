package com.example.sangtran.abproject7;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.sangtran.abproject7.data.ItemContract.ItemEntry;

import java.io.ByteArrayOutputStream;

import static android.content.ContentUris.withAppendedId;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int REQ_CODE_PICK_IMAGE = 100;
    private String mItemPrice;

    private ItemCursorAdapter mItemCursorAdapter;

    private static final int URL_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });

        ListView listView = (ListView) findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);


        mItemCursorAdapter = new ItemCursorAdapter(this,null);
        listView.setAdapter(mItemCursorAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(URL_LOADER, null, this);

        //TODO add listview itemclicklistener EVERYTHING BEFORE THIS IS GOOD
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Uri selectedUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);

                intent.setData(selectedUri);
                startActivity(intent);
            }
        });

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //TODO add the rest of the attributes
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_PICTURE};

        return new CursorLoader(this, ItemEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mItemCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mItemCursorAdapter.swapCursor(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case REQ_CODE_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(
                            selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();


                    //Create BitmapUtility object to convert bitmap to byte array for INSTERTING
                    //COnvert back with querying
                    DbBitmapUtility bitmapUtility = new DbBitmapUtility();


                    Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);

                    ContentValues values = new ContentValues();
                    values.put(ItemEntry.COLUMN_ITEM_NAME, "Violin");

                    //convert String to Double then Int * 100 for insert into DB
                    mItemPrice = "199.50";
                    values.put(ItemEntry.COLUMN_ITEM_PRICE, StringToDouble(mItemPrice));
                    values.put(ItemEntry.COLUMN_ITEM_QUANTITY, 10);
                    values.put(ItemEntry.COLUMN_ITEM_PICTURE, bitmapUtility.getBytes(yourSelectedImage));
                    values.put(ItemEntry.COLUMN_ITEM_SUPPLIER, "Sang Tran");
                    values.put(ItemEntry.COLUMN_ITEM_ORDERED, 0);


                    getContentResolver().insert(ItemEntry.CONTENT_URI, values);
                    Toast.makeText(this, "insert", Toast.LENGTH_SHORT).show();
                }
        }
    }

    //Helper method to String to Double then Int * 100 for insert into DB
    public static Double StringToDouble(String s) {
        s = s.replaceAll(",", ""); //remove commas
        Double num = Double.parseDouble(s);
        return num * 100; //return rounded double cast to int
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertData();
                return true;
            case R.id.action_delete_all_entries:
                Toast.makeText(this, "delete all", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void insertData() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);

        onActivityResult(REQ_CODE_PICK_IMAGE, 0, photoPickerIntent);
    }
}
