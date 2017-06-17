package com.gururaj.pricetracker;

import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.transition.TransitionManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.gururaj.pricetracker.data.PriceDBContract;
import com.gururaj.pricetracker.data.PriceDBHandler;
import com.gururaj.pricetracker.data.Tuple;
import com.gururaj.pricetracker.utils.PriceTrackerUtils;

import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static com.gururaj.pricetracker.ItemDetailsActivity.EXTRA_DATA_HAS_MORE_THAN_ONE_SHOP;
import static com.gururaj.pricetracker.ItemDetailsActivity.EXTRA_DATA_ITEM_FROM_SHOP;
import static com.gururaj.pricetracker.ItemDetailsActivity.EXTRA_DATA_SHOP_CLICKED;
import static com.gururaj.pricetracker.MainActivity.EXTRA_DATA_ITEM_NAME;
import static com.gururaj.pricetracker.MainActivity.EXTRA_DATA_PRICE_TAG;
import static com.gururaj.pricetracker.MainActivity.EXTRA_DATA_SHOP_NAME;
import static com.gururaj.pricetracker.MainActivity.EXTRA_DATA_TIME;

public class ItemPriceHistoryInShopActivity extends AppCompatActivity implements ItemPriceHistoryAdaptor.ListItemClickListener{

    private String shop;
    private String item;
    private int itemID;
    private boolean mHasMoreThanOneShop;
    private RecyclerView rvPriceHistory;
    private PriceDBHandler mDBHandler;
    private boolean mSnackShown = false;
    private final String LOG_TAG = ItemPriceHistoryInShopActivity.class.getSimpleName();

    private ItemPriceHistoryAdaptor adaptor;
    private int mRemovedPosition;
    private Tuple<String,Float> mRemovedItem;
    private float mDirtyprice = 0;
    private String mDirtyDate = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_price_history_in_shop);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rvPriceHistory = (RecyclerView) findViewById(R.id.rv_item_price_history);



        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        shop = getIntent().getStringExtra(EXTRA_DATA_SHOP_CLICKED);
        item = getIntent().getStringExtra(EXTRA_DATA_ITEM_FROM_SHOP);
        String shopUnQuoted = shop.substring(1,shop.length()-1);
        String title = getResources().getString(R.string.item_price_history,shopUnQuoted);
        getSupportActionBar().setTitle(title);

        mHasMoreThanOneShop = getIntent().getBooleanExtra(EXTRA_DATA_HAS_MORE_THAN_ONE_SHOP,true);

        mDBHandler = PriceDBHandler.getInstance();

        itemID = mDBHandler.getItemID(item);

        ArrayList<Tuple<String,Float>> priceHistoryList = getItemPriceHistory(shop,item);

        adaptor = new ItemPriceHistoryAdaptor(priceHistoryList,this);

        rvPriceHistory.setLayoutManager(layoutManager);
        rvPriceHistory.setAdapter(adaptor);



        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int id = viewHolder.getAdapterPosition();
                final TextView textViewDate = (TextView)viewHolder.itemView.findViewById(R.id.tv_list_item);
                final TextView textViewPrice = (TextView)viewHolder.itemView.findViewById(R.id.tv_list_item_price);
                String dateWithSlash = textViewDate.getText().toString();
                //String dateArray[] = dateWithSlash.split("/");
                final String date = PriceTrackerUtils.parseForDBInsert(dateWithSlash);
                final String price = textViewPrice.getText().toString();

                Snackbar snackbar = Snackbar.make(rvPriceHistory,"Entry Removed",Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                adaptor.getData().add(mRemovedPosition,mRemovedItem);
                                mDirtyprice = 0;
                                mDirtyDate = null;
                                TransitionManager.beginDelayedTransition(rvPriceHistory);
                                adaptor.notifyDataSetChanged();
                            }
                        }).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                super.onDismissed(transientBottomBar, event);
                                    Log.d(LOG_TAG,"Item Swiped : "+ id);

                                if(event != DISMISS_EVENT_CONSECUTIVE )
                                    mSnackShown = false;

                                if(event == DISMISS_EVENT_ACTION)
                                        return;

                                Log.d(LOG_TAG, "onDismissed : Old Price being Removed is :" + price);

                                    if(mDirtyDate != null && mDirtyprice != 0) {

                                        Log.d(LOG_TAG, "onDismissed : Resetting Entry!");
                                        mDBHandler.resetEntry(shop, itemID, date,ItemPriceHistoryInShopActivity.this);

                                        mDBHandler.updateAveragePrice(item, "0",Float.valueOf(price),PriceDBHandler.PRICEDBHELPER_RESET_PRICE);

                                        if(!mSnackShown) {
                                            mDirtyprice = 0;
                                            mDirtyDate = null;
                                        }

                                    }
                            }

                            @Override
                            public void onShown(Snackbar transientBottomBar) {
                                super.onShown(transientBottomBar);
                                mSnackShown = true;
                            }
                        });
                snackbar.show();
                mRemovedPosition = viewHolder.getAdapterPosition();
                mRemovedItem = adaptor.getData().remove(mRemovedPosition);

                mDirtyDate = mRemovedItem.x;
                mDirtyprice = mRemovedItem.y;

                TransitionManager.beginDelayedTransition(rvPriceHistory);
                adaptor.notifyItemRemoved(mRemovedPosition);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
        }
        ).attachToRecyclerView(rvPriceHistory);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if(adaptor.getData().size() == 0 && !mHasMoreThanOneShop) {
                    Intent startMainActivityIntent = new Intent(this,MainActivity.class);
                    startMainActivityIntent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(startMainActivityIntent);
                    return true;
                }
                else {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG,"onPause");
        super.onPause();
        if(mDirtyprice != 0 && mDirtyDate != null) {
            Log.d(LOG_TAG,"onPause : Old Price being Removed is :"+mDirtyprice);

            mDBHandler.resetEntry(shop,itemID,PriceTrackerUtils.parseForDBInsert(mDirtyDate),ItemPriceHistoryInShopActivity.this);
            mDBHandler.updateAveragePrice(item,"0",mDirtyprice,PriceDBHandler.PRICEDBHELPER_RESET_PRICE);
            mDirtyprice = 0;
            mDirtyDate = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(adaptor.getData().size() == 0 && !mHasMoreThanOneShop) {
                Intent startMainActivityIntent = new Intent(this,MainActivity.class);
                startMainActivityIntent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(startMainActivityIntent);
                return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    private ArrayList<Tuple<String,Float>> getItemPriceHistory(String shop, String item) {

        ArrayList<Tuple<String,Float>> priceHistoryList = new ArrayList<Tuple<String,Float>>();

        Cursor cursor =  mDBHandler.getEntries(shop,
                    null,
                    PriceDBContract.PriceDB.COLUMN_ITEM_NAME+"=?",
                    new String[]{String.valueOf(itemID)},
                    null,
                    null,
                    null);

        int colCount = cursor.getColumnCount();

        Log.d(LOG_TAG,"Total entries in shop : "+shop + "for item : "+item + "is "+ colCount);

        if(cursor.moveToFirst()) {
            for (int i = 3; i < colCount; i++) {
                String date = cursor.getColumnName(i);
                float price = cursor.getFloat(i);

                //Log.d(LOG_TAG, "Entry : " + i + " Date : " + date + "Price : " + price);
                if(price != 0) {
                    Tuple<String, Float> tuple = new Tuple<String, Float>(date, price);
                    priceHistoryList.add(tuple);
                }
            }
        }
        return priceHistoryList;
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {

        Tuple<String,Float> data = adaptor.getData().get(clickedItemIndex);

        Intent editItemEntryIntent = new Intent(this,ShowItemEntryActivity.class);

        editItemEntryIntent.putExtra(EXTRA_DATA_ITEM_NAME,item);
        editItemEntryIntent.putExtra(EXTRA_DATA_SHOP_NAME,shop);
        editItemEntryIntent.putExtra(EXTRA_DATA_PRICE_TAG,(Float)data.y);
        editItemEntryIntent.putExtra(EXTRA_DATA_TIME,data.x);


        startActivity(editItemEntryIntent);
    }
}
