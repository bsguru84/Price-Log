package com.gururaj.pricetracker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gururaj.pricetracker.data.PriceDBContract;
import com.gururaj.pricetracker.data.PriceDBHandler;
import com.gururaj.pricetracker.data.PriceDBHelper;
import com.gururaj.pricetracker.utils.PriceTrackerUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.zip.Inflater;

import static com.gururaj.pricetracker.MainActivity.EXTRA_DATA_ITEM_NAME;
import static com.gururaj.pricetracker.MainActivity.EXTRA_DATA_PRICE_TAG;
import static com.gururaj.pricetracker.MainActivity.EXTRA_DATA_SHOP_NAME;
import static com.gururaj.pricetracker.MainActivity.EXTRA_DATA_TIME;

public class AddItemActivity extends AppCompatActivity {

    private Button buttonAddItem;
/*    private AutoCompleteTextView itemText;
    private AutoCompleteTextView shopText;
    private EditText priceText;
    private EditText dateText;*/
    private PriceDBHandler mDBHandler;
    private Calendar myCalendar = Calendar.getInstance();
    private final String LOG_TAG = AddItemActivity.class.getSimpleName();
    private final Integer ACTION_GET_ITEM_LIST = 0;
    private final Integer ACTION_GET_SHOPS_LIST = 1;

    private String mItem;
    private String mShop;
    private Float mPrice;
    private String mDate;

    private boolean mUpdateMode = false;

    TextInputLayout mTextInputLayoutWhat;
    TextInputLayout mTextInputLayoutWhere;
    TextInputLayout mTextInputLayoutCost;
    TextInputLayout mTextInputLayoutWhen;

    AutoCompleteTextView mTextinputWhat;
    AutoCompleteTextView mTextinputWhere;
    TextInputEditText mTextinputCost;
    TextInputEditText mTextinputWhen;

    static final String EXTRA_DATA_UPDATED_FLAG = "updated";

    private TypedArray defaultItemsIconsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item_material);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTextInputLayoutWhat = (TextInputLayout)findViewById(R.id.tilayout_what);
        mTextInputLayoutWhere = (TextInputLayout)findViewById(R.id.tilayout_where);
        mTextInputLayoutCost = (TextInputLayout)findViewById(R.id.tilayout_cost);
        mTextInputLayoutWhen = (TextInputLayout)findViewById(R.id.tilayout_when);

        mTextinputWhat = (AutoCompleteTextView) mTextInputLayoutWhat.findViewById(R.id.et_what);
        mTextinputWhere = (AutoCompleteTextView) mTextInputLayoutWhere.findViewById(R.id.et_where);
        mTextinputCost = (TextInputEditText) mTextInputLayoutCost.findViewById(R.id.et_cost);
        mTextinputWhen = (TextInputEditText) mTextInputLayoutWhen.findViewById(R.id.et_when);
        buttonAddItem = (Button)findViewById(R.id.button_add_item);

/*        itemText = (AutoCompleteTextView)findViewById(R.id.et_item_name);
        shopText = (AutoCompleteTextView)findViewById(R.id.et_shop_name);
        priceText = (EditText)findViewById(R.id.et_price);
        dateText = (EditText)findViewById(R.id.et_date);*/

        if(getIntent().hasExtra(EXTRA_DATA_ITEM_NAME)) {
            mItem = getIntent().getStringExtra(EXTRA_DATA_ITEM_NAME);
            mTextinputWhat.setText(mItem);
            mUpdateMode = true;
        }
        if(getIntent().hasExtra(EXTRA_DATA_SHOP_NAME)) {
            mShop = getIntent().getStringExtra(EXTRA_DATA_SHOP_NAME);
            mShop = mShop.substring(1,mShop.length()-1);
            mTextinputWhere.setText(mShop);
        }
        if(getIntent().hasExtra(EXTRA_DATA_PRICE_TAG)) {
            mPrice = getIntent().getFloatExtra(EXTRA_DATA_PRICE_TAG, 0);
            mTextinputCost.setText(String.valueOf(mPrice));
        }
        if(getIntent().hasExtra(EXTRA_DATA_TIME)) {
            mDate = getIntent().getStringExtra(EXTRA_DATA_TIME);
            mTextinputWhen.setText(mDate);
        }

        if(mUpdateMode) {
            String title = getResources().getString(R.string.update_item_entry_title);
            getSupportActionBar().setTitle(title);
            buttonAddItem.setText(getString(R.string.update));
        }

        mDBHandler = PriceDBHandler.getInstance();

        defaultItemsIconsArray = getResources().obtainTypedArray(R.array.default_item_list_drawable_array);


        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                myCalendar.set(Calendar.HOUR,1);
                myCalendar.set(Calendar.MINUTE,0);
                myCalendar.set(Calendar.SECOND,0);
                myCalendar.set(Calendar.AM_PM,0);
                updateDateLabel(null);
            }

        };

        if(mUpdateMode)
            updateDateLabel(mDate);
        else
            updateDateLabel(null);

        buttonAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String item = mTextinputWhat.getText().toString();
                String shop = mTextinputWhere.getText().toString();
                String price = mTextinputCost.getText().toString();

                boolean updated = false;

                if( item.isEmpty() || item.length() == 0
                    || shop.isEmpty() || shop.length() == 0
                    || price.isEmpty() || price.length() == 0) {
                    Toast.makeText(AddItemActivity.this,getString(R.string.fields_empty),Toast.LENGTH_SHORT).show();
                    return;
                }

                price = String.format("%.1f",Float.valueOf(price));

                if(price.equals("0.0")) {
                    Toast.makeText(AddItemActivity.this,getString(R.string.fields_price_zero),Toast.LENGTH_SHORT).show();
                    return;
                }

                if(mUpdateMode) {
                    if(!mItem.equals(item) || !mShop.equals(shop) || !mDate.equals(mTextinputWhen.getText().toString()) ||
                            !String.valueOf(mPrice).equals(price)) {
                        Log.d(LOG_TAG,"There is update in the edit!!");
                        updated = true;
                    }
                }

                //Delete old entry first
                if(mUpdateMode && updated) {
                    Log.d(LOG_TAG,"Date Value : "+mDate);
                    String shopQuoted = PriceTrackerUtils.parseForDBInsert(mShop);
                    mDBHandler.resetEntry(shopQuoted, mDBHandler.getItemID(mItem), PriceTrackerUtils.parseForDBInsert(mDate),
                            AddItemActivity.this);
                    mDBHandler.updateAveragePrice(mItem, "0", Float.valueOf(mPrice), PriceDBHandler.PRICEDBHELPER_RESET_PRICE);
                }

                shop = PriceTrackerUtils.parseForDBInsert(shop);;//SQlite hack for table name
                Intent returnResults = new Intent();
                returnResults.putExtra(EXTRA_DATA_ITEM_NAME,item);
                returnResults.putExtra(EXTRA_DATA_SHOP_NAME,shop);
                returnResults.putExtra(EXTRA_DATA_PRICE_TAG,price);

                if(mUpdateMode){
                    returnResults.putExtra(EXTRA_DATA_UPDATED_FLAG,updated);
                }

                myCalendar.set(Calendar.HOUR,1);
                myCalendar.set(Calendar.MINUTE,0);
                myCalendar.set(Calendar.AM_PM,0);

                //returnResults.putExtra(EXTRA_DATA_TIME,myCalendar.getTimeInMillis());
                String date = mTextinputWhen.getText().toString();
                //String[] dateSplits = date.split("/");

                //date = dateSplits[0]+dateSplits[1]+dateSplits[2];
                date = PriceTrackerUtils.parseForDBInsert(date);

                returnResults.putExtra(EXTRA_DATA_TIME,date);

                myCalendar.set(Calendar.SECOND,0);
                setResult(RESULT_OK,returnResults);

                finish();
            }
        });

        mTextinputWhen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddItemActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        GetItemsTask getItemsTask = new GetItemsTask(ACTION_GET_ITEM_LIST);
        getItemsTask.execute(ACTION_GET_ITEM_LIST);

        GetItemsTask getShopsTask = new GetItemsTask(ACTION_GET_SHOPS_LIST);
        getShopsTask.execute(ACTION_GET_SHOPS_LIST);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateDateLabel(String date) {

        if(date != null) {
            mTextinputWhen.setText(date);
        }
        else {
            String myFormat = "dd/MM/yy"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

            mTextinputWhen.setText(sdf.format(myCalendar.getTime()));
            myCalendar.getTimeInMillis();
        }
    }


    private class GetItemsTask extends AsyncTask<Integer,Void,ArrayList<String>> {

        private Integer mType;
        private ArrayList<String> itemListFinal = null;
        public GetItemsTask(Integer type) {
            mType = type;
        }
        @Override
        protected void onPostExecute(final ArrayList<String>  itemsArray) {
            super.onPostExecute(itemsArray);

            if(itemsArray == null) {
                Log.d(LOG_TAG,"There are no items/shops in DB!!");
                return;
            }
            if(mType.equals(ACTION_GET_ITEM_LIST)) {

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddItemActivity.this,
                        R.layout.layout_array_adapter,R.id.tv_adaptor,itemsArray) {
                    //int id = android.R.layout.simple_dropdown_item_1line;
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        //View view =  super.getView(position, convertView, parent);

                        Log.d(LOG_TAG,"Position : "+position);
                        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                        View view = inflater.inflate(R.layout.layout_array_adapter,parent,false);

                        ImageView imageViewIcon = (ImageView)view.findViewById(R.id.iv_item_icon_adaptor);
                        int pos = itemsArray.indexOf(getItem(position));

                        int defaultID = R.drawable.ic_salad;
                        int id = defaultItemsIconsArray.getResourceId(pos,defaultID);
                        imageViewIcon.setImageResource(id);


                        TextView textViewItem = (TextView)view.findViewById(R.id.tv_adaptor);
                        textViewItem.setText(getItem(position));

                        return view;
                    }
                };

                mTextinputWhat.setAdapter(adapter);

            }
            else if(mType.equals(ACTION_GET_SHOPS_LIST)) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddItemActivity.this,
                        android.R.layout.simple_dropdown_item_1line, itemsArray);

                mTextinputWhere.setAdapter(adapter);
                //mDB.close();
            }
        }

        @Override
        protected ArrayList<String> doInBackground(Integer... params) {

            String tableName = null;
            String sortType = null;
            String[] itemListFinalString = null;
            if(params[0].equals(ACTION_GET_ITEM_LIST)) {
                tableName = PriceDBContract.ItemsDB.TABLE_NAME;
                sortType = PriceDBContract.ItemsDB.COLUMN_ITEM_NAME;

                //Get Default Items from xml!!
                String[] defaultItemList = getResources().getStringArray(R.array.item_list_predefined);
                itemListFinal = new ArrayList<String>(Arrays.asList(defaultItemList));

            }
            else if(params[0].equals(ACTION_GET_SHOPS_LIST)){
                tableName = PriceDBContract.ShopsDB.TABLE_NAME;
                sortType = PriceDBContract.ShopsDB.COLUMN_SHOP_NAME;
                itemListFinal = new ArrayList<String>();
                //Log.d(LOG_TAG,"Setting Table : "+tableName+" Coloumn Name : "+sortType);
            }
            else {
                Log.d(LOG_TAG,"Error : Task is of neither types!!");
            }
            Log.d(LOG_TAG,"Setting Table : "+tableName+" Coloumn Name : "+sortType);
            Cursor cursor = mDBHandler.getAllEntries(tableName,sortType);

            while(cursor.moveToNext()) {

                String item = cursor.getString(cursor.getColumnIndex(sortType));

                if(params[0].equals(ACTION_GET_ITEM_LIST)) {
                    if(!itemListFinal.contains(item)) {
                        itemListFinal.add(item);
                    }
                }
                else {
                    item = item.substring(1,item.length()-1);
                    itemListFinal.add(item);
                }
            }
            cursor.close();

/*            if(itemListFinal.size() > 0)
                itemListFinalString = itemListFinal.toArray(new String[itemListFinal.size()]);*/

            return itemListFinal;
        }
    }
}
