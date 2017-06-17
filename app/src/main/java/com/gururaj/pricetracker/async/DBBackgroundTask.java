package com.gururaj.pricetracker.async;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.gururaj.pricetracker.ItemListAdaptor;
import com.gururaj.pricetracker.MainActivity;
import com.gururaj.pricetracker.data.PriceDBContract;
import com.gururaj.pricetracker.data.PriceDBHandler;
import com.gururaj.pricetracker.data.PriceDBHelper;
import com.gururaj.pricetracker.utils.PriceTrackerUtils;

/**
 * Created by Gururaj on 5/6/2017.
 */

public class DBBackgroundTask extends AsyncTask<String,Void,Void> {

    private Context mContext;
    private ItemListAdaptor mAdpator;
    private PriceDBHandler mDBHandler;
    private static final String LOG_TAG = DBBackgroundTask.class.getSimpleName();

    public DBBackgroundTask(Context context, ItemListAdaptor adaptor) {
        mContext = context;
        mAdpator = adaptor;
    }

    @Override
    protected Void doInBackground(String... params) {

        String itemName = params[0];
        String shopName = params[1];
        String priceTag = params[2];
        String date = params[3];

        Log.d(LOG_TAG,"Item : "+itemName);
        Log.d(LOG_TAG,"Shop : "+shopName);
        Log.d(LOG_TAG,"Price : "+priceTag);
        Log.d(LOG_TAG,"Date : "+date);

        float oldPrice = 0;
        boolean dateExistsForItem;
        boolean isItemNew = mDBHandler.isNewItem(itemName);

        //Add the item if it is not in "Items" table
        if(isItemNew) {
            Log.v(LOG_TAG,"Item : "+itemName+" is new adding it to items table");
            mDBHandler.addItemToDB(itemName,priceTag);
        }

        int itemID = mDBHandler.getItemID(itemName);

        //Create the table if it is a new shop
        boolean isNew = mDBHandler.isNewShop(shopName);
        if(isNew) {
            //Add new table for corresponding to shop name
            Log.v(LOG_TAG,"It is a new Shop : "+shopName + " Adding to Shops DB!");
            mDBHandler.addShopNameToDB(shopName,mContext);
            dateExistsForItem = false;
        }
        else
            dateExistsForItem = mDBHandler.isColumnExists(shopName,date);

        if(dateExistsForItem)
            oldPrice = mDBHandler.getFloatValue(shopName,itemID,date);

        //boolean discardOldPrice = dateExistsForItem && !isNew && oldPrice != 0;

        //Add the item if it is not in "Items" table
        String mode = null;
        if(!isItemNew) {
            Log.v(LOG_TAG,"Item : "+itemName+" is existing one");


            if(dateExistsForItem && oldPrice != 0)
                mode = PriceDBHandler.PRICEDBHELPER_UPDATE_PRICE;
            else
                mode = PriceDBHandler.PRICEDBHELPER_ADD_PRICE;

            Log.v(LOG_TAG,"Calling updateAveragePrice in mode : "+mode);

            mDBHandler.updateAveragePrice(itemName,priceTag,oldPrice,mode);
        }

        Log.d(LOG_TAG,"Date Exists already ? : "+dateExistsForItem + " Oldprice : "+oldPrice);

        //Add the new entry for item in the table
        mDBHandler.addItemSample(shopName,itemID,priceTag,date,dateExistsForItem,mode,mContext);

        return null;
    }

    @Override
    protected void onPreExecute() {
        mDBHandler = PriceDBHandler.getInstance();
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        //Log.d(LOG_TAG,"Number of items "+cursor.getCount());

        if(mAdpator != null) {
            Cursor cursor = mDBHandler.getAllEntries(PriceDBContract.ItemsDB.TABLE_NAME,null);
            mAdpator.setData(PriceTrackerUtils.getTupleList(cursor), true);

            MainActivity activity = (MainActivity) mContext;
            activity.setItemListVisible(true);
        }
    }

}
