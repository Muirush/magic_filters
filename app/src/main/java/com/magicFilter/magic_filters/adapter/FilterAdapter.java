package com.magicFilter.magic_filters.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.magicFilter.magic_filters.R;
import com.magicFilter.magic_filters.bean.FilterBean;
import com.magicFilter.magic_filters.model.RecyclerViewItemClick;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {

    private List<FilterBean> list;
    private Activity activity;
    private RecyclerViewItemClick recyclerViewItemClick;
    private int lastClickPosition = -1;
    private boolean isFilterLock = false;

    public FilterAdapter(Activity activity, List<FilterBean> list) {
        this.activity = activity;
        this.list = list;
    }

    public void setRecyclerViewItemClick(RecyclerViewItemClick recyclerViewItemClick) {
        this.recyclerViewItemClick = recyclerViewItemClick;
    }

    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FilterViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_rv_filter, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, final int position) {
        FilterBean filterBean = list.get(position);
        holder.ivFilter.setImageResource(filterBean.getImage());
        holder.tvFilterName.setText(filterBean.getName());
        if (isFilterLock) {
            holder.ivFilterOver.setImageResource(R.drawable.ic_lock);
            holder.ivFilterOver.setVisibility(View.VISIBLE);
        } else {
            if (position == lastClickPosition) {
                holder.ivFilter.setBackgroundResource(R.drawable.round_filter_background);
                holder.ivFilterOver.setImageResource(R.drawable.ic_select_filter);
                holder.ivFilterOver.setVisibility(View.VISIBLE);
                holder.tvFilterName.setAlpha(1.0f);
            } else {
                holder.ivFilter.setBackground(null);
                holder.ivFilterOver.setVisibility(View.GONE);
                holder.tvFilterName.setAlpha(0.6f);
            }
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                recyclerViewItemClick.onClick(v, position);

            }
        });
    }

    public void singleChoose(int position){
        lastClickPosition = position;
        notifyDataSetChanged();
    }

    public void setFilterLock(boolean filterLock) {
        isFilterLock = filterLock;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class FilterViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivFilter;
        private TextView tvFilterName;
        private ImageView ivFilterOver;

        public FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFilter = itemView.findViewById(R.id.iv_filter);
            tvFilterName = itemView.findViewById(R.id.tv_filter_name);
            ivFilterOver = itemView.findViewById(R.id.iv_filter_over);
        }
    }

}
