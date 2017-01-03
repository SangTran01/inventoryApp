package com.example.sangtran.abproject7;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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

import com.example.sangtran.abproject7.data.ItemContract.ItemEntry;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static com.example.sangtran.abproject7.R.id.item_quantity;
import static com.example.sangtran.abproject7.R.id.item_sold;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private EditText mEditItemName;
    private EditText mEditItemPrice;
    private TextView mTextAmountSold;
    private EditText mEditItemQuantity;
    private TextView mTextItemQuantity;
    private TextView mTextItemSold;
    private EditText mEditItemSupplier;
    private EditText mEditSupplierEmail;
    private Button mBtnAddImage;
    private ImageView mItemImageView;
    private Button mBtnItemOrder;
    private Button mBtnItemAddSale;
    private Button mBtnItemReceiveShipment;


    //global variables to send to email intent
    private String mName;
    private Double mPrice;
    private int mQuantity;
    private int mSold;
    private String mSupplier;
    private String mSupplierEmail;
    //selected image URI coming from Intent MainActivity
    private Uri mSelectedImageUri;

    //REQUEST CODE for image
    private final int PICK_IMAGE_REQUEST = 100;

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
        mTextItemQuantity = (TextView) findViewById(item_quantity);
        mTextItemSold = (TextView) findViewById(item_sold);
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
            mTextItemQuantity.setVisibility(View.GONE);
            mTextAmountSold.setVisibility(View.GONE);
            mTextItemSold.setVisibility(View.GONE);
            mBtnItemAddSale.setVisibility(View.GONE);
            mBtnItemReceiveShipment.setVisibility(View.GONE);
            mBtnItemOrder.setVisibility(View.GONE);
            //Replace textView quantity with Edittext quantity
        } else {
            setTitle("Edit Item");

            //Hide edittext Quantity when updating
            mEditItemQuantity.setVisibility(View.GONE);
            mTextItemQuantity.setVisibility(View.VISIBLE);
            mTextAmountSold.setVisibility(View.VISIBLE);
            mTextItemSold.setVisibility(View.VISIBLE);
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
        mTextItemQuantity.setOnTouchListener(mTouchListener);
        mTextItemSold.setOnTouchListener(mTouchListener);
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

        //button Item Add to Sale
        mBtnItemAddSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSelectedUri != null) {
                    String item_quantity = mTextItemQuantity.getText().toString().trim();
                    if (!TextUtils.isEmpty(item_quantity)) {
                        mQuantity = Integer.parseInt(item_quantity);

                        if (mQuantity > 0) {
                            mQuantity--;
                            mSold++;

                            mTextItemQuantity.setText(String.valueOf(mQuantity));
                            mTextItemSold.setText(String.valueOf(mSold));
                        }
                    }
                    Log.e("DETAIL","String item_quantity is empty " + item_quantity);
                }
            }
        });

        //button Item Receive shipment
        mBtnItemReceiveShipment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSelectedUri != null) {
                    String item_quantity = mTextItemQuantity.getText().toString().trim();
                    if (!TextUtils.isEmpty(item_quantity)) {
                        mQuantity = Integer.parseInt(item_quantity);

                        if (mQuantity >= 0) {
                            mQuantity++;

                            mTextItemQuantity.setText(String.valueOf(mQuantity));
                            mTextItemSold.setText(String.valueOf(mSold));
                        }
                    }
                    Log.e("DETAIL","String item_quantity is empty " + item_quantity);
                }
            }
        });

        //button Item Order more supplies
        mBtnItemOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //used to view price in currency format
                NumberFormat formatter = NumberFormat.getCurrencyInstance();

                String message = "Item Name: " + mName + "\n" +
                        "Item Price: " + formatter.format(mPrice) + "\n" +
                        "Item Quantity: " + mQuantity + "\n" +
                        "Item Amount Sold: " + mSold + "\n" +
                        "Item Picture URI: " + mSelectedImageUri + "\n" +
                        "Item Supplier: " + mSupplier + "\n" +
                        "Item Supplier email: " + mSupplierEmail;

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setData(Uri.parse("mailto:"));
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mSupplierEmail});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Supply Order");
                intent.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(intent);
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
        //depending if adding new or updating
        //item quality will get text from either edittext (new) or textview (updating)
        String item_quantity;
        if (mSelectedUri == null) {
            //New item
            item_quantity = mEditItemQuantity.getText().toString().trim();
        } else {
            //Updating item
            item_quantity = mTextItemQuantity.getText().toString().trim();
        }
        String item_sold = mTextItemSold.getText().toString().trim();
        String item_price = mEditItemPrice.getText().toString().trim();
        String item_supplier = mEditItemSupplier.getText().toString().trim();
        String item_supplier_email = mEditSupplierEmail.getText().toString().trim();


        if (mSelectedUri == null &&
                TextUtils.isEmpty(item_name) &&
                TextUtils.isEmpty(item_quantity) &&
                TextUtils.isEmpty(item_price) &&
                TextUtils.isEmpty(item_supplier) &&
                TextUtils.isEmpty(item_supplier_email) &&
                mSelectedImageUri == null) {

            Toast.makeText(this, "Inputs are empty no changes made", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();


        //Now check if some are empty
        //CHECK ITEM NAME
        if (!TextUtils.isEmpty(item_name)) {
            values.put(ItemEntry.COLUMN_ITEM_NAME, item_name);
        } else {
            Toast.makeText(this, "Name was invalid and did not update",
                    Toast.LENGTH_SHORT).show();
        }

        //CHECK PRICE
        if (!TextUtils.isEmpty(item_price)) {
            double price = Double.parseDouble(item_price);
            values.put(ItemEntry.COLUMN_ITEM_PRICE, price);
        } else {
            Toast.makeText(this, "Price was invalid and did not update",
                    Toast.LENGTH_SHORT).show();
        }

        //CHECK ITEM QUANTITY
        if (!TextUtils.isEmpty(item_quantity)) {
            try {
                int quantity = Integer.parseInt(item_quantity);
                values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Quantity number was invalid and did not update",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Quantity number was invalid and did not update",
                    Toast.LENGTH_SHORT).show();
        }

        //CHECK ITEM SOLD
        if (mSelectedUri != null) {
            if (!TextUtils.isEmpty(item_sold)) {
                try {
                    int sold = Integer.parseInt(item_sold);
                    values.put(ItemEntry.COLUMN_ITEM_SOLD, sold);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Sold number was invalid and did not update",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Sold number was invalid and did not update",
                        Toast.LENGTH_SHORT).show();
            }
        }

        //CHECK ITEM IMAGE
        if (mSelectedImageUri != null) {
            values.put(ItemEntry.COLUMN_ITEM_PICTURE, mSelectedImageUri.toString());
        } else {
            Toast.makeText(this, "Item image was invalid and did not update",
                    Toast.LENGTH_SHORT).show();
        }

        //CHECK ITEM SUPPLIER
        if (!TextUtils.isEmpty(item_supplier)) {
            values.put(ItemEntry.COLUMN_ITEM_SUPPLIER, item_supplier);
        } else {
            Toast.makeText(this, "Supplier was invalid and did not update",
                    Toast.LENGTH_SHORT).show();
        }

        //CHECK ITEM SUPPLIER EMAIL
        if (!TextUtils.isEmpty(item_supplier_email)) {
            values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL, item_supplier_email);
        } else {
            Toast.makeText(this, "Supplier email was invalid and did not update",
                    Toast.LENGTH_SHORT).show();
        }

        // If/else clause to ensure no fields are left empty before the
        // information is sent to the provider
        if (!TextUtils.isEmpty(item_name) && !TextUtils.isEmpty(item_quantity) &&
                !TextUtils.isEmpty(item_price) && !TextUtils.isEmpty(item_supplier) &&
                !TextUtils.isEmpty(item_supplier_email) && mSelectedImageUri != null) {

            if (mSelectedUri == null) {
                Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI,values);

                if (newUri == null) {
                    Toast.makeText(this, "failed to insert into database",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "successfully inserted into database",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                int rowsUpdated = getContentResolver().update(mSelectedUri,values,null,null);

                if (rowsUpdated == 0) {
                    Toast.makeText(this, "failed to update database",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "successfully updated into database",
                            Toast.LENGTH_SHORT).show();
                }
            }

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
            Picasso.with(this).load(mSelectedImageUri).resize(100,100).into(mItemImageView);

            Log.v("DETAILACTIVITY", "SELECTED IMAGE URI " + mSelectedImageUri);
        }
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
            mName = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME));
            mPrice = cursor.getDouble(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE));
            mQuantity = cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY));
            mSold = cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SOLD));
            String imageUri = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PICTURE));
            mSupplier = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER));
            mSupplierEmail = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL));

            // Update the views on the screen with the values from the database
            mEditItemName.setText(mName);
            mEditItemPrice.setText(String.format("%.2f", mPrice));
            mEditItemQuantity.setText(String.valueOf(mQuantity));
            mTextItemQuantity.setText(String.valueOf(mQuantity));
            mTextItemSold.setText(String.valueOf(mSold));

            //setting imageUri to imageView
            mSelectedImageUri = Uri.parse(imageUri);
            Log.v("DETAILACTIVITY ", "URI IMAGE SET" + mSelectedImageUri);
            Picasso.with(this).load(mSelectedImageUri).resize(100, 100).into(mItemImageView);
            mEditItemSupplier.setText(mSupplier);
            mEditSupplierEmail.setText(mSupplierEmail);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mEditItemName.setText("");
        mEditItemPrice.setText("");
        mEditItemQuantity.setText("");
        mTextItemQuantity.setText("");
        mTextItemSold.setText("");
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
