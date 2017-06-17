package com.gururaj.pricetracker;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.IntegerRes;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.transition.TransitionManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.gururaj.pricetracker.async.DBBackgroundTask;
import com.gururaj.pricetracker.data.FakeDataUtil;
import com.gururaj.pricetracker.data.PriceDBContract;
import com.gururaj.pricetracker.data.PriceDBHandler;
import com.gururaj.pricetracker.data.PriceDBHelper;
import com.gururaj.pricetracker.data.Tuple;
import com.gururaj.pricetracker.utils.PriceTrackerUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements ItemListAdaptor.ListItemClickListener,RenameDialogFragment.NoticeDialogListener{
    RecyclerView listRecyclerView;
    LinearLayout layoutItems;
    FloatingActionButton fab;
    TextView noItemsTV;
    ItemListAdaptor adaptor;
    PriceDBHandler mDBHandler;
    boolean mRefresh = false;
    private boolean mSnackShown = false;
    Snackbar snackbar = null;
    private String mDirtyItem = null;

    private String mRenamedItem;
    private int mClickedItemIndex;
    static final String EXTRA_DATA_ITEM_NAME = "itemname";
    static final String EXTRA_DATA_SHOP_NAME = "shopname";
    static final String EXTRA_DATA_PRICE_TAG = "price";
    static final String EXTRA_DATA_TIME = "time";

    static final String EXTRA_DATA_ITEM_CLICKED = "itemclicked";
    static final String EXTRA_DATA_IMAGE_RESOURCE_ID = "imageresourceid";
    static final int REQUEST_GET_ITEM_PRICE_SHOP = 1;
    static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(LOG_TAG,"MainActivity : onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);


        //Get reference to our recyclerview
        listRecyclerView = (RecyclerView)findViewById(R.id.rv_listofitems);

        layoutItems = (LinearLayout)findViewById(R.id.layout_Items);

        noItemsTV = (TextView)findViewById(R.id.tv_no_items);

        fab = (FloatingActionButton)findViewById(R.id.fab_add);

        mDBHandler = PriceDBHandler.getInstance();
        mDBHandler.Init(this);

        //Se the visibility of the list if any item is present in db
        setItemListVisible(mDBHandler.isAnyEntryinTable(PriceDBContract.ItemsDB.TABLE_NAME));


        //Create a layoutmanager for our recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listRecyclerView.setLayoutManager(layoutManager);

        //Insert Some Fake Data
        //FakeDataUtil.InsertFakeData(mDB);

        Cursor cursor = mDBHandler.getAllEntries(PriceDBContract.ItemsDB.TABLE_NAME,
                PriceDBContract.ItemsDB.COLUMN_ITEM_NAME);

        ArrayList<Tuple<String,Float>> list = PriceTrackerUtils.getTupleList(cursor);

        //Create an instance of adaptor for our recyclerview
        adaptor = new ItemListAdaptor(this,list,this,listRecyclerView, PriceTrackerUtils.getDefaultItemsList(this),
                PriceTrackerUtils.getDefaultItemListIconIDs(this));

        listRecyclerView.setAdapter(adaptor);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,AddItemActivity.class);
                //intent.putExtra()
                startActivityForResult(intent,REQUEST_GET_ITEM_PRICE_SHOP);
            }
        });

        attachItemTouchHelper();

    }

    private void attachItemTouchHelper() {

        final ItemTouchHelper itemTouchHelper;
         itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

             private Tuple<String,Float> mRemovedItem;
             private int mPosition;
             private boolean mUndoTapped = false;

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                TextView itemTextView = (TextView)viewHolder.itemView.findViewById(R.id.tv_list_item);
                final String item = itemTextView.getText().toString();

                Log.d(LOG_TAG,"Item : "+item + " is being removed completely from DB!!");

                snackbar = Snackbar.make(listRecyclerView,"Item Removed",
                        Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(LOG_TAG,"snackbar UNDO Pressed!!");
                        mDirtyItem = null;
                        //TransitionManager.beginDelayedTransition(listRecyclerView);
                        adaptor.getData().add(mPosition,mRemovedItem);
                        setItemListVisible(adaptor.getData().size() > 0);
                        adaptor.notifyDataSetChanged();

                    }
                }).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        Log.d(LOG_TAG,"onDismissed , Event : "+event +" Item : "+item);

                        if(event != DISMISS_EVENT_CONSECUTIVE )
                            mSnackShown = false;

                        if(event == DISMISS_EVENT_ACTION)
                            return;

                        if(mDirtyItem != null) {
                            mDBHandler.deleteItemFromAllShops(mDBHandler.getItemID(item), MainActivity.this);
                        }

                        if(!mSnackShown)
                            mDirtyItem = null;

                        setItemListVisible(adaptor.getData().size() > 0);
                    }

                    @Override
                    public void onShown(Snackbar transientBottomBar) {
                        super.onShown(transientBottomBar);
                        mSnackShown = true;
                    }
                });

                Log.d(LOG_TAG, "Showing new Snackbar , adapterposition : "+viewHolder.getAdapterPosition());
                snackbar.show();
                mDirtyItem = item;
                //TransitionManager.beginDelayedTransition(listRecyclerView);
                mPosition = viewHolder.getAdapterPosition();
                mRemovedItem = adaptor.getData().remove(mPosition);
                setItemListVisible(adaptor.getData().size() > 0);
                adaptor.notifyItemRemoved(viewHolder.getAdapterPosition());
            }

        });

        itemTouchHelper.attachToRecyclerView(listRecyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG,"onResume");
        if(mRefresh) {
            Log.d(LOG_TAG,"onResume : Refreshing!");
            boolean isAnyEntryinItemsTable = mDBHandler.isAnyEntryinTable(PriceDBContract.ItemsDB.TABLE_NAME);
            if(isAnyEntryinItemsTable) {
                Cursor cursor = mDBHandler.getAllEntries(PriceDBContract.ItemsDB.TABLE_NAME,
                        PriceDBContract.ItemsDB.COLUMN_ITEM_NAME);
                ArrayList<Tuple<String,Float>> list = PriceTrackerUtils.getTupleList(cursor);
                adaptor.setData(list,true);
            }
            else {
                setItemListVisible(false);
            }
            mRefresh = false;
        }
    }

    public void setItemListVisible(boolean visible) {
        if(visible) {
            layoutItems.setVisibility(View.VISIBLE);
            noItemsTV.setVisibility(View.INVISIBLE);
        }
        else {
            noItemsTV.setVisibility(View.VISIBLE);
            layoutItems.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_CANCELED)
            mRefresh = false;


        Log.d(LOG_TAG,"onActivityResult");

        if(requestCode == REQUEST_GET_ITEM_PRICE_SHOP) {

            if(resultCode == RESULT_OK) {

                String itemName = data.getStringExtra(EXTRA_DATA_ITEM_NAME);
                String shopName = data.getStringExtra(EXTRA_DATA_SHOP_NAME);
                String priceTag = data.getStringExtra(EXTRA_DATA_PRICE_TAG);
                String date = data.getStringExtra(EXTRA_DATA_TIME);

                Log.d(LOG_TAG,"Item : "+data.getStringExtra(EXTRA_DATA_ITEM_NAME));
                Log.d(LOG_TAG,"Shop : "+data.getStringExtra(EXTRA_DATA_SHOP_NAME));
                Log.d(LOG_TAG,"Price : "+data.getStringExtra(EXTRA_DATA_PRICE_TAG));
                Log.d(LOG_TAG,"Date : "+date);

                DBBackgroundTask dbTask = new DBBackgroundTask(this,adaptor);
                dbTask.execute(itemName,shopName,priceTag,date);
            }
        }
    }

    @Override
    protected void onPause() {
        if(mDirtyItem != null) {
            mDBHandler.deleteItemFromAllShops(mDBHandler.getItemID(mDirtyItem),this);
            mDirtyItem = null;
        }
        super.onPause();
    }

    @Override
    public void onListItemClick(int clickedItemIndex,String item,int imageresID) {
        Log.d(LOG_TAG,"Item Clicked : " + clickedItemIndex + " Item : "+item);

        mRefresh = true;

        Intent itemDetailsIntent = new Intent(this,ItemDetailsActivity.class);
        itemDetailsIntent.putExtra(EXTRA_DATA_ITEM_CLICKED,item);
        itemDetailsIntent.putExtra(EXTRA_DATA_IMAGE_RESOURCE_ID,imageresID);
        startActivity(itemDetailsIntent);
    }

    private void showRenamedialg(int clickedItemIndex, String item) {

        mRenamedItem = item;
        mClickedItemIndex = clickedItemIndex;

        RenameDialogFragment fragment = new RenameDialogFragment();
        fragment.show(getSupportFragmentManager(),"rename");

    }

    @Override
    public void onListItemLongClick(final int clickedItemIndex, final String item) {
        showRenamedialg(clickedItemIndex,item);
/*        View popupView = getLayoutInflater().inflate(R.layout.popup_rename,null);

        final AutoCompleteTextView textView = (AutoCompleteTextView)popupView.findViewById(R.id.et_rename);

        Button buttonOk = (Button)popupView.findViewById(R.id.bt_ok);
        Button buttonCancel = (Button)popupView.findViewById(R.id.bt_cancel);

        final PopupWindow popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,true);;

        //String[] defaultItemList = getResources().getStringArray(R.array.item_list_predefined);
*//*        ArrayList<String> itemListFinal = new ArrayList<String>(Arrays.asList(defaultItemList));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.layout_array_adapter,R.id.tv_adaptor,itemListFinal);

        textView.setAdapter(adapter);*//*

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newItemName = textView.getText().toString();

                if(!newItemName.isEmpty() && newItemName.length() != 0) {

                    if(newItemName.equals(item)) {
                        popupWindow.dismiss();
                        return;
                    }
                    else if (isNameAlreadyInItemList(newItemName)) {
                        Toast.makeText(MainActivity.this,"That one already present in List!",Toast.LENGTH_SHORT).show();
                        //popupWindow.dismiss();
                        return;
                    }
                    else {
                        mDBHandler.rename(item, newItemName);
                        renameAdapterListData(item, newItemName);
                        adaptor.notifyItemChanged(clickedItemIndex);
                    }
                }

                popupWindow.dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER,0,0);*/

    }

    private boolean renameAdapterListData(String oldName , String newName) {

        ArrayList<Tuple<String,Float>> list = adaptor.getData();

        for(Tuple<String,Float> listItem : list) {
            if(listItem.x.equals(oldName)) {
                listItem.x = newName;
                return true;
            }
        }

        return false;
    }


    private boolean isNameAlreadyInItemList(String name) {

        ArrayList<Tuple<String,Float>> list = adaptor.getData();

        for(Tuple<String,Float> listItem : list) {
            if(listItem.x.equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return true;
    }

    private void launchAboutActivity() {
        Intent aboutIntent = new Intent(this,AboutActivity.class);
        startActivity(aboutIntent);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.id_about:
                launchAboutActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDBHandler.deInit();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog,String newItemName) {

        if(!newItemName.isEmpty() && newItemName.length() != 0) {

            if(newItemName.equals(mRenamedItem)) {
                dialog.dismiss();
            }
            else if (isNameAlreadyInItemList(newItemName)) {
                Toast.makeText(MainActivity.this,"That one already present in List!",Toast.LENGTH_SHORT).show();
            }
            else {
                mDBHandler.rename(mRenamedItem, newItemName);
                renameAdapterListData(mRenamedItem, newItemName);
                adaptor.notifyItemChanged(mClickedItemIndex);
            }
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }
}
