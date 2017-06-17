package com.gururaj.pricetracker.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gururaj on 4/15/2017.
 */

public class FakeDataUtil {

    public static void InsertFakeData(SQLiteDatabase db) {

        if(db == null)
            return;

        List<ContentValues> list = new ArrayList<ContentValues>();

        ContentValues cv = new ContentValues();
        cv.put(PriceDBContract.ShopsDB.COLUMN_SHOP_NAME,"Burma");
        //cv.put(PriceDBContract.PriceDB.COLUMN_PRICE,15);
        list.add(cv);

/*        cv = new ContentValues();
        cv.put(PriceDBContract.PriceDB.COLUMN_ITEM_NAME,"Potato");
        cv.put(PriceDBContract.PriceDB.COLUMN_PRICE,16);
        list.add(cv);

        cv = new ContentValues();
        cv.put(PriceDBContract.PriceDB.COLUMN_ITEM_NAME,"Grapes");
        cv.put(PriceDBContract.PriceDB.COLUMN_PRICE,110);
        list.add(cv);

        cv = new ContentValues();
        cv.put(PriceDBContract.PriceDB.COLUMN_ITEM_NAME,"Mango");
        cv.put(PriceDBContract.PriceDB.COLUMN_PRICE,90);
        list.add(cv);


        cv = new ContentValues();
        cv.put(PriceDBContract.PriceDB.COLUMN_ITEM_NAME,"Banana");
        cv.put(PriceDBContract.PriceDB.COLUMN_PRICE,70);
        list.add(cv);

        cv = new ContentValues();
        cv.put(PriceDBContract.PriceDB.COLUMN_ITEM_NAME,"Apple");
        cv.put(PriceDBContract.PriceDB.COLUMN_PRICE,190);
        list.add(cv);*/

        try
        {
            db.beginTransaction();
            //Clear the table
            db.delete(PriceDBContract.ShopsDB.TABLE_NAME,null,null);

            for(ContentValues c : list) {
                db.insert(PriceDBContract.ShopsDB.TABLE_NAME,null,c);
            }

            db.setTransactionSuccessful();
        }
        catch (SQLiteException e) {
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
        }
    }
}
