package com.gururaj.pricetracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Gururaj on 4/15/2017.
 */

public class PriceDBHelper extends SQLiteOpenHelper {

    static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "prices.db";
    private final String LOG_TAG = PriceDBHelper.class.getSimpleName();
    private String table;
    private String column;
    private int action;

    public static final int ACTION_ADD_NEW_TABLE = 1;
    public static final int ACTION_ADD_COLUMN_TO_TABLE = 2;
    public static final int ACTION_DELETE_TABLE = 3;

    //private int version = DATABASE_VERSION;

    //action -> ACTION_ADD_NEW_TABLE or ACTION_ADD_COLUMN_TO_TABLE
    //name -> Table name if action is ACTION_ADD_NEW_TABLE
    //name -> Column name if action is ACTION_ADD_COLUMN_TO_TABLE
    public PriceDBHelper(Context context,int version,int action,String table,String column) {
        super(context,DATABASE_NAME,null,version);
        this.table = table;
        this.column = column;
        this.action = action;
    }

    public PriceDBHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
/*        final String DB_CREATE_STATEMENT =
                "CREATE TABLE " + PriceDBContract.PriceDB.TABLE_NAME + " (" +
                 PriceDBContract.PriceDB._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        PriceDBContract.PriceDB.COLUMN_ITEM_NAME + " TEXT," +
                        PriceDBContract.PriceDB.COLUMN_PRICE + " INTEGER)";*/

        //Statement for Items Tables creation
        final String DB_CREATE_STATEMENT_TABLE_ITEMS =
                "CREATE TABLE " + PriceDBContract.ItemsDB.TABLE_NAME + " (" +
                        PriceDBContract.ItemsDB._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        PriceDBContract.ItemsDB.COLUMN_ITEM_NAME + " TEXT,"+
                        PriceDBContract.ItemsDB.COLUMN_ITEM_COUNT + " INTEGER,"+
                        PriceDBContract.ItemsDB.COLUMN_ITEM_AVERAGE_PRICE + " FLOAT)";

        //Statement for Shops Tables creation
        final String DB_CREATE_STATEMENT_TABLE_SHOPS =
                "CREATE TABLE " + PriceDBContract.ShopsDB.TABLE_NAME + " (" +
                        PriceDBContract.ShopsDB._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        PriceDBContract.ShopsDB.COLUMN_SHOP_NAME + " TEXT)";

        db.execSQL(DB_CREATE_STATEMENT_TABLE_ITEMS);
        db.execSQL(DB_CREATE_STATEMENT_TABLE_SHOPS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion > oldVersion) {

            if(action == ACTION_ADD_NEW_TABLE) {
                final String DB_CREATE_STATEMENT_TABLE_SHOP_NAME =
                        "CREATE TABLE " + table + " (" +
                                PriceDBContract.PriceDB._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                                PriceDBContract.PriceDB.COLUMN_ITEM_NAME + " INTEGER,"+
                                PriceDBContract.PriceDB.COLUMN_ITEM_PRICE_ENTRY_COUNT + " INTEGER)";

                db.execSQL(DB_CREATE_STATEMENT_TABLE_SHOP_NAME);
                Log.d(LOG_TAG,"Table : "+table+" added to DB!!");
            }
            else if(action == ACTION_ADD_COLUMN_TO_TABLE) {
                final String DB_ADD_COLUMN_STATEMENT_TABLE_SHOP_NAME =
                "ALTER TABLE "+ table + " ADD COLUMN "+ "D" + column + " FLOAT";

                db.execSQL(DB_ADD_COLUMN_STATEMENT_TABLE_SHOP_NAME);

                Log.d(LOG_TAG,"New Column :D"+ column + " added to Table : "+table);

            }
            else if(action == ACTION_DELETE_TABLE) {

                final String DB_DELETE_TABLE_STATEMENT =
                        "DROP TABLE "+table;

                db.execSQL(DB_DELETE_TABLE_STATEMENT);

                Log.d(LOG_TAG,"Table : "+table+" deleted from DB!");
            }
        }
    }

    //To avoid crash
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //super.onDowngrade(db, oldVersion, newVersion);
    }
}
