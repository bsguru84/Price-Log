package com.gururaj.pricetracker;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.gururaj.pricetracker.async.DBBackgroundTask;
import com.gururaj.pricetracker.utils.PriceTrackerUtils;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static com.gururaj.pricetracker.AddItemActivity.EXTRA_DATA_UPDATED_FLAG;
import static com.gururaj.pricetracker.MainActivity.EXTRA_DATA_ITEM_NAME;
import static com.gururaj.pricetracker.MainActivity.EXTRA_DATA_PRICE_TAG;
import static com.gururaj.pricetracker.MainActivity.EXTRA_DATA_SHOP_NAME;
import static com.gururaj.pricetracker.MainActivity.EXTRA_DATA_TIME;

public class ShowItemEntryActivity extends AppCompatActivity {

    private String mItem;
    private String mShop;
    private Float mPrice;
    private String mDate;

    private TextView mTVItemValue;
    private TextView mTVShopValue;
    private TextView mTVPriceValue;
    private TextView mTVDateValue;
    //private String mDateSlashed;

    private boolean mUpdated = false;

    private final String LOG_TAG = ShowItemEntryActivity.class.getSimpleName();

    private static final int REQUEST_EDIT_ITEM_ENTRY = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_iem_entry);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String shopNameUnQuoted;


        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mItem = getIntent().getStringExtra(EXTRA_DATA_ITEM_NAME);
        mShop = getIntent().getStringExtra(EXTRA_DATA_SHOP_NAME);

        String title = getResources().getString(R.string.item_price_entry,mItem);
        getSupportActionBar().setTitle(title);

        Log.d(LOG_TAG,"Shop Name Received : "+mShop);

        shopNameUnQuoted = mShop.substring(1,mShop.length()-1);

        mPrice = getIntent().getFloatExtra(EXTRA_DATA_PRICE_TAG,0);
        mDate = getIntent().getStringExtra(EXTRA_DATA_TIME);

/*        String day = mDate.substring(1,3);
        String month = mDate.substring(3,5);
        String year = mDate.substring(5,7);*/

        //mDateSlashed = day+"/"+month+"/"+year;

        mTVItemValue = (TextView)findViewById(R.id.tv_item_name);
        mTVShopValue = (TextView)findViewById(R.id.tv_shop_name);
        mTVPriceValue = (TextView)findViewById(R.id.tv_price_name);
        mTVDateValue = (TextView)findViewById(R.id.tv_date_name);

        mTVItemValue.setText(mItem);
        mTVShopValue.setText(shopNameUnQuoted);
        mTVPriceValue.setText(String.valueOf(mPrice));
        mTVDateValue.setText(mDate);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.edit_item_entry:
                launchEditEntryActivity();

        }
        return super.onOptionsItemSelected(item);
    }

    private void launchEditEntryActivity() {

        Intent editItemEntryIntent = new Intent(this,AddItemActivity.class);

        editItemEntryIntent.putExtra(EXTRA_DATA_ITEM_NAME,mItem);
        editItemEntryIntent.putExtra(EXTRA_DATA_SHOP_NAME,mShop);
        editItemEntryIntent.putExtra(EXTRA_DATA_PRICE_TAG,mPrice);
        editItemEntryIntent.putExtra(EXTRA_DATA_TIME,mDate);

        startActivityForResult(editItemEntryIntent,REQUEST_EDIT_ITEM_ENTRY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

/*        if(resultCode == RESULT_CANCELED)
            mRefresh = false;*/

        Log.d(LOG_TAG,"onActivityResult");

        if(requestCode == REQUEST_EDIT_ITEM_ENTRY) {

            if(resultCode == RESULT_OK) {

                String itemName = mItem = data.getStringExtra(EXTRA_DATA_ITEM_NAME);

                String shopName = mShop = data.getStringExtra(EXTRA_DATA_SHOP_NAME);
                String shopUnQuote = PriceTrackerUtils.deParseFromDBQuery(shopName);
                String priceTag  = data.getStringExtra(EXTRA_DATA_PRICE_TAG);
                mPrice = Float.parseFloat(priceTag);

                String date = data.getStringExtra(EXTRA_DATA_TIME);
                //String dateForDB = PriceTrackerUtils.parseForDBInsert(date);
                //String dateSlashed = date.substring(0,2)+"/"+date.substring(2,4)+"/"+date.substring(4,6);
                mDate = PriceTrackerUtils.deParseFromDBQuery(date);

                mUpdated = data.getBooleanExtra(EXTRA_DATA_UPDATED_FLAG,false);

                Log.d(LOG_TAG,"Item : "+data.getStringExtra(EXTRA_DATA_ITEM_NAME));
                Log.d(LOG_TAG,"Shop : "+data.getStringExtra(EXTRA_DATA_SHOP_NAME));
                Log.d(LOG_TAG,"Price : "+data.getStringExtra(EXTRA_DATA_PRICE_TAG));
                Log.d(LOG_TAG,"Date : "+date);

                if(mUpdated) {
                    mTVItemValue.setText(itemName);
                    mTVShopValue.setText(shopUnQuote);
                    mTVPriceValue.setText(priceTag);
                    mTVDateValue.setText(mDate);

                    DBBackgroundTask dbTask = new DBBackgroundTask(this, null);
                    dbTask.execute(itemName, shopName, priceTag, date);
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mUpdated) {
                Intent startMainActivityIntent = new Intent(this,MainActivity.class);
                startMainActivityIntent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(startMainActivityIntent);
                return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_show_item_entry,menu);
        return true;
    }

}
