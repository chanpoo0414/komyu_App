package com.example.komyuapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_EMPTY = 0;
    private static final int TYPE_ITEM = 1;

    public static class Item {
        public String title;
        public String subtitle;
        public String amount;
        public int iconRes = 0; // optional drawable resource id
    }

    private List<Item> data;

    public RecentActivityAdapter(List<Item> data) {
        this.data = data;
    }

    public void setData(List<Item> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (data == null || data.isEmpty()) {
            return TYPE_EMPTY;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_EMPTY) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empty_recentactivity, parent, false);
            return new EmptyViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_activity, parent, false);
            return new ItemViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            Item item = data.get(position);

            itemHolder.tvTitle.setText(item.title);
            itemHolder.tvSub.setText(item.subtitle);
            itemHolder.tvAmount.setText(item.amount);

            // Set different icons based on activity type
            if (item.iconRes != 0) {
                itemHolder.imgIcon.setImageResource(item.iconRes);
            } else {
                // Fallback to default based on amount or type
                if (item.amount.startsWith("+")) {
                    itemHolder.imgIcon.setImageResource(R.drawable.plus_points_icon);
                } else if (item.amount.startsWith("-")) {
                    itemHolder.imgIcon.setImageResource(R.drawable.bus_icon);
                } else {
                    itemHolder.imgIcon.setImageResource(R.drawable.plus_points_icon);
                }
            }

            // Set amount color based on positive/negative
            if (item.amount.startsWith("+")) {
                itemHolder.tvAmount.setTextColor(ContextCompat.getColor(itemHolder.itemView.getContext(), R.color.greenKomyu));
            } else if (item.amount.startsWith("-")) {
                itemHolder.tvAmount.setTextColor(ContextCompat.getColor(itemHolder.itemView.getContext(), R.color.RedLogout));
            } else {
                itemHolder.tvAmount.setTextColor(ContextCompat.getColor(itemHolder.itemView.getContext(), android.R.color.black));
            }
        }
        // EmptyViewHolder doesn't need binding
    }

    @Override
    public int getItemCount() {
        if (data == null || data.isEmpty()) {
            return 1; // Return 1 for empty view
        }
        return data.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSub, tvAmount;
        ImageView imgIcon;

        ItemViewHolder(View v) {
            super(v);
            imgIcon = v.findViewById(R.id.img_icon);
            tvTitle = v.findViewById(R.id.tv_title);
            tvSub = v.findViewById(R.id.tv_sub);
            tvAmount = v.findViewById(R.id.tv_amount);
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmptyMessage;

        EmptyViewHolder(View v) {
            super(v);
            tvEmptyMessage = v.findViewById(R.id.tv_empty_message);
        }
    }
}