package edu.northeastern.echolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WishListActivity extends AppCompatActivity {
    private WishListAdapter wishListAdapter;
    private RecyclerView wishListRecyclerView;
    private List<WishListItem> wishList = new ArrayList<>();
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist_layout);

        eventId = getIntent().getStringExtra("eventId");
        String eventTitle = getIntent().getStringExtra("eventTitle");

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarwishlist);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(eventTitle + " Wish List");
        }

        wishListRecyclerView = findViewById(R.id.wishlist_recyclerview);

        wishListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        wishListAdapter = new WishListAdapter(this, wishList, eventId);
        wishListRecyclerView.setAdapter(wishListAdapter);


        DatabaseReference databaseWishLists = FirebaseDatabase.getInstance().getReference("wishlists").child(eventId);

        databaseWishLists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                wishList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    WishListItem item = snapshot.getValue(WishListItem.class);
                    wishList.add(item);
                }

                wishListAdapter.notifyDataSetChanged(); // Refresh the list
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(WishListActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        int fromPosition = viewHolder.getAdapterPosition();
                        int toPosition = target.getAdapterPosition();

                        WishListItem fromWishListItem = wishList.get(fromPosition);
                        wishList.remove(fromPosition);
                        wishList.add(toPosition, fromWishListItem);
                        wishListAdapter.notifyItemMoved(fromPosition, toPosition);

                        updateFirebaseWithNewOrder();

                        return true;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int index = viewHolder.getLayoutPosition();
                        WishListItem deletedWishListItem = wishList.get(index);


                        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("wishlists").child(eventId).child(deletedWishListItem.getId());
                        itemRef.removeValue();
                        wishList.remove(index);
                        wishListAdapter.notifyItemRemoved(index);
                        Snackbar snackbar = Snackbar.make(wishListRecyclerView,
                                                "Wish List Item is deleted", Snackbar.LENGTH_SHORT)
                                        .setAction("Undo", v -> {
                                            itemRef.setValue(deletedWishListItem);
                                            wishList.add(index, deletedWishListItem);
                                            wishListAdapter.notifyItemInserted(index);
                                        });
                                snackbar.show();
                    }

                });
        itemTouchHelper.attachToRecyclerView(wishListRecyclerView);

    }

    // Iterate over each wishlist item. Put the index of each wishlist item to a hashmap as the value
    // to store the order of each item. Update the order field of each item with the order from the
    // hashmap in Firebase
    private void updateFirebaseWithNewOrder() {
        DatabaseReference databaseWishLists = FirebaseDatabase.getInstance().getReference("wishlists").child(eventId);
        for (int i = 0; i < wishList.size(); i++) {
            WishListItem item = wishList.get(i);
            Map<String, Object> itemUpdates = new HashMap<>();
            itemUpdates.put("order", i + 1);
            databaseWishLists.child(item.getId()).updateChildren(itemUpdates);
        }
    }

    private void emptyEntryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Please enter name of the item");
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }


    // save the state of the wishlist
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SaveWishList saveWishList = new SaveWishList();
        saveWishList.setWishList(wishList);
        ;       outState.putSerializable("wishlist_key", saveWishList);
    }

    // restore the state of the wishlist
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SaveWishList saveWishList = (SaveWishList) savedInstanceState.getSerializable("wishlist_key");
        wishListAdapter.setWishList(saveWishList.getWishList());
    }
}
