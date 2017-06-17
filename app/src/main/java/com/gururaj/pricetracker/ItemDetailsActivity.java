package com.gururaj.pricetracker;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.design.widget.AppBarLayout;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gururaj.pricetracker.data.PriceDBContract;
import com.gururaj.pricetracker.data.PriceDBHandler;
import com.gururaj.pricetracker.data.PriceDBHelper;
import com.gururaj.pricetracker.data.Tuple;

import java.util.ArrayList;
import java.util.HashMap;

import static com.gururaj.pricetracker.MainActivity.EXTRA_DATA_IMAGE_RESOURCE_ID;
import static com.gururaj.pricetracker.MainActivity.EXTRA_DATA_ITEM_CLICKED;
import static com.gururaj.pricetracker.MainActivity.LOG_TAG;

public class ItemDetailsActivity extends AppCompatActivity implements ItemDetailsAdaptor.ListItemClickListener/*,AppBarLayout.OnOffsetChangedListener*/{

    String LOG_TAG = ItemDetailsActivity.class.getSimpleName();
    RecyclerView rvItemDetails;
    String item;
    int itemID;
    ItemDetailsAdaptor adaptor;
    PriceDBHandler mDBHandler;
    String mDirtyShop = null;
    private boolean mRefresh = false;
    private boolean mSnackShown = false;
    private ImageView iv_headerImage;
    private Toolbar mToolbar;

    static final String EXTRA_DATA_SHOP_CLICKED = "shopclicked";
    static final String EXTRA_DATA_ITEM_FROM_SHOP = "itemfromshp";
    static final String EXTRA_DATA_HAS_MORE_THAN_ONE_SHOP = "hasmrethanoneshop";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);


        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.d(LOG_TAG,"onCreate");

        mDBHandler = PriceDBHandler.getInstance();

        Intent intent = getIntent();
        item = intent.getStringExtra(EXTRA_DATA_ITEM_CLICKED);
        int imageResId  = intent.getIntExtra(EXTRA_DATA_IMAGE_RESOURCE_ID,-1);
        Log.d(LOG_TAG, "Item Details : " + item);
        iv_headerImage = (ImageView)findViewById(R.id.image_header);
        iv_headerImage.setImageResource(imageResId);
        itemID = mDBHandler.getItemID(item);

        String title = getResources().getString(R.string.item_details,item);
        getSupportActionBar().setTitle(title);

        //AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        //appBarLayout.addOnOffsetChangedListener(this);

        rvItemDetails = (RecyclerView) findViewById(R.id.rv_item_details);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rvItemDetails.setLayoutManager(layoutManager);

        adaptor = new ItemDetailsAdaptor(null,this);

        rvItemDetails.setAdapter(adaptor);

        mRefresh = false;

        GetItemDetailsTask task = new GetItemDetailsTask();
        task.execute();
    }

/*    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset == 0)
        {
            // Collapsed
            getSupportActionBar().setTitle("Shopwise details for Tomato");
            Log.d(LOG_TAG,"Collapsed");
        }
        else
        {
            // Not collapsed
            Log.d(LOG_TAG,"Not Collapsed");
            getSupportActionBar().setTitle("Tomato");
        }
    }*/

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG,"onResume");

        if(mRefresh) {
            GetItemDetailsTask task = new GetItemDetailsTask();
            task.execute();
        }
    }

    private void attachItemTouchHelper() {

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            private Tuple<String,Float> mRemovedItem;
            private int mRemovedPosition;

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {

                final TextView shopTextView = (TextView)viewHolder.itemView.findViewById(R.id.tv_list_item);
                final String shop = shopTextView.getText().toString();
                final String shopQuoted = "\""+shop+"\"";

                Snackbar snackbar = Snackbar.make(rvItemDetails,"Shop removed",Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TransitionManager.beginDelayedTransition(rvItemDetails);
                                adaptor.getData().add(mRemovedPosition,mRemovedItem);
                                mDirtyShop = null;
                                adaptor.notifyDataSetChanged();
                            }
                        }).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                super.onDismissed(transientBottomBar, event);

                                if(event != DISMISS_EVENT_CONSECUTIVE )
                                    mSnackShown = false;

                                if(event == DISMISS_EVENT_ACTION)
                                    return;

                                if(mDirtyShop != null) {
                                    Log.d(LOG_TAG, "Item History of " + item + " being deleted from Shop : " + shopQuoted);
                                    ArrayList<Float> priceList = mDBHandler.getPriceListofItemFromShop(shopQuoted, itemID);

                                    Float[] priceListFloat = priceList.toArray(new Float[priceList.size()]);

                                    mDBHandler.deleteItemFromShop(shopQuoted, itemID,ItemDetailsActivity.this);

                                    mDBHandler.updateAveragePriceAfterDeletion(item, priceListFloat);

                                    if(!mSnackShown)
                                        mDirtyShop = null;
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
                mDirtyShop = shopQuoted;
                TransitionManager.beginDelayedTransition(rvItemDetails);
                adaptor.notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        });

        itemTouchHelper.attachToRecyclerView(rvItemDetails);
    }

    @Override
    public void onListItemClick(int clickedItemIndex, String shop) {
        Log.d(LOG_TAG,"Shop Name Clicked : "+item);
        mRefresh = true;
        Intent itemPriceHistoryIntent = new Intent(this,ItemPriceHistoryInShopActivity.class);

        itemPriceHistoryIntent.putExtra(EXTRA_DATA_SHOP_CLICKED,"\""+shop+"\"");
        itemPriceHistoryIntent.putExtra(EXTRA_DATA_ITEM_FROM_SHOP,item);
        itemPriceHistoryIntent.putExtra(EXTRA_DATA_HAS_MORE_THAN_ONE_SHOP,adaptor.getData().size()>1);
        startActivity(itemPriceHistoryIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mDirtyShop != null) {
            ArrayList<Float> priceList = mDBHandler.getPriceListofItemFromShop(mDirtyShop,itemID);

            Float[] priceListFloat = priceList.toArray(new Float[priceList.size()]);

            Log.d(LOG_TAG,"onPause : Deleting Item : "+item+" from shop : "+mDirtyShop);
            mDBHandler.deleteItemFromShop(mDirtyShop,itemID,this);

            mDBHandler.updateAveragePriceAfterDeletion(item,priceListFloat);
            mDirtyShop = null;
        }
    }

    private class GetItemDetailsTask extends AsyncTask<Void, Void, ArrayList<Tuple<String,Float>>> {

        @Override
        protected void onPostExecute(ArrayList<Tuple<String,Float>> stringIntegerHashMap) {
            super.onPostExecute(stringIntegerHashMap);

            adaptor.setData(stringIntegerHashMap);

            attachItemTouchHelper();

        }

        @Override
        protected ArrayList<Tuple<String,Float>> doInBackground(Void... params) {

            ArrayList<Tuple<String,Float>> shopList = new ArrayList<Tuple<String,Float>>();

            Cursor cursor = mDBHandler.getAllEntries(PriceDBContract.ShopsDB.TABLE_NAME,null);

            Log.v(LOG_TAG,"ItemDetailsActivity : doInBackground starting");

            while(cursor.moveToNext()) {

                String shopName = cursor.getString(cursor.getColumnIndex(PriceDBContract.ShopsDB.COLUMN_SHOP_NAME));
                float priceSum = 0;
                int priceCount = 0;
                Log.d(LOG_TAG, "Shop from DB : " + shopName);

                Cursor cursorForItem = mDBHandler.getEntries(shopName,
                        null,
                        PriceDBContract.PriceDB.COLUMN_ITEM_NAME + "=?",
                        new String[]{String.valueOf(itemID)},
                        null,
                        null,
                        null);

                while (cursorForItem.moveToNext()) {
                    int colCount = cursorForItem.getColumnCount();

                    for (int i = 3; i < colCount; i++) {
                        if (cursorForItem.getInt(i) != 0) {
                            priceCount++;
                            priceSum += cursorForItem.getFloat(i);
                        }
                    }
                }

                cursorForItem.close();

                if (priceCount != 0) {
                    float averagePrice = priceSum / priceCount;
                    Tuple<String,Float> tuple = new Tuple<String,Float>(shopName,averagePrice);
                    shopList.add(tuple);
                    Log.d(LOG_TAG, "Shop Name : " +shopName + "Avg Price : "+averagePrice);
                }
            }

            cursor.close();
            return shopList;
        }
    }
}
