package edu.northeastern.echolist;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.ViewHolder> {
    private List<WishListItem> wishList;
    private Context context;
    private String eventId;
    private Button editButton;

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

    @Override
    public void onBindViewHolder(WishListAdapter.ViewHolder holder, int position) {
        WishListItem wishListItem = wishList.get(position);
        holder.titleTextView.setText(wishListItem.getTitle());
        holder.purchaseCheckBox.setChecked(wishListItem.isPurchased());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // trigger onWishListItemClick when user taps on an item of the wishlist recycler
                // view. the onWishListItemClick is for editing the wishlist item
                onWishListItemClick(wishListItem);
//                Intent intent = new Intent(v.getContext(), WishListActivity.class);
//                intent.putExtra("wishListId", wishListItem.getId());
//                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wishList.size();
    }

    public List<WishListItem> getWishList() {
        return this.wishList;
    }

    public void onWishListItemClick(WishListItem wishListItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_add_wishlist_item_dialog, null);

        builder.setView(view);

        EditText nameAdd = view.findViewById(R.id.addName);

        String originalName = wishListItem.getTitle();

        nameAdd.setText( wishListItem.getTitle());
        nameAdd.setSelection( wishListItem.getTitle().length());

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String newName = nameAdd.getText().toString();
                if (!newName.isEmpty()) {
                    DatabaseReference databaseWishLists = FirebaseDatabase.getInstance().getReference("wishlists").child(eventId).child(wishListItem.getId());
                    databaseWishLists.child("title").setValue(newName).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Data successfully updated in Firebase
                            wishListItem.setTitle(newName);
                            notifyDataSetChanged(); // refresh the RecyclerView
                        }
                    });
                } else {
                    emptyEntryDialog();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void emptyEntryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Error");
        builder.setMessage("Please enter name");
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }

    public void editWishListItem(int index) {
        WishListItem wishListItem = wishList.get(index);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_add_wishlist_item_dialog, null);

        builder.setView(view);

        EditText nameAdd = view.findViewById(R.id.addName);

        String originalName = wishListItem.getTitle();

        nameAdd.setText( wishListItem.getTitle());
        nameAdd.setSelection( wishListItem.getTitle().length());

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String newName = nameAdd.getText().toString();
                if (!newName.isEmpty()) {
                    DatabaseReference databaseWishLists = FirebaseDatabase.getInstance().getReference("wishlists").child(eventId).child(wishListItem.getId());
                    databaseWishLists.child("title").setValue(newName).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Data successfully updated in Firebase
                            wishListItem.setTitle(newName);
                            notifyDataSetChanged(); // refresh the RecyclerView
                        }
                    });
                } else {
                    emptyEntryDialog();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        private Button editButton;
        public CheckBox purchaseCheckBox;

        public ViewHolder(View itemView, WishListAdapter wishListAdapter) {
            super(itemView);
            this.titleTextView = itemView.findViewById(R.id.name);
            this.editButton = itemView.findViewById(R.id.editbutton);
            purchaseCheckBox = itemView.findViewById(R.id.purchaseCheckbox);

            // checkbox for user to select
            purchaseCheckBox.setOnCheckedChangeListener(((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    WishListItem item = wishListAdapter.getWishList().get(position);
                    item.setPurchased(isChecked);
                    DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("wishlists").child(wishListAdapter.eventId).child(item.getId());
                    itemRef.child("purchased").setValue(isChecked);
                }
                })
            );

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int index = getLayoutPosition();
                    if (index != RecyclerView.NO_POSITION) {
                        wishListAdapter.onWishListItemClick(wishListAdapter.getWishList().get(index));
                    }
                }
            });

            // press the edit button will trigger editWishListItem which allows user to edit the
            // wishlist item
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = getAdapterPosition();
                    if (index != RecyclerView.NO_POSITION) {
                        wishListAdapter.editWishListItem(index);
                    }
                }
            });

        }
    }

    public void setWishList(List<WishListItem> wishList) {
        this.wishList.clear();
        this.wishList.addAll(wishList);
        notifyDataSetChanged();
    }
}
