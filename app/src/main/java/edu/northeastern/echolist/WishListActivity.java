package edu.northeastern.echolist;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WishListActivity extends AppCompatActivity {
    private FloatingActionButton fabAddWishListItem;
    private WishListAdapter wishListAdapter;
    private RecyclerView wishListRecyclerView;
    private RecyclerView.LayoutManager lLayoutManager;
    private List<WishListItem> wishList = new ArrayList<>();
    private String eventId;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist_layout);

        eventId = getIntent().getStringExtra("eventId");

        fabAddWishListItem = findViewById(R.id.fabAddWishListItem);
        wishListRecyclerView = findViewById(R.id.wishlist_recyclerview);

        wishListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        wishListAdapter = new WishListAdapter(this, wishList, eventId);
        wishListRecyclerView.setAdapter(wishListAdapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
//        bottomNavigationView.setSelectedItemId(R.id.page_view_posts);
        NavigationRouter navigationRouter = new NavigationRouter(bottomNavigationView, this);
        navigationRouter.initNavigation();

        //        bottomNavigationView.setSelectedItemId(R.id.page_view_posts);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.page_home) {
                Intent intent = new Intent(WishListActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.page_add_post) {
                Intent intent = new Intent(WishListActivity.this, AddItemActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.page_view_posts) {
                Intent intent = new Intent(WishListActivity.this, MyListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            return false;
        });

        DatabaseReference databaseWishLists = FirebaseDatabase.getInstance().getReference("wishlists").child(eventId);

        databaseWishLists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                wishList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    WishListItem item = snapshot.getValue(WishListItem.class);
                    wishList.add(item);
                }

                // ensure wish list items are ordered by the order property
                Collections.sort(wishList, new Comparator<WishListItem>() {
                    @Override
                    public int compare(WishListItem o1, WishListItem o2) {
                        return Integer.compare(o1.getOrder(), o2.getOrder());
                    }
                });

                wishListAdapter.notifyDataSetChanged(); // Refresh the list
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                Toast.makeText(WishListActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
        fabAddWishListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WishListActivity.this);
                builder.setTitle("Add Wish List Item");

                View view = getLayoutInflater().inflate(R.layout.activity_add_wishlist_item_dialog, null);
                builder.setView(view);

                EditText nameAdd = view.findViewById(R.id.addName);

                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = nameAdd.getText().toString();
                        if (!name.isEmpty()) {
                            String id = databaseWishLists.push().getKey();
                            int order = wishList.size();
                            WishListItem newItem = new WishListItem(id, name, order);
                            if (id != null) {
                                databaseWishLists.child(id).setValue(newItem);
                            }
                        } else {
                            // a error dialog will show if name and url are empty
                            emptyEntryDialog();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.create().show();

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
                                        .setAction("Undo", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                itemRef.setValue(deletedWishListItem);
                                                wishList.add(index, deletedWishListItem);
                                                wishListAdapter.notifyItemInserted(index);
                                            }
                                        });
                                snackbar.show();

                    }

                });
        itemTouchHelper.attachToRecyclerView(wishListRecyclerView);

    }

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


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SaveWishList saveWishList = new SaveWishList();
        saveWishList.setWishList(wishList);
        ;       outState.putSerializable("wishlist_key", saveWishList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SaveWishList saveWishList = (SaveWishList) savedInstanceState.getSerializable("wishlist_key");
        wishListAdapter.setWishList(saveWishList.getWishList());
    }



}
