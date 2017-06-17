package com.gururaj.pricetracker;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.annotation.StringDef;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gururaj.pricetracker.data.PriceDBContract;
import com.gururaj.pricetracker.data.Tuple;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Gururaj on 4/15/2017.
 */

public class ItemListAdaptor extends RecyclerView.Adapter<ItemListAdaptor.ListItemViewHolder> {

    private ArrayList<Tuple<String,Float>> mData;
    private Context context;
    private String LOG_TAG = ItemListAdaptor.class.getSimpleName();
    private final ListItemClickListener mListner;
    private RecyclerView mRCView;
    private ArrayList<String> mdefaultList;
    private TypedArray mdefaultListIcons;

    public ItemListAdaptor(Context context, ArrayList<Tuple<String,Float>> data, ListItemClickListener listener, RecyclerView rcView,
                           ArrayList<String> defaultList, TypedArray defaultListIcons) {
        this.context = context;
        mData = data;
        mListner = listener;
        mRCView = rcView;
        mdefaultList = defaultList;
        mdefaultListIcons = defaultListIcons;
    }

    //Interface for click handling
    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex,String item,int imageresID);
        void onListItemLongClick(int clickedItemIndex,String item);
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int id = R.layout.layout_list_item;
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(id,parent,false);

        ListItemViewHolder viewHolder = new ListItemViewHolder(view);
        return viewHolder;
    }

    private int getIndexfromDefaultList(String item) {
        String[] itemWords = item.split(" ");
        int index = -1;

        for(String itemWord : itemWords) {
            if(mdefaultList.contains(itemWord)) {
                index = mdefaultList.indexOf(itemWord);
                return index;
            }
        }
        return index;
    }
    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {

        //Log.d(LOG_TAG,"onBindViewHolder , position : "+position);

        if(mData.get(position) == null)
            return;

        //Get Item Name
/*        String item = mCursor.getString(mCursor.getColumnIndex(PriceDBContract.ItemsDB.COLUMN_ITEM_NAME));
        float avgPrice = mCursor.getFloat(mCursor.getColumnIndex(PriceDBContract.ItemsDB.COLUMN_ITEM_AVERAGE_PRICE));*/
        String item = mData.get(position).x;
        float avgPrice = mData.get(position).y;


        holder.tvListItemName.setText(item);
        holder.tvListItemName.setSelected(true);
        holder.tvListItemPrice.setText(String.format("%.1f",avgPrice));

        if(mdefaultList.contains(item)) {
            int id = mdefaultListIcons.getResourceId(mdefaultList.indexOf(item),-1);
            holder.ivItemIcon.setImageResource(id);
            holder.ivItemIcon.setTag(id);
        }
        else {
            holder.ivItemIcon.setImageResource(R.drawable.ic_salad);
            holder.ivItemIcon.setTag(R.drawable.ic_salad);
        }
/*        int id = getIndexfromDefaultList(item);
        if(id == -1) {
            holder.ivItemIcon.setImageResource(R.drawable.ic_salad);
            holder.ivItemIcon.setTag(R.drawable.ic_salad);
        }
        else {
            int imageId = mdefaultListIcons.getResourceId(id,-1);
            holder.ivItemIcon.setImageResource(imageId);
            holder.ivItemIcon.setTag(imageId);
        }*/

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(ArrayList<Tuple<String,Float>> data,boolean refresh) {
        this.mData = data;
        if(refresh)
            notifyDataSetChanged();
    }

    public ArrayList<Tuple<String,Float>> getData() {
        return mData;
    }

    class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{

        TextView tvListItemName;
        TextView tvListItemPrice;
        ImageView ivItemIcon;
        //TextView tvSecretText;
        //TextView tvSecretText2;

        public ListItemViewHolder(View itemView) {
            super(itemView);

            tvListItemName = (TextView)itemView.findViewById(R.id.tv_list_item);
            tvListItemPrice = (TextView)itemView.findViewById(R.id.tv_list_item_price);
            ivItemIcon = (ImageView)itemView.findViewById(R.id.iv_item_icon);
            //tvSecretText = (TextView)itemView.findViewById(R.id.tv_details);
            //tvSecretText2 = (TextView)itemView.findViewById(R.id.tv_details_2);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            //Log.d(LOG_TAG,"Clicked on RV Item : Postion : "+position);
            TextView listItemName = (TextView)itemView.findViewById(R.id.tv_list_item);
            ImageView imageView = (ImageView)itemView.findViewById(R.id.iv_item_icon);
            int id = (int)imageView.getTag();

            mListner.onListItemClick(position,listItemName.getText().toString(),id);
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            TextView listItemName = (TextView)itemView.findViewById(R.id.tv_list_item);
            mListner.onListItemLongClick(position,listItemName.getText().toString());
            return true;
        }
    }
}
