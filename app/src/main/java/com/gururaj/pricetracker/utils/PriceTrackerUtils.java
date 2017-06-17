package com.gururaj.pricetracker.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;

import com.gururaj.pricetracker.R;
import com.gururaj.pricetracker.data.PriceDBContract;
import com.gururaj.pricetracker.data.Tuple;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Gururaj on 5/14/2017.
 */

public class PriceTrackerUtils {

    public static TypedArray getDefaultItemListIconIDs(Context context) {
        TypedArray defaultItemsIconsArray;
        defaultItemsIconsArray = context.getResources().obtainTypedArray(R.array.default_item_list_drawable_array);

        return defaultItemsIconsArray;
    }

    public static ArrayList<String> getDefaultItemsList(Context context) {

        String[] itemsListStringArray = context.getResources().getStringArray(R.array.item_list_predefined);

        ArrayList<String> itemListArrayList = new ArrayList<String>(Arrays.asList(itemsListStringArray));

        return itemListArrayList;
    }

    public static ArrayList<Tuple<String, Float>> getTupleList(Cursor cursor) {

        ArrayList<Tuple<String, Float>> list = new ArrayList<Tuple<String, Float>>();

        while(cursor.moveToNext()) {
            Tuple<String, Float> item = new Tuple<>(cursor.getString(cursor.getColumnIndex(PriceDBContract.ItemsDB.COLUMN_ITEM_NAME)),
                    cursor.getFloat(cursor.getColumnIndex(PriceDBContract.ItemsDB.COLUMN_ITEM_AVERAGE_PRICE)));
            list.add(item);
        }

        return list;
    }

    public static String parseForDBInsert(String input) {

        String output = "\""+input+"\"";
        return output;
    }

    public static String deParseFromDBQuery(String input) {
        String output = input.substring(1,input.length()-1);
        return output;
    }
}
