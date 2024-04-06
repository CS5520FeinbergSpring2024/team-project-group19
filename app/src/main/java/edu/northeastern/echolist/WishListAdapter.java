package edu.northeastern.echolist;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.ViewHolder> {
    private List<WishListItem> wishList;

    public WishListAdapter(Context context, List<WishListItem> wishList) {
        this.wishList = wishList;
    }

    @Override
    public WishListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_wishlist_item_layout, parent, false);
        return new WishListAdapter.ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(WishListAdapter.ViewHolder holder, int position) {
        WishListItem wishListItem = wishList.get(position);
        holder.titleTextView.setText(wishListItem.getTitle());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MyListActivity.class);
                intent.putExtra("wishListId", wishListItem.getId());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wishList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;

        public ViewHolder(View itemView, WishListAdapter wishListAdapter) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.name);
        }
    }
}
