package com.gururaj.pricetracker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gururaj.pricetracker.data.Tuple;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Gururaj on 5/9/2017.
 */

public class ItemDetailsAdaptor extends RecyclerView.Adapter<ItemDetailsAdaptor.ListItemViewHolder> {

    private ArrayList<Tuple<String,Float>> mHmap;
    ListItemClickListener mListItemClickListener;
    private int mIndex = -1;

    public ItemDetailsAdaptor(ArrayList<Tuple<String,Float>> hmap,ListItemClickListener clickListener) {
        mHmap = hmap;
        mListItemClickListener = clickListener;
    }

    //Interface for click handling
    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex,String item);
    }
    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int resId = R.layout.layout_list_item;
        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        View view = inflator.inflate(resId,parent,false);
        ListItemViewHolder holder = new ListItemViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {

        if(position < mHmap.size()) {
            String shopQuote = mHmap.get(position).x;
            String shopUnQuote = shopQuote.substring(1,shopQuote.length()-1);
            holder.shopText.setText(shopUnQuote);
            holder.priceText.setText(String.format("%.1f",mHmap.get(position).y));//   valueOf(mHmap.get(position).y));
            //holder.itemView.setId(position);
            //holder.ivShop.setImageResource(R.drawable.ic_store);
        }

    }

    @Override
    public int getItemCount() {
        if(mHmap != null) {
            return mHmap.size();
        }
        else
            return 0;
    }

    public void setData(ArrayList<Tuple<String,Float>> map) {
        mHmap = map;
        notifyDataSetChanged();
    }

    public ArrayList<Tuple<String,Float>> getData() {
        return mHmap;
    }


    class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView shopText;
        TextView priceText;
        ImageView ivShop;

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            TextView shopNameTV = (TextView)itemView.findViewById(R.id.tv_list_item);
            String shopName = shopNameTV.getText().toString();

            mListItemClickListener.onListItemClick(position,shopName);

        }

        private ListItemViewHolder(View view) {
            super(view);
            shopText = (TextView)view.findViewById(R.id.tv_list_item);
            priceText = (TextView)view.findViewById(R.id.tv_list_item_price);
            ivShop = (ImageView)view.findViewById(R.id.iv_item_icon);

            ivShop.setVisibility(View.GONE);

            view.setOnClickListener(this);


        }
    }
}
