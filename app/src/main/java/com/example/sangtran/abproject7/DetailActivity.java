package com.example.sangtran.abproject7;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sangtran.abproject7.data.ItemContract;
import com.example.sangtran.abproject7.data.ItemContract.ItemEntry;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private EditText mEditItemName;
    private EditText mEditItemPrice;
    private TextView mTextAmountSold;
    private EditText mEditItemQuantity;
    private TextView mItemQuantity;
    private TextView mItemSold;
    private EditText mEditItemSupplier;
    private EditText mEditSupplierEmail;
    private Button mBtnAddImage;
    private ImageView mItemImageView;
    private Button mBtnItemOrder;
    private Button mBtnItemAddSale;
    private Button mBtnItemReceiveShipment;

    private int mQuantity;
    private int mSold;
    private String mSupplierEmail;

    //current Text for edittext price
    private String current;

    //REQUEST CODE for image
    private final int PICK_IMAGE_REQUEST = 100;

    //selected image URI coming from Intent MainActivity
    private Uri mSelectedImageUri;

    //selected URI for item
    private Uri mSelectedUri;

    //Contstant URL LOADER
    private static final int URL_LOADER = 0;

    //check if item has changed
    private boolean mItemHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mSelectedUri = intent.getData();


        //findById all views
        mTextAmountSold = (TextView) findViewById(R.id.text_amount_sold);
        mEditItemQuantity = (EditText) findViewById(R.id.edit_item_quantity);
        mEditItemPrice = (EditText) findViewById(R.id.edit_item_price);
        mItemQuantity = (TextView) findViewById(R.id.item_quantity);
        mItemSold = (TextView) findViewById(R.id.item_sold);
        mEditItemSupplier = (EditText) findViewById(R.id.edit_item_supplier);
        mEditSupplierEmail = (EditText) findViewById(R.id.edit_item_supplier_email);
        mItemImageView = (ImageView) findViewById(R.id.img_item_painting);
        mEditItemName = (EditText) findViewById(R.id.edit_item_painter);
        mBtnAddImage = (Button) findViewById(R.id.btn_item_image);
        mBtnItemAddSale = (Button) findViewById(R.id.btn_item_add_sale);
        mBtnItemReceiveShipment = (Button) findViewById(R.id.btn_item_receive_shipment);
        mBtnItemOrder = (Button) findViewById(R.id.btn_item_order);


        if (mSelectedUri == null) {
            setTitle("Add Item");
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a item that hasn't been created yet.)
            invalidateOptionsMenu();
            //When adding New item
            //Show editText quantity
            //Hide sold label, and quantity and sold values
            //Hide buttons add sale and receive shipment
            mEditItemQuantity.setVisibility(View.VISIBLE);
            mItemQuantity.setVisibility(View.GONE);
            mTextAmountSold.setVisibility(View.GONE);
            mItemSold.setVisibility(View.GONE);
            mBtnItemAddSale.setVisibility(View.GONE);
            mBtnItemReceiveShipment.setVisibility(View.GONE);
            mBtnItemOrder.setVisibility(View.GONE);
            //Replace textView quantity with Edittext quantity
        } else {
            setTitle("Edit Item");

            //Hide edittext Quantity when updating
            mEditItemQuantity.setVisibility(View.GONE);
            mItemQuantity.setVisibility(View.VISIBLE);
            mTextAmountSold.setVisibility(View.VISIBLE);
            mItemSold.setVisibility(View.VISIBLE);
            mBtnItemAddSale.setVisibility(View.VISIBLE);
            mBtnItemReceiveShipment.setVisibility(View.VISIBLE);
            mBtnItemOrder.setVisibility(View.VISIBLE);
            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(URL_LOADER, null, this);
        }

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mEditItemPrice.setOnTouchListener(mTouchListener);
        mEditItemQuantity.setOnTouchListener(mTouchListener);
        mItemQuantity.setOnTouchListener(mTouchListener);
        mItemSold.setOnTouchListener(mTouchListener);
        mEditItemSupplier.setOnTouchListener(mTouchListener);
        mEditSupplierEmail.setOnTouchListener(mTouchListener);
        mBtnAddImage.setOnTouchListener(mTouchListener);
        mItemImageView.setOnTouchListener(mTouchListener);
        mBtnItemOrder.setOnTouchListener(mTouchListener);
        mBtnItemReceiveShipment.setOnTouchListener(mTouchListener);
        mEditItemName.setOnTouchListener(mTouchListener);

        //button Add Image
        mBtnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addImage();
            }
        });

        //button Item Order more supplies
        mBtnItemOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setData(Uri.parse("mailto:"));
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL  , new String[] {mSupplierEmail});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Supply Order");
                intent.putExtra(Intent.EXTRA_TEXT   , "Message Body");
                startActivity(intent);
            }
        });

        //button Item Add to Sale
        mBtnItemAddSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSelectedUri != null) {

                    if (mQuantity > 0) {
                        mQuantity--;
                        mSold++;

                        mItemQuantity.setText(String.valueOf(mQuantity));
                        mItemSold.setText(String.valueOf(mSold));
                    }
                }
            }
        });

        //button Item Receive shipment
        mBtnItemReceiveShipment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSelectedUri != null) {
                    mQuantity++;
                    mItemQuantity.setText(String.valueOf(mQuantity));
                }
            }
        });

        //add textWatcher for price edittext input format
        mEditItemPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (!s.toString().equals(current)) {
                    mEditItemPrice.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[$,.]", "");

                    double parsed;
                    if (cleanString == "" || cleanString == null) {
                        parsed = 0;
                    } else {
                        parsed = Double.parseDouble(cleanString);
                    }

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
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    //THis method is called after invalidateOptionsMenu(), so that
    //the menu can be updated (some menu items can be hidden or invisible)
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mSelectedUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_item);
            menuItem.setVisible(false);
        }
        return true;
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
                showDeleteConfirmationDialog();
                return true;
            //Option Up button
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mSelectedUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the item that we want.
            String selection = ItemEntry._ID + "=?";
            String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mSelectedUri))};
            int rowsDeleted = getContentResolver().delete(mSelectedUri, selection, selectionArgs);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        //closes the EditorActivty and returns to CatalogActivity
        finish();
    }

    public void saveItem() {
        //get values from views
        String item_name = mEditItemName.getText().toString().trim();
        String item_quantity = mItemQuantity.getText().toString().trim();
        String item_sold = mItemSold.getText().toString().trim();
        String item_price = mEditItemPrice.getText().toString().trim();
        String item_supplier = mEditItemSupplier.getText().toString().trim();
        String item_supplier_email = mEditSupplierEmail.getText().toString().trim();

        if (mSelectedUri == null &&
                TextUtils.isEmpty(item_name) && TextUtils.isEmpty(item_quantity) &&
                TextUtils.isEmpty(item_sold) && TextUtils.isEmpty(item_price) &&
                TextUtils.isEmpty(item_supplier) && TextUtils.isEmpty(item_supplier_email) &&
                mItemImageView.getDrawable() == null) {
            return;
        }

        //if user doesn't add image when creating new Item
        //replace with error icon
        if (mSelectedImageUri == null) {
            mSelectedImageUri = Uri.parse("android.resource://" + ItemContract.CONTENT_AUTHORITY +
                    "/" + R.drawable.ic_no_image);
        }

        //If quantity not provided, dont try to parse string into int. Use 0 default
        int quantity = 0;
        if (!TextUtils.isEmpty(item_quantity)) {
            quantity = Integer.parseInt(item_quantity);
        }

        //remove $ sign from price
        String remove_sign_price = item_price.replace("$", "");

        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, item_name);
        //convert String to Double then Int * 100 for insert into DB
        values.put(ItemEntry.COLUMN_ITEM_PRICE, StringToDouble(remove_sign_price));
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
        values.put(ItemEntry.COLUMN_ITEM_SOLD,item_sold);
        values.put(ItemEntry.COLUMN_ITEM_PICTURE, mSelectedImageUri.toString());
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER, item_supplier);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL, item_supplier_email);

        //if mSelectedUri is not null
        //insert NEW item
        //else
        //update item
        if (mSelectedUri == null) {
            getContentResolver().insert(ItemEntry.CONTENT_URI, values);
            Toast.makeText(this, "successfully inserted into database", Toast.LENGTH_SHORT).show();
        } else {
            //Use selection and selectionArgs to get current Item to update
            String selection = ItemEntry._ID + "=?";
            String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mSelectedUri))};
            getContentResolver().update(mSelectedUri, values, selection, selectionArgs);
            Toast.makeText(this, "successfully updated into database", Toast.LENGTH_SHORT).show();
        }


    }


    //Helper method to add image to view
    public void addImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICK_IMAGE_REQUEST);

        onActivityResult(PICK_IMAGE_REQUEST, 0, photoPickerIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            //store imgUri
            mSelectedImageUri = data.getData();
            mItemImageView.setImageURI(mSelectedImageUri);

            Log.v("DETAILACTIVITY", "SELECTED IMAGE URI " + mSelectedImageUri);
        }
    }


    //Helper method for price variable
    //to String to Double then Int * 100 for insert into DB
    public static Double StringToDouble(String s) {
        s = s.replaceAll(",", ""); //remove commas
        double parsed;
        if (s == null || s == "") {
            parsed = 0;
        } else {
            parsed = Double.parseDouble(s);
        }
        return parsed * 100; //return rounded double cast to int
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_SOLD,
                ItemEntry.COLUMN_ITEM_PICTURE,
                ItemEntry.COLUMN_ITEM_SUPPLIER,
                ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL};

        return new CursorLoader(this,
                mSelectedUri,
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
            mQuantity = cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY));
            mSold = cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SOLD));
            String imageUri = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PICTURE));
            String supplier = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER));
            mSupplierEmail = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL));
            NumberFormat formatter = NumberFormat.getCurrencyInstance();

            // Update the views on the screen with the values from the database
            mEditItemName.setText(name);
            mEditItemPrice.setText(formatter.format(price / 100));
            mItemQuantity.setText(String.valueOf(mQuantity));
            mEditItemQuantity.setText(String.valueOf(mQuantity));
            mItemSold.setText(String.valueOf(mSold));

            //setting imageUri to imageView
            mSelectedImageUri = Uri.parse(imageUri);
            Log.v("DETAILACTIVITY ", "URI IMAGE SET" + mSelectedImageUri);
            Picasso.with(this).load(mSelectedImageUri).resize(100, 100).into(mItemImageView);
            mEditItemSupplier.setText(supplier);
            mEditSupplierEmail.setText(mSupplierEmail);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mEditItemName.setText("");
        mEditItemPrice.setText("");
        mEditItemQuantity.setText("");
        mItemQuantity.setText("");
        mItemSold.setText("");
        mItemImageView.setImageResource(0);
        mEditItemSupplier.setText("");
        mEditSupplierEmail.setText("");
    }

    //Show dialog message when backing out during editing
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
