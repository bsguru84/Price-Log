package com.gururaj.pricetracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.gururaj.pricetracker.utils.PriceTrackerUtils;

import java.util.ArrayList;

/**
 * Created by Gururaj on 5/13/2017.
 */

public class PriceDBHandler {

    private static PriceDBHandler mDBHandler = null;
    private SQLiteDatabase mDB = null;
    private PriceDBHelper mDBHelper;
    private final String LOG_TAG = PriceDBHandler.class.getSimpleName();

    public static final String PRICEDBHELPER_UPDATE_PRICE = "update";
    public static final String PRICEDBHELPER_ADD_PRICE = "add";
    public static final String PRICEDBHELPER_RESET_PRICE = "reset";
    //private Context mContext;



    private PriceDBHandler(){}

    /**
     * API to get Instance of PriceDBHandler
     * @return
     */
    public static PriceDBHandler getInstance() {
        if(mDBHandler == null) {
            mDBHandler = new PriceDBHandler();
        }
        return mDBHandler;
    }

    /**
     * Method to init the instance of PriceDBHandler
     * @param context
     */
    public void Init(Context context) {
        Context mContext;
        mContext = context;
        if(mDBHelper == null) {
            mDBHelper = new PriceDBHelper(mContext);
            mDB = mDBHelper.getWritableDatabase();
        }
    }

    public void deInit() {
        if(mDBHelper != null) {

            if(mDB != null) {
                mDB.close();
                mDB = null;
            }
            mDBHelper = null;
            mDBHandler = null;
        }
    }

    /**
     * Method to check for the presense of any entry in the table
     * @param table
     * @return
     */
    public  boolean isAnyEntryinTable(String table) {

        if(table == null)
            return false;
        Cursor cursor = mDB.query(table,
                null,
                null,
                null,
                null,
                null,
                null);

        boolean isAnyEntry = cursor.getCount() > 0;
        cursor.close();

        return isAnyEntry;
    }

    public  Cursor getAllEntries(String table,String sortby) {

        return mDB.query(table,
                null,
                null,
                null,
                null,
                null,
                sortby);
    }

    /**
     * Utility to check if the shop name is new
     * @param shopName
     * @return
     */
    public  boolean isNewShop(String shopName) {

        Cursor cursor = mDB.query(PriceDBContract.ShopsDB.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                PriceDBContract.ShopsDB.COLUMN_SHOP_NAME);
        int count = cursor.getCount();

        //No Shops present in DB
        if(count == 0) {
            Log.d(LOG_TAG,"Shop : "+shopName+" not found in DB!!!");
            return true;
        }

        cursor.moveToFirst();

        for(int i = 0;i < count;i++) {
            String shopinDB = cursor.getString(cursor.getColumnIndex(PriceDBContract.ShopsDB.COLUMN_SHOP_NAME));

            Log.d(LOG_TAG,"Shop " + i + " from DB "+shopinDB);

            if(shopinDB.equals(shopName)) {
                cursor.close();
                Log.d(LOG_TAG,"Shop : "+shopName+" found in DB!!!");
                return false;
            }
            cursor.moveToNext();
        }

        Log.d(LOG_TAG,"Shop : "+shopName+" not found in DB!!!");
        return true;
    }

    /**
     * Utility to add shopname to DB
     * @param shopName
     * @param context
     */

    public  void addShopNameToDB(String shopName,Context context) {

/*        int currentVersion = mDB.getVersion();
        mDB.close();

        PriceDBHelper dbHelper = new PriceDBHelper(context,currentVersion+1,PriceDBHelper.ACTION_ADD_NEW_TABLE,shopName,null);
        mDB = dbHelper.getWritableDatabase();*/
        String query = "CREATE TABLE " + shopName + " (" +
                PriceDBContract.PriceDB._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PriceDBContract.PriceDB.COLUMN_ITEM_NAME + " INTEGER,"+
                PriceDBContract.PriceDB.COLUMN_ITEM_PRICE_ENTRY_COUNT + " INTEGER)";

        try {
            mDB.beginTransaction();
            SQLiteStatement statement = mDB.compileStatement(query);
            statement.execute();
            mDB.setTransactionSuccessful();
        }
        finally {
            mDB.endTransaction();
        }


        ContentValues cv = new ContentValues();
        cv.put(PriceDBContract.ShopsDB.COLUMN_SHOP_NAME,shopName);

        try {
            mDB.beginTransaction();

            mDB.insert(PriceDBContract.ShopsDB.TABLE_NAME,null,cv);

            mDB.setTransactionSuccessful();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            mDB.endTransaction();
        }

    }

    /**
     * Utility to check existence of a column in table
     * @param table
     * @param column
     * @return
     */
    public  boolean isColumnExists(String table,String column) {

/*        Cursor cursor = mDB.query(table,
                null,
                null,null,
                null,null,null);

        Log.v(LOG_TAG,"isColumnExists : column : "+column+" Table : "+table);

        if(cursor != null) {
            int index = cursor.getColumnIndex(column);

            if(index != -1) {
                cursor.close();
                return true;
            }
            else {
                cursor.close();
                Log.v(LOG_TAG,"isColumnExists : CP 1");
                return false;
            }
        }
        else {
            Log.v(LOG_TAG,"isColumnExists : CP 2");
            return false;
        }*/


        Cursor cursor = mDB.query(table,null,
                null,null,null,null,null);
        column = PriceTrackerUtils.deParseFromDBQuery(column);
        int index = cursor.getColumnIndex(column);

        if(index != -1)
            return true;
        else
            return false;

/*        int colIndex = 0;
        int colCount = cursor.getColumnCount();
        cursor.moveToFirst();

        Log.d(LOG_TAG,"isColumnExists : colCount : "+colCount);
        while(colCount-- != 0) {
            Log.d(LOG_TAG,"isColumnExists ; Price Sample  : "+cursor.getFloat(colIndex)+"column : "+cursor.getColumnName(colIndex));

            if(column.equals(cursor.getColumnName(colIndex)))
                return true;

            colIndex++;
        }
        return false;*/

/*        column = PriceTrackerUtils.deParseFromDBQuery(column);
        Log.v(LOG_TAG,"isColumnExists : column : "+column+" Table : "+table);
        boolean isExist = true;
        Cursor res = mDB.rawQuery("PRAGMA table_info("+table+")",null);
        int value = res.getColumnIndex(column);

        if(value == -1)
        {
            isExist = false;
        }
        res.close();
        return isExist;*/

    }


    public  float getFloatValue(String table,int itemID,String column) {

        float floatValue = 0;
        Cursor cursor = mDB.query(table,null,
                PriceDBContract.PriceDB.COLUMN_ITEM_NAME+"=?",
                new String[]{String.valueOf(itemID)},
                null,
                null,
                null);

        if(cursor != null) {
            if(cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(column);

                if(index != -1) {
                    floatValue = cursor.getFloat(index);
                    Log.d(LOG_TAG,"Float from table : "+floatValue);
                    cursor.close();
                    return floatValue;
                }
                else {
                    cursor.close();
                    return floatValue;
                }
            }
            else {
                cursor.close();
                return floatValue;
            }
        }
        return floatValue;
    }

    public  boolean isNewItem(String itemName) {

        Cursor cursor = mDB.query(PriceDBContract.ItemsDB.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                PriceDBContract.ItemsDB.COLUMN_ITEM_NAME);

        int count = cursor.getCount();

        //No Shops present in DB
        if(count == 0) {
            Log.d(LOG_TAG,"Item : "+itemName+" not found in Table!!!");
            return true;
        }

        cursor.moveToFirst();

        for(int i = 0;i < count;i++) {
            String itemInDB = cursor.getString(cursor.getColumnIndex(PriceDBContract.ItemsDB.COLUMN_ITEM_NAME));

            if(itemInDB.equals(itemName)) {
                cursor.close();
                Log.d(LOG_TAG,"Item : "+itemName+" found in DB!!!");
                return false;
            }
            cursor.moveToNext();
        }

        Log.d(LOG_TAG,"Shop : "+itemName+" not found in DB!!!");
        cursor.close();
        return true;

    }

    /**
     * Helper method for inserting item name to DB table
     * @param itemName
     */
    public  void addItemToDB(String itemName,String price) {
        ContentValues cv = new ContentValues();
        cv.put(PriceDBContract.ItemsDB.COLUMN_ITEM_NAME,itemName);
        cv.put(PriceDBContract.ItemsDB.COLUMN_ITEM_COUNT,1);
        cv.put(PriceDBContract.ItemsDB.COLUMN_ITEM_AVERAGE_PRICE,Float.valueOf(price));

        try {
            mDB.beginTransaction();

            mDB.insert(PriceDBContract.ItemsDB.TABLE_NAME,null,cv);

            mDB.setTransactionSuccessful();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            mDB.endTransaction();
        }
    }


    public  void updateAveragePrice(String item,String price,float oldPrice,String mode) {

        Log.d(LOG_TAG,"updateAveragePrice : Begin for "+item + "Mode : "+mode);
        Cursor cursor = mDB.query(PriceDBContract.ItemsDB.TABLE_NAME,
                null,
                PriceDBContract.ItemsDB.COLUMN_ITEM_NAME+"=?",
                new String[]{item},
                null,
                null,
                null,
                null);

        float priceFloat = Float.valueOf(price);

        if(cursor.moveToFirst()) {
            float curAverage = cursor.getFloat(cursor.getColumnIndex(PriceDBContract.ItemsDB.COLUMN_ITEM_AVERAGE_PRICE));
            int curItemCount = cursor.getInt(cursor.getColumnIndex(PriceDBContract.ItemsDB.COLUMN_ITEM_COUNT));
            int newItemCount = 0;
            float newAverage = 0;

            switch (mode) {
                //Update an aleready present price entry
                case PRICEDBHELPER_UPDATE_PRICE:
                    newItemCount = curItemCount;
                    newAverage = (curAverage*(float)curItemCount - oldPrice + priceFloat) / newItemCount;
                    break;

                //Reset an old price to 0
                case PRICEDBHELPER_RESET_PRICE:
                    newItemCount = curItemCount-1;
                    if(newItemCount != 0)
                        newAverage = (curAverage*(float)curItemCount - oldPrice) / newItemCount;
                    else {
                        newAverage = 0;
                        deleteItemEntry(mDBHandler.getItemID(item));
                    }
                    break;

                //Add a new price
                case PRICEDBHELPER_ADD_PRICE:
                    newItemCount = curItemCount + 1;
                    newAverage = (curAverage * curItemCount + priceFloat) / newItemCount;
                    break;

                default:
                    break;
            }

            ContentValues cv = new ContentValues();

            cv.put(PriceDBContract.ItemsDB.COLUMN_ITEM_COUNT,newItemCount);
            cv.put(PriceDBContract.ItemsDB.COLUMN_ITEM_AVERAGE_PRICE,newAverage);

            Log.d(LOG_TAG,"For Item : "+item);
            Log.d(LOG_TAG,"updateAveragePrice : Current : "+curAverage + " New : "+newAverage);
            Log.d(LOG_TAG,"updateItemCount : Current : "+curItemCount + " New  : "+newItemCount);

            try {
                mDB.beginTransaction();

                //mDB.insert(shopName,null,cv);
                mDB.update(PriceDBContract.ItemsDB.TABLE_NAME,cv,PriceDBContract.ItemsDB.COLUMN_ITEM_NAME+"=?",new String[]{item});

                mDB.setTransactionSuccessful();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                mDB.endTransaction();
            }

            cursor.close();
        }
        else {
            Log.d(LOG_TAG,"Error while getting data from DB!!");
        }
    }

    /**
     * Method to add an item entry in the table of the corresponding shop
     * if the item entry exists for the date , it will be replaced
     * @param shopName : Name of the table
     * @param itemID
     * @param priceTag
     * @param time
     * @param dateexists
     * @param context
     */
    public  void addItemSample(String shopName,int itemID,String priceTag,String time,boolean dateexists,String mode,Context context) {
        //int currentVersion = mDB.getVersion();

        if(!dateexists) {
/*            mDB.close();
            PriceDBHelper dbHelper = new PriceDBHelper(context, currentVersion + 1, PriceDBHelper.ACTION_ADD_COLUMN_TO_TABLE, shopName, time);
            mDB = dbHelper.getWritableDatabase();*/

            final String DB_ADD_COLUMN_STATEMENT_TABLE_SHOP_NAME =
                    "ALTER TABLE "+ shopName + " ADD COLUMN "+ time + " FLOAT";
            try {
                mDB.beginTransaction();
                //SQLiteStatement statement = mDB.compileStatement(DB_ADD_COLUMN_STATEMENT_TABLE_SHOP_NAME);
                //statement.execute();
                mDB.execSQL(DB_ADD_COLUMN_STATEMENT_TABLE_SHOP_NAME);
                mDB.setTransactionSuccessful();
            }
/*            catch (Exception e) {
                Log.d(LOG_TAG,"addItemSample : Exception while adding column to table!!");
            }*/
            finally {
                mDB.endTransaction();
            }

        }
        //Why we need to do this? , otherwise column wont be included in the next call :(
        mDB.close();
        mDBHelper = new PriceDBHelper(context);
        mDB = mDBHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(PriceDBContract.PriceDB.COLUMN_ITEM_NAME,itemID);
        cv.put(time, Float.valueOf(priceTag));

        try {
            mDB.beginTransaction();

            Cursor cursor = mDB.query(shopName,
                    null,
                    PriceDBContract.PriceDB.COLUMN_ITEM_NAME+"=?",
                    new String[]{String.valueOf(itemID)},
                    null,
                    null,null);
            if(cursor.moveToFirst()) {
                int oldPriceEntryCount = cursor.getInt(cursor.getColumnIndex(PriceDBContract.PriceDB.COLUMN_ITEM_PRICE_ENTRY_COUNT));
                int newPriceEntryCount = 0;

                if(mode.equals(PriceDBHandler.PRICEDBHELPER_UPDATE_PRICE))
                    newPriceEntryCount = oldPriceEntryCount;
                else
                    newPriceEntryCount = oldPriceEntryCount + 1;

                cv.put(PriceDBContract.PriceDB.COLUMN_ITEM_PRICE_ENTRY_COUNT,newPriceEntryCount);

                mDB.update(shopName, cv, PriceDBContract.PriceDB.COLUMN_ITEM_NAME + "=?", new String[]{String.valueOf(itemID)});
            }
            else {
                cv.put(PriceDBContract.PriceDB.COLUMN_ITEM_PRICE_ENTRY_COUNT,1);

                mDB.insert(shopName, null, cv);
            }

            cursor.close();
            mDB.setTransactionSuccessful();
        }
        catch (Exception e) {
            Log.e(LOG_TAG,"CAUGHT AN EXCEPTION !!");
            e.printStackTrace();
        }
        finally {
            mDB.endTransaction();
        }
    }

    /**
     * Utiliy to get Entries from table
     * @param table
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @return
     */
    public  Cursor getEntries(String table, String[] columns, String selection,
                           String[] selectionArgs, String groupBy, String having,
                           String orderBy) {

        return mDB.query(table,columns,selection,selectionArgs,groupBy,having,orderBy);
    }


    private  void deleteShopFromShopsDB(String shopName) {

        try {
            mDB.beginTransaction();
            mDB.delete(PriceDBContract.ShopsDB.TABLE_NAME,PriceDBContract.ShopsDB.COLUMN_SHOP_NAME+"=?",
                    new String[]{shopName});
            mDB.setTransactionSuccessful();
        }
        finally {
            mDB.endTransaction();
        }


    }
    public  void resetEntry(String table,int itemID ,String column,Context context) {

        Cursor cursor = mDB.query(table,null,
                PriceDBContract.PriceDB.COLUMN_ITEM_NAME+"=?",
                new String[]{String.valueOf(itemID)},
                null,
                null,null);

        if(!cursor.moveToFirst()) {
            Log.e(LOG_TAG,"resetEntry : There is no item : "+itemID+" in shop : "+table);
            return;
        }
        int priceEntryCount = cursor.getInt(cursor.getColumnIndex(PriceDBContract.PriceDB.COLUMN_ITEM_PRICE_ENTRY_COUNT));

        if(priceEntryCount == 1) {
            //Delete table itself
            Log.v(LOG_TAG,"Shop : "+table+" has no longer any entries , deleting it!!");
            try {
                mDB.beginTransaction();
                mDB.delete(table,PriceDBContract.PriceDB.COLUMN_ITEM_NAME+"=?",new String[]{String.valueOf(itemID)});
                mDB.setTransactionSuccessful();
            }
            finally {
                mDB.endTransaction();
            }


            //If there are no rows in the table delete it
            Cursor cursorAll = getAllEntries(table,null);
            if(cursorAll.getCount() <= 0) {
/*                int currentVersion = mDB.getVersion();
                mDB.close();
                PriceDBHelper dbHelper = new PriceDBHelper(context, currentVersion + 1, PriceDBHelper.ACTION_DELETE_TABLE, table, null);
                mDB = dbHelper.getWritableDatabase();*/
                final String DB_DELETE_TABLE_STATEMENT =
                        "DROP TABLE "+table;
                try {
                    mDB.beginTransaction();
                    SQLiteStatement statement = mDB.compileStatement(DB_DELETE_TABLE_STATEMENT);
                    statement.execute();
                    mDB.setTransactionSuccessful();
                }
                finally {
                    mDB.endTransaction();
                }

                deleteShopFromShopsDB(table);
            }
        }
        else {
            ContentValues cv = new ContentValues();

            cv.put(column,(float)0.0);
            cv.put(PriceDBContract.PriceDB.COLUMN_ITEM_PRICE_ENTRY_COUNT,priceEntryCount-1);

            Log.v(LOG_TAG,"resetEntry : New priceEntryCount : " + (priceEntryCount-1));
            Log.v(LOG_TAG,"resetEntry : Date : " + column);

            try {
                mDB.beginTransaction();
                mDB.update(table,cv,PriceDBContract.PriceDB.COLUMN_ITEM_NAME+"=?",new String[]{String.valueOf(itemID)});
                mDB.setTransactionSuccessful();
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.d(LOG_TAG,"Catching an exception while.....");
            }
            finally {
                mDB.endTransaction();
            }

//            Cursor cursor1 = mDB.query(table,null,null,null,null,null,null);
//            if(cursor1.moveToFirst()) {
//                Log.d(LOG_TAG,"Column count immediately after resetting column : "+cursor1.getColumnCount());
//            }
//            else
//                Log.d(LOG_TAG,"Cursor retrive failed!!");
//
//            cursor1.close();


        }

        cursor.close();
    }

    private  void deleteItemEntry(int itemID){

        Log.v(LOG_TAG,"Deleting Item ID : "+itemID+" from Item Entry table");
        try{
            mDB.beginTransaction();
            mDB.delete(PriceDBContract.ItemsDB.TABLE_NAME,PriceDBContract.ItemsDB._ID+"="+String.valueOf(itemID),null);
            mDB.setTransactionSuccessful();
        }
        finally {
            mDB.endTransaction();
        }

    }


    public  ArrayList<Float> getPriceListofItemFromShop(String shop,int itemID) {

        Cursor cursor = mDB.query(shop,null,PriceDBContract.PriceDB.COLUMN_ITEM_NAME+"=?",new String[]{String.valueOf(itemID)},
                null,null,null);


        ArrayList<Float> priceList = new ArrayList<Float>();

        int noOfColumns = cursor.getColumnCount();

        if(cursor.moveToFirst()) {

            for(int i = 3;i < noOfColumns;i++) {
                float price = cursor.getFloat(i);
                if(price != 0) {
                    priceList.add(price);
                }
            }
        }

        cursor.close();
        return priceList;
    }

    public  void deleteItemFromShop(String shop,int itemID,Context context) {

        try {
            mDB.beginTransaction();
            mDB.delete(shop,PriceDBContract.PriceDB.COLUMN_ITEM_NAME+"=?",new String[]{String.valueOf(itemID)});
            mDB.setTransactionSuccessful();
        }finally {
            mDB.endTransaction();
        }

        checkAndDeleteShopTable(shop,context);
    }

    public  void updateAveragePriceAfterDeletion(String item,Float[] priceList) {

        int count = priceList.length;

        Cursor cursor = mDB.query(PriceDBContract.ItemsDB.TABLE_NAME,
                null,
                PriceDBContract.ItemsDB.COLUMN_ITEM_NAME+"=?",
                new String[]{item},
                null,
                null,
                null,
                null);

        if(cursor.moveToFirst()) {
            float curAverage = cursor.getFloat(cursor.getColumnIndex(PriceDBContract.ItemsDB.COLUMN_ITEM_AVERAGE_PRICE));
            int curItemCount = cursor.getInt(cursor.getColumnIndex(PriceDBContract.ItemsDB.COLUMN_ITEM_COUNT));
            float curSum = curAverage*(float)curItemCount;
            int newItemCount = curItemCount;
            float newAverage = 0;

            Log.v(LOG_TAG,"updateAveragePriceAfterDeletion : curItemCount : "+curItemCount+" curAverage : "+curAverage);

            for(int i = 0;i < count;i++) {
                curSum -= priceList[i];
                newItemCount--;
            }

            if(newItemCount <= 0) {
                newAverage = 0;
                deleteItemEntry(mDBHandler.getItemID(item));
                Log.d(LOG_TAG,"All Entries for Item removed from DB!!");
            }
            else {
                newAverage = curSum/newItemCount;
            }

            ContentValues cv = new ContentValues();

            cv.put(PriceDBContract.ItemsDB.COLUMN_ITEM_COUNT,newItemCount);
            cv.put(PriceDBContract.ItemsDB.COLUMN_ITEM_AVERAGE_PRICE,newAverage);

            Log.d(LOG_TAG,"updateAveragePrice : Current Avg Price "+curAverage + " New Avg : "+newAverage);

            try {
                mDB.beginTransaction();

                //mDB.insert(shopName,null,cv);
                mDB.update(PriceDBContract.ItemsDB.TABLE_NAME,cv,PriceDBContract.ItemsDB.COLUMN_ITEM_NAME+"=?",new String[]{item});

                mDB.setTransactionSuccessful();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                mDB.endTransaction();
            }

            cursor.close();
        }
        else {
            Log.d(LOG_TAG,"Error while getting data from DB!!");
        }


    }

    private  void checkAndDeleteShopTable(String shopname,Context context) {

        Cursor cursor = mDB.query(shopname,
                null,
                null,
                null,
                null,null,
                null);

        if(!cursor.moveToFirst()) {
                Log.d(LOG_TAG,"Since Shop table empty deleting shop Table : "+shopname);
/*                int currentVersion = mDB.getVersion();
                mDB.close();
                PriceDBHelper dbHelper = new PriceDBHelper(context, currentVersion + 1, PriceDBHelper.ACTION_DELETE_TABLE, shopname, null);
                mDB = dbHelper.getWritableDatabase();*/
                final String DB_DELETE_TABLE_STATEMENT =
                        "DROP TABLE "+shopname;
                try {
                    mDB.beginTransaction();
                    SQLiteStatement statement = mDB.compileStatement(DB_DELETE_TABLE_STATEMENT);
                    statement.execute();
                    mDB.setTransactionSuccessful();
                }
                finally {
                    mDB.endTransaction();
                }
                deleteShopFromShopsDB(shopname);
        }

        cursor.close();
    }

    public  void deleteItemFromAllShops(int itemID,Context context) {

        Cursor cursor = getAllEntries(PriceDBContract.ShopsDB.TABLE_NAME,null);

        while(cursor.moveToNext()) {

            String shopName = cursor.getString(cursor.getColumnIndex(PriceDBContract.ShopsDB.COLUMN_SHOP_NAME));
            Log.d(LOG_TAG, "deleteItemFromAllShops : Shop from DB : " + shopName);

            Cursor cursorForItem = getEntries(shopName,
                    null,
                    PriceDBContract.PriceDB.COLUMN_ITEM_NAME + "=?",
                    new String[]{String.valueOf(itemID)},
                    null,
                    null,
                    null);

            if(cursorForItem.getCount() > 0) {
                Log.v(LOG_TAG,"deleteItemFromAllShops : ItemID "+itemID+" found in shop :"+shopName);
                deleteItemFromShop(shopName,itemID,context);
            }

            cursorForItem.close();
        }

        deleteItemEntry(itemID);

        cursor.close();
    }

    public  int getItemID(String itemName) {

        Cursor cursor = mDB.query(PriceDBContract.ItemsDB.TABLE_NAME,
                null,
                PriceDBContract.ItemsDB.COLUMN_ITEM_NAME+"=?",
                new String[]{itemName},
                null,
                null,null);
        int id = 0;

        if(!cursor.moveToFirst()) {
            Log.d(LOG_TAG,"Item : "+itemName+" not found in DB!!");
        }
        else {
            id = cursor.getInt(cursor.getColumnIndex(PriceDBContract.ItemsDB._ID));
        }
        Log.d(LOG_TAG,"getItemID , itemName :"+itemName+" ID : "+id);
        cursor.close();
        return id;
    }

    public  void rename(String item,String newItemName) {

        ContentValues cv = new ContentValues();

        cv.put(PriceDBContract.ItemsDB.COLUMN_ITEM_NAME,newItemName);

        try {
            mDB.beginTransaction();
            mDB.update(PriceDBContract.ItemsDB.TABLE_NAME,
                    cv, PriceDBContract.ItemsDB.COLUMN_ITEM_NAME+"=?",
                    new String[]{item});
            mDB.setTransactionSuccessful();
        }
        finally {
            mDB.endTransaction();
        }

    }

}
