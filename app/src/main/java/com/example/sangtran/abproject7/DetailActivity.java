package com.example.sangtran.abproject7;

import android.app.LoaderManager;
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
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sangtran.abproject7.data.ItemContract.ItemEntry;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;

import static android.R.attr.name;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private EditText mEditItemName;
    private EditText mEditItemPrice;
    private EditText mEditItemQuantity;
    private EditText mEditItemSupplier;
    private Button mBtnAddImage;
    private ImageView mItemImageView;
    private Button mBtnItemOrder;

    //current Text for edittext price
    private String current;

    private Bitmap mSelectedImage;

    //selected URI coming from Intent MainActivity
    private Uri mSelectedUri;

    //number to store into db for order
    private int hasOrderednum = 0;

    private static final int URL_LOADER = 0;

    private static final int REQ_CODE_PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mSelectedUri = intent.getData();

        if (mSelectedUri == null) {
            setTitle("Add Item");
        } else {
            setTitle("Edit Item");
            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(URL_LOADER, null, this);
        }


        //findById all views
        mEditItemPrice = (EditText) findViewById(R.id.edit_item_price);
        mEditItemQuantity = (EditText) findViewById(R.id.edit_item_quantity);
        mEditItemSupplier = (EditText) findViewById(R.id.edit_item_supplier);
        mBtnAddImage = (Button) findViewById(R.id.btn_item_image);
        mItemImageView = (ImageView) findViewById(R.id.img_item_painting);
        mBtnItemOrder = (Button) findViewById(R.id.btn_item_order);
        mEditItemName = (EditText) findViewById(R.id.edit_item_painter);


        mBtnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addImage();
            }
        });

        mBtnItemOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hasOrderednum = 1;
                Toast.makeText(getApplicationContext(), "Item Ordered", Toast.LENGTH_SHORT).show();
            }
        });

        mEditItemPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (!s.toString().equals(current)) {
                    mEditItemPrice.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[$,.]", "");

                    double parsed = Double.parseDouble(cleanString);
                    String formatted = NumberFormat.getCurrencyInstance().format((parsed / 100));

                    current = formatted;
                    mEditItemPrice.setText(formatted);
                    mEditItemPrice.setSelection(formatted.length());

                    mEditItemPrice.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
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

                    mSelectedImage = BitmapFactory.decodeFile(filePath);

                    //set image to ImageView
                    mItemImageView.setImageBitmap(mSelectedImage);
                    Toast.makeText(this, "picture added to view", Toast.LENGTH_SHORT).show();
                }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_item:
                //SAVE ITEM
                saveItem();

                //closes the EditorActivty and returns to CatalogActivity
                finish();
                return true;
            case R.id.action_delete_item:
                Toast.makeText(this, "delete all", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveItem() {
        //get values from views
        String item_name = mEditItemName.getText().toString().trim();
        String item_quantity = mEditItemQuantity.getText().toString().trim();
        String item_price = mEditItemPrice.getText().toString().trim();
        String item_supplier = mEditItemSupplier.getText().toString().trim();


        //If weight not provided, dont try to parse string into int. Use 0 default
        int quantity = 0;
        if (!TextUtils.isEmpty(item_quantity)) {
            quantity = Integer.parseInt(item_quantity);
        }

        //remove $ sign from price
        String remove_sign_price = item_price.replace("$", "");

        //convert bitmap to bytes[]
        DbBitmapUtility dbBitmapUtility = new DbBitmapUtility();
        byte[] byteImage = dbBitmapUtility.getBytes(mSelectedImage);

        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, item_name);
        //convert String to Double then Int * 100 for insert into DB
        values.put(ItemEntry.COLUMN_ITEM_PRICE, StringToDouble(remove_sign_price));
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
        values.put(ItemEntry.COLUMN_ITEM_PICTURE, byteImage);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER, item_supplier);
        values.put(ItemEntry.COLUMN_ITEM_ORDERED, hasOrderednum);

        getContentResolver().insert(ItemEntry.CONTENT_URI, values);
        Toast.makeText(this, "inserted into database", Toast.LENGTH_SHORT).show();
    }


    public void addImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);

        onActivityResult(REQ_CODE_PICK_IMAGE, 0, photoPickerIntent);
    }


    //Helper method to String to Double then Int * 100 for insert into DB
    public static Double StringToDouble(String s) {
        s = s.replaceAll(",", ""); //remove commas
        Double num = Double.parseDouble(s);
        return num * 100; //return rounded double cast to int
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

        return new CursorLoader(this,
                ItemEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME));
            double price = cursor.getDouble(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE));
            int quantity = cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY));
            byte[] image = cursor.getBlob(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PICTURE));
            NumberFormat formatter = NumberFormat.getCurrencyInstance();

            // Update the views on the screen with the values from the database
            mEditItemName.setText(name);
            mEditItemPrice.setText(formatter.format(price / 100));
            mEditItemQuantity.setText(String.valueOf(quantity));
            mItemImageView.setImageBitmap(new DbBitmapUtility().getImage(image));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mEditItemName.setText("");
        mEditItemPrice.setText("");
        mEditItemQuantity.setText("");
        mItemImageView.setImageResource(0);
    }

    //Helper method to convert bitmap image to byte[] and back
    public class DbBitmapUtility {

        // convert bitmap to byte array
        public byte[] getBytes(Bitmap bitmap) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            return stream.toByteArray();
        }

        // convert byte array to bitmap
        public Bitmap getImage(byte[] image) {
            return BitmapFactory.decodeByteArray(image, 0, image.length);
        }
    }
}
