package com.gururaj.pricetracker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gururaj.pricetracker.data.Tuple;
import com.gururaj.pricetracker.utils.PriceTrackerUtils;

import java.util.ArrayList;

/**
 * Created by Gururaj on 5/11/2017.
 */

public class ItemPriceHistoryAdaptor extends RecyclerView.Adapter<ItemPriceHistoryAdaptor.ListItemViewHolder> {

    ArrayList<Tuple<String,Float>> mPriceHistoryList;
    ListItemClickListener mClickListener;

    public ItemPriceHistoryAdaptor(ArrayList<Tuple<String,Float>> priceHistoryList,ListItemClickListener clickListener) {
        mPriceHistoryList = priceHistoryList;
        mClickListener = clickListener;
    }

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
        //void onListItemLongClick(int clickedItemIndex,String item);
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        View view = inflator.inflate(R.layout.layout_list_item,parent,false);
        ListItemViewHolder viewHolder = new ListItemViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {

        if(position < mPriceHistoryList.size() ) {
            String date = mPriceHistoryList.get(position).x;

/*            String day = date.substring(1,3);
            String month = date.substring(3,5);
            String year = date.substring(5,7);*/

            //holder.shopText.setText(day+"/"+month+"/"+year);
            holder.shopText.setText(date);

            holder.priceText.setText(String.format("%.1f",mPriceHistoryList.get(position).y));

            //holder.itemView.setId(position);
        }

    }

    @Override
    public int getItemCount() {
         return mPriceHistoryList.size();
    }

    public void setData(ArrayList<Tuple<String,Float>> priceHistoryList) {
        mPriceHistoryList = priceHistoryList;
        notifyDataSetChanged();
    }

    public ArrayList<Tuple<String,Float>> getData() { return mPriceHistoryList;}

    class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView shopText;
        TextView priceText;
        ImageView ivDummy;

        private ListItemViewHolder(View view) {
            super(view);
            shopText = (TextView) view.findViewById(R.id.tv_list_item);
            priceText = (TextView) view.findViewById(R.id.tv_list_item_price);
            ivDummy = (ImageView)view.findViewById(R.id.iv_item_icon);

            ivDummy.setVisibility(View.GONE);

            view.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mClickListener.onListItemClick(position);
        }
    }
}
