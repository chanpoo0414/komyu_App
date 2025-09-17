package com.example.komyuapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.VH> {

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
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_activity, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Item it = data.get(position);
        holder.title.setText(it.title);
        holder.sub.setText(it.subtitle);
        holder.amount.setText(it.amount);
        if (it.iconRes != 0) {
            holder.icon.setImageResource(it.iconRes);
        } else {
            // keep default icon (ic_launcher_round) or set a tint
        }
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, sub, amount;
        ImageView icon;
        VH(View v) {
            super(v);
            icon = v.findViewById(R.id.img_icon);
            title = v.findViewById(R.id.tv_title);
            sub = v.findViewById(R.id.tv_sub);
            amount = v.findViewById(R.id.tv_amount);
        }
    }
}
