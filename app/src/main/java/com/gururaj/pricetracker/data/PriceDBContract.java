package com.gururaj.pricetracker.data;

import android.provider.BaseColumns;

/**
 * Created by Gururaj on 4/15/2017.
 */

public class PriceDBContract {

    /*
        No  need to instantiate this class , so a private constructor!!
    */
    private PriceDBContract(){}

    public static final class PriceDB implements BaseColumns {

        //Table name is dummy as it will actually replaced by shop name
        public static final String TABLE_NAME = "prices";
        //Item name such as Tomato
        public static final String COLUMN_ITEM_NAME = "itemname";
        //No of entries for this item in the shop
        public static final String COLUMN_ITEM_PRICE_ENTRY_COUNT = "itemcount";
        //Price coloumn , but columns are variable
        public static final String COLUMN_PRICE= "price";
    }

    public static final class ItemsDB implements BaseColumns {

        //Table name
        public static final String TABLE_NAME = "itemstable";
        //Item name such as Tomato
        public static final String COLUMN_ITEM_NAME = "items";
        //Total number of entries
        public static final String COLUMN_ITEM_COUNT = "itemcount";
        //Average price
        public static final String COLUMN_ITEM_AVERAGE_PRICE = "itemaverageprice";
    }

    public static final class ShopsDB implements BaseColumns {

        //Table name
        public static final String TABLE_NAME = "shopstable";
        //Shop name such as Burma Bazaar
        public static final String COLUMN_SHOP_NAME = "shops";
    }
}
