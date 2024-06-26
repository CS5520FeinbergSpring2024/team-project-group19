package edu.northeastern.echolist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.ViewHolder> {
    private List<WishListItem> wishList;
    private Context context;
    private String eventId;

    public WishListAdapter(Context context, List<WishListItem> wishList, String eventId) {
        this.wishList = wishList;
        this.context = context;
        this.eventId = eventId;
    }

    @Override
    public WishListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_wishlist_item_layout, parent, false);
        return new WishListAdapter.ViewHolder(view, this);
    }

    // Method to remove an item from the local list
    public void removeWishlistItem(String itemId) {
        for (int i = 0; i < wishList.size(); i++) {
            if (wishList.get(i).getId().equals(itemId)) {
                wishList.remove(i);
                notifyItemChanged(i); // Notify adapter that data at index i has changed
                return;
            }
        }
    }

    @Override
    public void onBindViewHolder(WishListAdapter.ViewHolder holder, int position) {
        WishListItem wishListItem = wishList.get(position);
        holder.titleTextView.setText(wishListItem.getTitle());

        if (wishListItem.isPurchased()) {
            holder.moreButton.setVisibility(View.GONE);
            holder.purchaseDisplay.setVisibility(View.VISIBLE);
        } else {
            holder.moreButton.setVisibility(View.VISIBLE);
            holder.purchaseDisplay.setVisibility(View.GONE);
        }

        holder.moreButton.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(v.getContext(), holder.moreButton);
            menu.inflate(R.menu.gift_settings_menu);
            menu.setOnMenuItemClickListener(i -> {
                if (i.getItemId() == R.id.purchase_gift) {
                    wishListItem.setPurchased(true);
                    DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("wishlists").child(eventId).child(wishListItem.getId());
                    itemRef.child("purchased").setValue(true);
                    holder.purchaseDisplay.setVisibility(View.VISIBLE);
                    holder.moreButton.setVisibility(View.GONE);
                } else if (i.getItemId() == R.id.remove_gift) {
                    DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("wishlists").child(eventId).child(wishListItem.getId());
                    itemRef.removeValue()
                            .addOnSuccessListener(unused -> {
                                // Remove the item from the local list and notify the adapter
                                removeWishlistItem(wishListItem.getId());
                                Toast.makeText(v.getContext(), "Gift removed from wishlist", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(v.getContext(), "Failed to remove gift from wishlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
                return true;
            });
            menu.show();
        });
    }

    @Override
    public int getItemCount() {
        return wishList.size();
    }

    public List<WishListItem> getWishList() {
        return this.wishList;
    }

    private void emptyEntryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Error");
        builder.setMessage("Please enter name");
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView purchaseDisplay;
        public ImageButton moreButton;


        public ViewHolder(View itemView, WishListAdapter wishListAdapter) {
            super(itemView);
            this.titleTextView = itemView.findViewById(R.id.name);
            this.purchaseDisplay = itemView.findViewById(R.id.purchasedIndicator);
            moreButton = itemView.findViewById(R.id.moreButton);
        }
    }

    public void setWishList(List<WishListItem> wishList) {
        this.wishList.clear();
        this.wishList.addAll(wishList);
        notifyDataSetChanged();
    }
}
